package org.hogwarts.android.feature.subjects.textbook.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hogwarts.android.core.designsystem.component.LocalWebLink
import org.hogwarts.android.core.designsystem.theme.BrandFonts
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.textbook.data.ReaderAnchor
import org.hogwarts.android.feature.subjects.textbook.data.ReaderPrefs
import org.hogwarts.android.feature.subjects.textbook.domain.Book
import org.hogwarts.android.feature.subjects.textbook.domain.TextbookLoad
import org.hogwarts.android.feature.subjects.textbook.engine.BookPaginator
import org.hogwarts.android.feature.subjects.textbook.engine.BookSearch
import org.hogwarts.android.feature.subjects.textbook.engine.ComposeLineBreaker
import org.hogwarts.android.feature.subjects.textbook.engine.Pagination
import org.hogwarts.android.feature.subjects.textbook.engine.ReaderMetrics
import org.hogwarts.android.feature.subjects.textbook.engine.ReaderType
import org.hogwarts.android.feature.subjects.textbook.engine.ScreenKind
import org.hogwarts.android.feature.subjects.textbook.engine.SearchHit
import org.hogwarts.android.feature.subjects.textbook.engine.TextRole
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

/**
 * The textbook, read in the app — the web's Books-style reader
 * (`/subjects/{slug}/textbook`, `textbook/book.tsx`). Cover, contents, then
 * the chapters, one screen per page, turned by a tap on the outer 22% or a
 * swipe; the middle shows and hides the chrome.
 */
@Composable
fun TextbookReaderScreen(
    onClose: () -> Unit,
    onOpenPdf: (String) -> Unit,
    viewModel: TextbookReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val lang = if (Locale.getDefault().language == "en") "en" else "ar"
    val loaded = state as? ReaderUiState.Loaded
    val p = prefs
    when {
        loaded == null || p == null -> Box(Modifier.fillMaxSize().background(Color.White), Alignment.Center) {
            CircularProgressIndicator(Modifier.size(28.dp), color = Color(0xFF8E8E93), strokeWidth = 2.dp)
        }
        loaded.load is TextbookLoad.Ready -> BookReader(
            book = loaded.load.book,
            initialAnchor = loaded.anchor,
            prefs = p,
            bookmarks = bookmarks,
            lang = lang,
            viewModel = viewModel,
            onClose = onClose,
        )
        else -> ReaderFallback(
            message = stringResource(
                if (loaded.load is TextbookLoad.NoText) R.string.reader_no_text else R.string.reader_unavailable,
            ),
            pdfUrl = when (val l = loaded.load) {
                is TextbookLoad.NoText -> l.pdfUrl
                is TextbookLoad.Unavailable -> l.pdfUrl
                else -> null
            },
            onOpenPdf = onOpenPdf,
            onClose = onClose,
        )
    }
}

/** The web's `Fallback`: the message and the PDF, when there is no text to read. */
@Composable
private fun ReaderFallback(message: String, pdfUrl: String?, onOpenPdf: (String) -> Unit, onClose: () -> Unit) {
    val c = HogwartsTheme.colors
    Box(Modifier.fillMaxSize().background(c.background)) {
        RoundButton(
            ReaderIcons.XThin, stringResource(R.string.reader_close), readerPalette(
                org.hogwarts.android.feature.subjects.textbook.data.ReaderThemeName.Original, c.isDark,
            ),
            modifier = Modifier.align(Alignment.TopEnd).padding(WindowInsetsTop()).padding(top = 12.dp, end = 35.dp),
            iconSize = 28.dp, onClick = onClose,
        )
        Column(
            Modifier.align(Alignment.Center).padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(message, color = c.mutedForeground, textAlign = TextAlign.Center, fontSize = 16.sp)
            if (pdfUrl != null) {
                OutlinedButton(onClick = { onOpenPdf(pdfUrl) }, border = BorderStroke(1.dp, c.border), shape = RoundedCornerShape(8.dp)) {
                    Icon(ReaderIcons.FileText, null, tint = c.foreground, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.reader_open_pdf), color = c.foreground, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun WindowInsetsTop() = with(LocalDensity.current) {
    androidx.compose.foundation.layout.PaddingValues(top = WindowInsets.statusBars.getTop(this).toDp())
}

private enum class Sheet { Contents, Search, Settings, Customize }

private val TURN_EASE = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

@Composable
private fun BookReader(
    book: Book,
    initialAnchor: ReaderAnchor?,
    prefs: ReaderPrefs,
    bookmarks: List<Int>,
    lang: String,
    viewModel: TextbookReaderViewModel,
    onClose: () -> Unit,
) {
    val palette = readerPalette(prefs.theme, prefs.dark)
    val density = LocalDensity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webLink = LocalWebLink.current
    val contentsLabel = stringResource(R.string.reader_contents)
    val fontResolver = LocalFontFamilyResolver.current
    val bookDir = if (book.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val search = remember(book) { BookSearch(book) }

    var chrome by remember { mutableStateOf(true) }
    var menuOpen by remember { mutableStateOf(false) }
    var guideMenu by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf<Sheet?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SearchHit>>(emptyList()) }
    var activeHit by remember { mutableStateOf<ActiveHit?>(null) }
    var guideY by remember { mutableFloatStateOf(-1f) }

    // Rotation lock asks the activity to hold its orientation — and lets go when the reader closes.
    val activity = remember(context) { context.findActivity() }
    DisposableEffect(activity, prefs.rotationLocked) {
        activity?.requestedOrientation =
            if (prefs.rotationLocked) ActivityInfo.SCREEN_ORIENTATION_LOCKED else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    // The status bar's icons follow the page: light over a dark palette, dark over a light one.
    val view = androidx.compose.ui.platform.LocalView.current
    val lightPage = palette.bg.luminance() > 0.5f
    DisposableEffect(activity, lightPage) {
        val window = activity?.window
        val controller = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, view) }
        val before = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = lightPage
        controller?.isAppearanceLightNavigationBars = lightPage
        onDispose {
            if (before != null) {
                controller.isAppearanceLightStatusBars = before
                controller.isAppearanceLightNavigationBars = before
            }
        }
    }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1800)
            toast = null
        }
    }
    LaunchedEffect(query) {
        delay(200)
        results = if (query.trim().length < 2) emptyList() else withContext(Dispatchers.Default) { search.run(query) }
    }

    BackHandler(enabled = menuOpen || guideMenu) {
        menuOpen = false
        guideMenu = false
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(palette.bg)) {
        val statusTop = WindowInsets.statusBars.getTop(density)
        val navBottom = WindowInsets.navigationBars.getBottom(density)
        val topDp = with(density) { statusTop.toDp() }
        val bottomDp = with(density) { navBottom.toDp() }
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()
        val dp = density.density
        // `.book-viewport`: 3.75rem under the top, 3.5rem over the foot, min(100% − 2·clamp(1.25rem, 6vw, 4rem), 44rem) wide.
        val gutter = (screenW * 0.06f).coerceIn(20f * dp, 64f * dp)
        val pageW = floor(minOf(screenW - 2 * gutter, 704f * dp))
        val pageH = floor(screenH - statusTop - 60f * dp - 56f * dp - navBottom)
        val base = with(density) { 18.sp.toPx() } * prefs.scale
        val metrics = remember(pageW, pageH, base, prefs.leading) { ReaderMetrics(base, prefs.leading, dp, pageW, pageH) }
        val type = remember(metrics, prefs.sans, prefs.theme, book.rtl) {
            ReaderType(
                metrics, density,
                bookFont = if (prefs.sans) BrandFonts.LatinText else BrandFonts.ArabicText,
                sansFont = BrandFonts.ArabicText,
                bodyWeight = if (prefs.theme == org.hogwarts.android.feature.subjects.textbook.data.ReaderThemeName.Bold) FontWeight.SemiBold else FontWeight.Normal,
                rtl = book.rtl,
            )
        }

        var pagination by remember { mutableStateOf<Pagination?>(null) }
        var restored by remember { mutableStateOf(false) }
        val pager = rememberPagerState(pageCount = { pagination?.total ?: 1 })

        // Lay the book out for this screen and this type; keep the reader's place across a relayout.
        LaunchedEffect(type) {
            val keep = pagination?.let { old -> old.anchorOf(pager.currentPage) }
            val next = withContext(Dispatchers.Default) {
                val breaker = ComposeLineBreaker(
                    androidx.compose.ui.text.TextMeasurer(fontResolver, density, bookDir, 0), type, density,
                )
                BookPaginator(metrics, breaker).paginate(book, contentsLabel)
            }
            val target = when {
                keep != null -> next.screenFor(keep)
                !restored && initialAnchor != null -> next.screenFor(initialAnchor)
                else -> 0
            }
            restored = true
            pagination = next
            pager.scrollToPage(target.coerceIn(0, next.total - 1))
        }

        val pages = pagination
        LaunchedEffect(pager.settledPage, pages) {
            if (pages == null) return@LaunchedEffect
            delay(300)
            viewModel.saveAnchor(pages.anchorOf(pager.settledPage))
        }

        fun turn(forward: Boolean) {
            val target = pager.currentPage + if (forward) 1 else -1
            if (pages == null || target !in 0 until pages.total) return
            scope.launch { pager.animateScrollToPage(target, animationSpec = tween(360, easing = TURN_EASE)) }
        }

        fun goTo(index: Int) {
            scope.launch { pager.scrollToPage(index) }
        }

        val current = pages?.screens?.getOrNull(pager.currentPage)
        val currentPdfPage = current?.page
        val total = pages?.total ?: 0
        val globalPage = pager.currentPage + 1
        val percent = if (total > 1) ((pager.currentPage.toFloat() / (total - 1)) * 100).toInt() else 0
        val runningHead = when (current?.kind) {
            ScreenKind.Cover, null -> ""
            ScreenKind.Contents -> contentsLabel
            ScreenKind.Flow -> book.sections.getOrNull(current.flow)?.title ?: book.title
        }
        val lensH = metrics.bodyLine + 9.6f * dp
        val guideOn = prefs.guide

        // ── The page ───────────────────────────────────────────────────
        if (pages != null) {
            CompositionLocalProvider(LocalLayoutDirection provides bookDir) {
                HorizontalPager(
                    state = pager,
                    beyondViewportPageCount = 1,
                    key = { it },
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(guideOn) {
                            if (!guideOn) return@pointerInput
                            awaitEachGesture {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull() ?: break
                                    guideY = snapToLine(change.position.y, statusTop + 60f * dp, metrics.bodyLine)
                                    if (!change.pressed) break
                                }
                            }
                        },
                ) { index ->
                    val screen = pages.screens[index]
                    val sign = if (book.rtl) -1f else 1f
                    val offset = (pager.currentPage - index) + pager.currentPageOffsetFraction
                    Box(
                        Modifier.fillMaxSize()
                            .zIndex(if (offset < 0f) 0f else 1f)
                            .graphicsLayer {
                                val o = (pager.currentPage - index) + pager.currentPageOffsetFraction
                                if (o < 0f) translationX = sign * o * size.width * (1f - 0.174f)
                                val moving = abs(o) > 0.001f && o > 0f
                                shape = RoundedCornerShape(if (moving) 48.dp else 0.dp)
                                clip = moving
                            }
                            .drawWithContent {
                                drawContent()
                                val o = (pager.currentPage - index) + pager.currentPageOffsetFraction
                                if (o < 0f) drawRect(Color.Black, alpha = (1f - 0.771f) * (-o).coerceAtMost(1f))
                            }
                            .background(palette.bg)
                            .pointerInput(book.rtl, menuOpen) {
                                detectTapGestures { pos ->
                                    if (menuOpen) {
                                        menuOpen = false
                                        return@detectTapGestures
                                    }
                                    val x = pos.x / size.width
                                    when {
                                        x < 0.22f -> turn(forward = book.rtl)
                                        x > 0.78f -> turn(forward = !book.rtl)
                                        else -> chrome = !chrome
                                    }
                                }
                            },
                    ) {
                        if (screen.kind == ScreenKind.Cover) {
                            CoverScreen(book, palette, prefs.dark)
                        } else {
                            Box(
                                Modifier.fillMaxSize().padding(top = topDp + 60.dp),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                PageScreen(
                                    book = book, screen = screen, type = type, palette = palette, lang = lang,
                                    hit = activeHit, markColor = HogwartsTheme.colors.primary,
                                    onGoToPage = { page -> pages.screenForPage(page)?.let(::goTo) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Running head and counter ───────────────────────────────────
        if (runningHead.isNotEmpty()) {
            Box(
                Modifier.fillMaxWidth().padding(top = topDp + 12.dp).height(46.dp).padding(horizontal = 88.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(runningHead, color = palette.muted, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (current != null && current.kind != ScreenKind.Cover) {
            Box(
                Modifier.align(Alignment.BottomCenter).padding(bottom = bottomDp + 12.dp).height(46.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (chrome) stringResource(R.string.reader_page_of_total, formatNumber(globalPage, lang), formatNumber(total, lang))
                    else formatNumber(globalPage, lang),
                    color = palette.muted, fontSize = 15.sp,
                )
            }
        }

        // ── Line guide ─────────────────────────────────────────────────
        if (guideOn) {
            val y = if (guideY < 0f) screenH / 2 else guideY
            val inset = (screenW * 0.04f).coerceIn(20f * dp, maxOf(20f * dp, screenW / 2 - 336f * dp))
            LineGuide(palette, prefs.guideDim, y, lensH, inset)
        }

        // ── Brightness veil ────────────────────────────────────────────
        if (prefs.brightness < ReaderPrefs.BRIGHTNESS_MAX) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = (ReaderPrefs.BRIGHTNESS_MAX - prefs.brightness) / 100f)))
        }

        // ── Chrome ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = chrome && !menuOpen, enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).padding(top = topDp + 12.dp, end = 35.dp),
        ) {
            RoundButton(ReaderIcons.XThin, stringResource(R.string.reader_close), palette, iconSize = 28.dp, onClick = onClose)
        }
        AnimatedVisibility(
            visible = chrome && !menuOpen && !guideMenu, enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = bottomDp + 12.dp, end = 35.dp),
        ) {
            RoundButton(ReaderIcons.Menu, stringResource(R.string.reader_reading_menu), palette, iconSize = 34.4.dp) {
                menuOpen = true
            }
        }
        if (guideOn) {
            RoundButton(
                ReaderIcons.LineGuide, stringResource(R.string.reader_line_guide_options), palette,
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = bottomDp + 12.dp, start = 35.dp),
            ) { guideMenu = !guideMenu }
        }

        if (menuOpen) {
            ReadingMenu(
                palette = palette,
                lang = lang,
                percent = percent,
                rotationLocked = prefs.rotationLocked,
                guide = guideOn,
                bookmarked = currentPdfPage != null && currentPdfPage in bookmarks,
                canBookmark = currentPdfPage != null,
                bottomInset = bottomDp,
                onClose = { menuOpen = false },
                onContents = {
                    menuOpen = false
                    sheet = Sheet.Contents
                },
                onScrub = { ratio -> if (total > 0) goTo((ratio * (total - 1)).toInt().coerceIn(0, total - 1)) },
                onSearch = {
                    menuOpen = false
                    sheet = Sheet.Search
                },
                onSettings = {
                    menuOpen = false
                    sheet = Sheet.Settings
                },
                onShare = {
                    menuOpen = false
                    val base = webLink.url("/subjects/${book.slug}/textbook")
                    val url = if (currentPdfPage != null) "$base#p-$currentPdfPage" else base
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(Intent.EXTRA_SUBJECT, book.title).putExtra(Intent.EXTRA_TEXT, url)
                    context.startActivity(Intent.createChooser(send, book.title))
                },
                onToggleRotation = {
                    menuOpen = false
                    viewModel.updatePrefs { it.copy(rotationLocked = !it.rotationLocked) }
                },
                onToggleGuide = {
                    menuOpen = false
                    viewModel.updatePrefs { it.copy(guide = !it.guide) }
                },
                onBookmark = {
                    currentPdfPage?.let { page ->
                        val added = viewModel.toggleBookmark(page)
                        toast = context.getString(if (added) R.string.reader_bookmark_added else R.string.reader_bookmark_removed)
                    }
                },
            )
        }
        if (guideMenu) {
            GuideMenu(
                palette = palette,
                dim = prefs.guideDim,
                bottomInset = bottomDp,
                onDim = { level ->
                    viewModel.updatePrefs { it.copy(guideDim = level) }
                    guideMenu = false
                },
                onTurnOff = {
                    viewModel.updatePrefs { it.copy(guide = false) }
                    guideMenu = false
                },
                onDismiss = { guideMenu = false },
            )
        }
        toast?.let { ReaderToast(it, palette, bottomDp) }

        // ── Sheets ─────────────────────────────────────────────────────
        val sheetMax = with(density) { screenH.toDp() }
        when (sheet) {
            Sheet.Contents -> ContentsSheet(
                book = book, lang = lang, currentPage = currentPdfPage, globalPage = globalPage, totalPages = total,
                bookmarks = bookmarks, maxHeight = sheetMax,
                onNavigate = { page ->
                    sheet = null
                    pages?.screenForPage(page)?.let(::goTo)
                },
                onClose = { sheet = null },
            )
            Sheet.Search -> SearchSheet(
                book = book, lang = lang, query = query, results = results, maxHeight = sheetMax,
                onQuery = {
                    activeHit = null
                    query = it
                },
                onPick = { hit ->
                    sheet = null
                    activeHit = ActiveHit(query, hit.flow, hit.pos)
                    pages?.let { goTo(it.screenFor(hit.flow, hit.pos)) }
                },
                onClose = { sheet = null },
            )
            Sheet.Settings -> SettingsSheet(
                prefs = prefs, lang = lang, onPrefs = viewModel::updatePrefs,
                onCustomize = { sheet = Sheet.Customize }, onClose = { sheet = null },
            )
            Sheet.Customize -> CustomizeSheet(
                prefs = prefs, maxHeight = sheetMax, onPrefs = viewModel::updatePrefs, onClose = { sheet = null },
            )
            null -> Unit
        }
    }
}

/** A line's middle on the flow's grid, so the guide holds one line rather than straddling two. */
private fun snapToLine(y: Float, top: Float, line: Float): Float =
    if (line <= 0f || y < top) y else top + (floor((y - top) / line) + 0.5f) * line

private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
