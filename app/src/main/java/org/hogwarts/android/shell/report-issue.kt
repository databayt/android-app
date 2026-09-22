package org.hogwarts.android.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.BuildConfig
import org.hogwarts.android.R
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import retrofit2.http.Body
import retrofit2.http.POST
import timber.log.Timber
import javax.inject.Inject

/** `/api/mobile/report` — the web's `reportIssue` action, over a bearer token. */
interface ReportApi {
    @POST("api/mobile/report")
    suspend fun report(@Body body: ReportRequest): ReportResponse
}

@Serializable
data class ReportRequest(
    val description: String,
    @SerialName("page_url") val pageUrl: String,
    val viewport: String,
    val direction: String,
    val client: String,
)

@Serializable
data class ReportResponse(
    val ok: Boolean = false,
    @SerialName("issue_number") val issueNumber: Int? = null,
)

/** A signed-in reporter's floor and everyone's ceiling — `REPORT_LIMITS`. */
private const val MIN_CHARS = 10
private const val MIN_TOKENS = 3
const val REPORT_MAX_CHARS = 2000
private const val COOLDOWN_MS = 60_000L
private const val SENT_ALERT_MS = 2_500L

/**
 * Distinct words of two characters or more, diacritics dropped —
 * `countMeaningfulTokens` in the web dialog, so "aaaa aaaa" does not count as
 * three words.
 */
internal fun countMeaningfulTokens(text: String): Int =
    text.lowercase()
        .replace(Regex("[\\u064B-\\u0670]"), "")
        .split(Regex("[\\s\\p{Punct}\\p{IsPunctuation}]+"))
        .filter { it.length >= 2 }
        .toSet()
        .size

data class ReportIssueUiState(
    /** The web URL of the page being reported; null while the sheet is shut. */
    val pageUrl: String? = null,
    val description: String = "",
    val sending: Boolean = false,
    val error: Boolean = false,
    val cooldownUntil: Long = 0L,
    /** The confirmation body, while "Report sent" is showing. */
    val sentBody: String? = null,
) {
    val cooling: Boolean get() = System.currentTimeMillis() < cooldownUntil
    val canSend: Boolean
        get() {
            val trimmed = description.trim()
            return trimmed.length >= MIN_CHARS && countMeaningfulTokens(trimmed) >= MIN_TOKENS &&
                !sending && !cooling
        }
}

@HiltViewModel
class ReportIssueViewModel @Inject constructor(
    private val api: ReportApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportIssueUiState())
    val uiState: StateFlow<ReportIssueUiState> = _uiState.asStateFlow()

    fun open(pageUrl: String) = _uiState.update { it.copy(pageUrl = pageUrl, error = false) }

    fun close() = _uiState.update { it.copy(pageUrl = null, error = false) }

    fun onDescription(text: String) =
        _uiState.update { it.copy(description = text.take(REPORT_MAX_CHARS), error = false) }

    fun dismissSent() = _uiState.update { it.copy(sentBody = null) }

    fun send(viewport: String, rtl: Boolean, sentBody: (Int?) -> String) {
        val state = _uiState.value
        val pageUrl = state.pageUrl ?: return
        if (!state.canSend) return
        _uiState.update { it.copy(sending = true, error = false) }
        viewModelScope.launch {
            val result = runCatching {
                api.report(
                    ReportRequest(
                        description = state.description,
                        pageUrl = pageUrl,
                        viewport = viewport,
                        direction = if (rtl) "rtl" else "ltr",
                        client = "hogwarts-android/${BuildConfig.VERSION_NAME}",
                    ),
                )
            }.onFailure { Timber.w(it, "Report failed") }.getOrNull()

            if (result?.ok == true) {
                // As on the web's phone: the sheet closes and the sent alert
                // shows, and a second report waits a minute.
                _uiState.update {
                    it.copy(
                        pageUrl = null,
                        description = "",
                        sending = false,
                        cooldownUntil = System.currentTimeMillis() + COOLDOWN_MS,
                        sentBody = sentBody(result.issueNumber),
                    )
                }
            } else {
                _uiState.update { it.copy(sending = false, error = true) }
            }
        }
    }
}

/**
 * The web dialog's phone face (`report-issue/dialog.tsx` MobileSheet): a
 * blank page like Notes — a slim bar with close at the start and the send
 * check at the end, then the text from the top down. Enter sends. Under it,
 * only when there is something to say: the error, or the cooldown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueSheet(
    state: ReportIssueUiState,
    onDescription: (String) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val focus = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null,
    ) {
        // `h-[calc(100dvh-2.5rem)]`: 40dp of the page stays visible above the
        // sheet. Measured against the room the sheet is actually given, which
        // already leaves out the system bars.
        BoxWithConstraints {
            Column(Modifier.fillMaxWidth().height(maxHeight - 40.dp).imePadding()) {
                Row(
                    Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.report_close),
                            tint = colors.mutedForeground, modifier = Modifier.size(20.dp))
                    }
                    Box(Modifier.weight(1f))
                    Box(
                        Modifier
                            .size(40.dp)
                            .alpha(if (state.canSend) 1f else 0.3f)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable(enabled = state.canSend, role = Role.Button, onClick = onSend),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.sending) {
                            CircularProgressIndicator(color = colors.primaryForeground, strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.report_send),
                                tint = colors.primaryForeground, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)) {
                    if (state.description.isEmpty()) {
                        Text(stringResource(R.string.report_composer_placeholder), fontSize = 17.sp, lineHeight = 24.sp,
                            color = colors.mutedForeground)
                    }
                    BasicTextField(
                        value = state.description,
                        onValueChange = { value ->
                            // Enter sends, as the web's does; it never inserts a newline.
                            if (value.endsWith("\n") && !state.description.endsWith("\n")) onSend() else onDescription(value)
                        },
                        textStyle = HogwartsTheme.type.body.copy(fontSize = 17.sp, lineHeight = 24.sp, color = colors.foreground),
                        cursorBrush = SolidColor(colors.foreground),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() }),
                        modifier = Modifier.fillMaxSize().focusRequester(focus),
                    )
                }
                if (state.error || state.cooling) {
                    Column(
                        Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.error) {
                            Text(stringResource(R.string.report_error), fontSize = 14.sp, color = colors.destructive)
                        }
                        if (state.cooling) {
                            Text(stringResource(R.string.report_cooldown), fontSize = 14.sp, color = colors.mutedForeground)
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
}

/**
 * `sent-alert.tsx`: a 250dp frosted card in the middle of the screen — a
 * filled disc with a check, "Report sent", the thanks — gone by itself after
 * two and a half seconds.
 */
@Composable
fun ReportSentAlert(body: String, onDismiss: () -> Unit) {
    val colors = HogwartsTheme.colors
    LaunchedEffect(body) {
        delay(SENT_ALERT_MS)
        onDismiss()
    }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .width(250.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.muted.copy(alpha = 0.85f))
                .padding(horizontal = 24.dp, vertical = 41.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val disc = colors.foreground.copy(alpha = 0.65f)
            val mark = colors.muted
            Box(
                Modifier.size(57.dp).drawBehind {
                    drawCircle(disc, radius = size.minDimension / 2f * (27f / 28.5f))
                    val s = size.width / 57f
                    drawLine(mark, Offset(17f * s, 29.5f * s), Offset(25f * s, 37.5f * s), strokeWidth = 4.5f * s, cap = StrokeCap.Round)
                    drawLine(mark, Offset(25f * s, 37.5f * s), Offset(41f * s, 20.5f * s), strokeWidth = 4.5f * s, cap = StrokeCap.Round)
                },
            )
            Text(stringResource(R.string.report_sent_title), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold,
                color = colors.foreground, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 33.dp))
            Text(body, fontSize = 17.sp, lineHeight = 24.sp, color = colors.foreground, textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 11.dp))
        }
    }
}
