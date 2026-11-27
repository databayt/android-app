package org.hogwarts.android.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.hogwarts.android.BuildConfig
import org.hogwarts.android.core.common.brand.AppVersionInfo
import org.hogwarts.android.core.common.brand.BrandConfig
import org.hogwarts.android.core.common.brand.LiveRoomPresence
import java.io.File

/** Where the in-app update is: what the gate screen draws below the notes. */
sealed interface UpdateStage {
    data object Idle : UpdateStage

    /** [percent] 0..100, or -1 while the size is unknown. */
    data class Downloading(val percent: Int) : UpdateStage

    data object Verifying : UpdateStage

    /** "Install unknown apps" is off: the screen asks for it and continues when the user comes back. */
    data object NeedsPermission : UpdateStage

    /** The system installer is open on top of the app. */
    data object Installing : UpdateStage

    /**
     * The verified file is on disk and the installer is not open. [afterBackOut]: the user left the
     * installer (or Play Protect stopped it) and is still on the old version, so show the hint.
     */
    data class ReadyToInstall(val afterBackOut: Boolean) : UpdateStage

    data class Failed(val reason: FailReason) : UpdateStage
}

enum class FailReason { Network, Incomplete, NoSpace, Installer }

/** One-shot instructions for the screen. */
sealed interface UpdateEvent {
    /** The installer could not be opened: send the user to the browser download instead. */
    data object OpenBrowser : UpdateEvent
}

/** What the screen draws: a decision plus the text, link and download stage it needs. */
data class UpdateUi(
    val decision: UpdateDecision = UpdateDecision.None,
    val apkUrl: String = "",
    val notesAr: String? = null,
    val notesEn: String? = null,
    val stage: UpdateStage = UpdateStage.Idle,
)

/**
 * Runs the version check and updates the app by itself: any newer version downloads quietly in the
 * background and installs when verified (no dialog, no "later"). Only a REQUIRED update draws the gate
 * UI. Nothing happens while a live class room is open: [LiveRoomPresence] releases it when the room is left.
 */
@HiltViewModel
class UpdateViewModel @javax.inject.Inject constructor(
    @ApplicationContext private val context: Context,
    private val checker: UpdateChecker,
    private val downloader: ApkDownloader,
    private val installer: ApkInstaller,
    private val brand: BrandConfig,
    private val app: AppVersionInfo,
) : ViewModel() {

    private val manifest = MutableStateFlow<UpdateManifest?>(null)

    // Lives in the view-model, so a rotation or recomposition never loses the download.
    private val stage = MutableStateFlow<UpdateStage>(UpdateStage.Idle)
    private var downloadJob: Job? = null
    private var installWhenReady = false

    private val _events = Channel<UpdateEvent>(Channel.BUFFERED)
    val events: Flow<UpdateEvent> = _events.receiveAsFlow()

    val ui: StateFlow<UpdateUi> = combine(manifest, LiveRoomPresence.inRoom, stage) { m, inRoom, currentStage ->
        if (m == null) return@combine UpdateUi()
        val decision = decideUpdate(app.versionCode, m.latestCode, m.minCode, inRoom)
        // Optional updates are silent: the screen only ever learns about a REQUIRED one.
        UpdateUi(decision, m.apkUrl?.takeIf { it.isNotBlank() } ?: brand.apkUrl, m.notesAr, m.notesEn, currentStage)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UpdateUi())

    init {
        // The whole automatic flow: every change of manifest / room / stage asks "what is next?".
        viewModelScope.launch {
            combine(manifest, LiveRoomPresence.inRoom, stage) { m, inRoom, st -> Triple(m, inRoom, st) }
                .collect { (m, inRoom, st) -> reconcile(m, inRoom, st) }
        }
    }

    private fun reconcile(m: UpdateManifest?, inRoom: Boolean, st: UpdateStage) {
        if (!BuildConfig.SELF_UPDATE || m == null) return
        val held = st is UpdateStage.ReadyToInstall && installWhenReady
        val action = nextUpdateAction(
            decision = decideUpdate(app.versionCode, m.latestCode, m.minCode, inRoom),
            inRoom = inRoom,
            downloaded = held,
            busy = st != UpdateStage.Idle && !held,
        )
        when (action) {
            UpdateAction.Download -> startUpdate()
            UpdateAction.Install -> install()
            UpdateAction.Nothing -> Unit
        }
    }

    /** Called on every activity start; the checker throttles to once per 30 min. */
    fun check() {
        if (!BuildConfig.SELF_UPDATE) return
        viewModelScope.launch {
            // A failed attempt is retried on the next start / return to the app.
            if (stage.value is UpdateStage.Failed && downloadJob?.isActive != true) stage.value = UpdateStage.Idle
            checker.check(brand.updateUrl)?.let { manifest.value = it }
        }
    }

    /** The update button: download in-app, verify, then open the installer. Also the retry. */
    fun startUpdate() {
        if (!BuildConfig.SELF_UPDATE) return
        val m = manifest.value ?: return
        val url = m.apkUrl?.takeIf { it.isNotBlank() } ?: brand.apkUrl
        if (url.isBlank() || downloadJob?.isActive == true) return
        val target = targetFile(m)
        installWhenReady = false
        stage.value = UpdateStage.Downloading(-1)
        downloadJob = viewModelScope.launch {
            val outcome = downloader.fetch(
                url = url,
                target = target,
                sha256 = m.sha256,
                onProgress = { percent -> if (isActive) stage.value = UpdateStage.Downloading(percent) },
                onVerifying = { if (isActive) stage.value = UpdateStage.Verifying },
            )
            when (outcome) {
                DownloadOutcome.Verified -> openInstaller(target)
                DownloadOutcome.Corrupt -> stage.value = UpdateStage.Failed(FailReason.Incomplete)
                DownloadOutcome.Network -> stage.value = UpdateStage.Failed(FailReason.Network)
                DownloadOutcome.NoSpace -> stage.value = UpdateStage.Failed(FailReason.NoSpace)
            }
        }
    }

    /** "Install update": the verified file is still on disk, so do not download again. */
    fun install() {
        if (!BuildConfig.SELF_UPDATE) return
        val m = manifest.value ?: return
        val target = targetFile(m)
        if (!target.exists()) startUpdate() else openInstaller(target)
    }

    /** Screen is in the foreground (ON_RESUME). */
    fun onResume() {
        UpdateForeground.resumed = true
        when (stage.value) {
            // Back from the settings page: continue as soon as the permission is on.
            UpdateStage.NeedsPermission -> if (installer.canInstall()) install()
            // Back from the installer and still running the old version: it was refused or backed out.
            UpdateStage.Installing -> stage.value = UpdateStage.ReadyToInstall(afterBackOut = true)
            is UpdateStage.ReadyToInstall -> if (installWhenReady) install()
            else -> Unit
        }
    }

    fun onPause() {
        UpdateForeground.resumed = false
    }

    private fun targetFile(m: UpdateManifest) =
        File(File(context.cacheDir, UPDATES_DIR), apkFileName(m.versionName, m.latestCode))

    private fun openInstaller(file: File) {
        // A session install needs no foreground activity, but a live class must never be interrupted.
        if (LiveRoomPresence.inRoom.value) {
            installWhenReady = true
            stage.value = UpdateStage.ReadyToInstall(afterBackOut = false)
            return
        }
        installWhenReady = false
        when (installer.launch(file)) {
            InstallLaunch.Launched -> stage.value = UpdateStage.Installing
            InstallLaunch.NeedsPermission -> stage.value = UpdateStage.NeedsPermission
            InstallLaunch.NoInstaller -> {
                stage.value = UpdateStage.Failed(FailReason.Installer)
                // Only the blocking gate may send the user to a browser; a silent update just waits for the next start.
                if (ui.value.decision == UpdateDecision.Required) _events.trySend(UpdateEvent.OpenBrowser)
            }
        }
    }
}
