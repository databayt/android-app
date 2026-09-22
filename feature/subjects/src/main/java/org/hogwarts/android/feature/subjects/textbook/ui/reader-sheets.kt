package org.hogwarts.android.feature.subjects.textbook.ui

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import org.hogwarts.android.core.designsystem.icon.LucideIcons
import org.hogwarts.android.core.designsystem.theme.BrandFonts
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.textbook.data.ReaderPrefs
import org.hogwarts.android.feature.subjects.textbook.data.ReaderThemeName
import org.hogwarts.android.feature.subjects.textbook.domain.Book
import org.hogwarts.android.feature.subjects.textbook.engine.SearchHit
import kotlin.math.roundToInt

/** Sheets portal to the page in the web, so they wear the app's theme, not the book's. */
private val bookUi: FontFamily = FontFamily.Default

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSheet(
    onDismiss: () -> Unit,
    color: Color,
    shape: RoundedCornerShape,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = color,
        contentColor = HogwartsTheme.colors.foreground,
        shape = shape,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = 0.2f),
        contentWindowInsets = { WindowInsets(0) },
    ) { content() }
}

// ── Contents ───────────────────────────────────────────────────────────

/**
 * Contents: the book's jacket, title and the folio the reader stands on,
 * then the chapters and lessons with the one being read caught in a filled
 * capsule, then the bookmarks.
 */
@Composable
internal fun ContentsSheet(
    book: Book,
    lang: String,
    currentPage: Int?,
    globalPage: Int?,
    totalPages: Int,
    bookmarks: List<Int>,
    maxHeight: Dp,
    onNavigate: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val dark = HogwartsTheme.colors.isDark
    val surface = if (dark) Color(0xFF1C1C1E) else Color(0xFFF5F5F5)
    val current = if (dark) Color(0xFF37373B) else Color(0xFFDFDFDF)
    val rule = if (dark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.13f)
    val muted = if (dark) Color(0xFF8B8A93) else Color(0xFF8E8E93)
    val ink = if (dark) Color(0xFFF2F2F7) else Color(0xFF1C1C1E)
    val gutter = 18.dp
    val currentChapter = book.toc.indices.firstOrNull { i ->
        val start = book.toc[i].page ?: return@firstOrNull false
        val next = book.toc.drop(i + 1).firstOrNull { it.page != null }?.page
        currentPage != null && currentPage >= start && (next == null || currentPage < next)
    }
    ReaderSheet(onClose, surface, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)) {
        Column(Modifier.fillMaxWidth().height(maxHeight * 0.94f)) {
            Row(
                Modifier.fillMaxWidth().padding(start = gutter, end = gutter, top = 16.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AsyncImage(
                    book.cover.url, null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.width(52.dp).height(72.dp)
                        .shadow(3.dp, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp)).background(current),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        book.title, color = ink, fontFamily = bookUi, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        lineHeight = 1.2.em, letterSpacing = (-0.02).em, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    )
                    if (globalPage != null && totalPages > 0) {
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = muted)) { append(stringResource(R.string.reader_page)) }
                                append(" ")
                                append(
                                    stringResource(
                                        R.string.reader_page_of_total,
                                        formatNumber(globalPage, lang), formatNumber(totalPages, lang),
                                    ),
                                )
                            },
                            color = ink, fontSize = 17.sp, modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                Box(
                    Modifier.size(48.dp).shadow(2.dp, CircleShape).clip(CircleShape)
                        .background(if (dark) Color.White.copy(alpha = 0.12f) else Color.White)
                        .clickable(role = Role.Button, onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        ReaderIcons.XBold, stringResource(R.string.reader_close),
                        tint = if (dark) Color(0xFFD6D5DC) else Color(0xFF88878C), modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(start = gutter, end = gutter, bottom = 32.dp).navigationBarsPadding(),
            ) {
                book.toc.forEachIndexed { ci, ch ->
                    val isCurrent = ci == currentChapter
                    val ruleAbove = ci > 0 && !isCurrent && ci - 1 != currentChapter
                    ContentsRow(
                        title = ch.name, page = ch.page, book = book, lang = lang, bold = true,
                        ink = ink, muted = muted, rule = if (ruleAbove) rule else Color.Transparent,
                        fill = if (isCurrent) current else null, gutter = gutter, minHeight = 52.dp,
                        fontSize = 17.sp, onNavigate = onNavigate,
                    )
                    ch.lessons.forEachIndexed { li, lesson ->
                        ContentsRow(
                            title = lesson.name, page = lesson.page, book = book, lang = lang, bold = false,
                            ink = ink, muted = muted, rule = if (li == 0 && isCurrent) Color.Transparent else rule,
                            fill = null, gutter = gutter, minHeight = 44.dp, fontSize = 16.sp, indent = 20.dp,
                            onNavigate = onNavigate,
                        )
                    }
                }
                Text(
                    stringResource(R.string.reader_bookmarks), color = HogwartsTheme.colors.mutedForeground,
                    fontSize = 12.8.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.06.em,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                )
                if (bookmarks.isEmpty()) {
                    Text(stringResource(R.string.reader_no_bookmarks), color = HogwartsTheme.colors.mutedForeground, fontSize = 14.sp)
                } else {
                    bookmarks.forEach { page ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onNavigate(page) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(ReaderIcons.BookmarkFilled, null, tint = ink, modifier = Modifier.size(16.dp))
                            Text(
                                stringResource(R.string.reader_page_n, formatNumber(book.printed(page), lang)),
                                color = ink, fontSize = 17.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentsRow(
    title: String, page: Int?, book: Book, lang: String, bold: Boolean,
    ink: Color, muted: Color, rule: Color, fill: Color?, gutter: Dp, minHeight: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit, indent: Dp = 0.dp, onNavigate: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(start = indent)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(rule))
        Row(
            Modifier
                .then(if (fill != null) Modifier.offset(x = 0.dp).fillMaxWidth() else Modifier.fillMaxWidth())
                .heightIn(min = minHeight)
                .then(
                    if (fill != null) {
                        Modifier.padding(horizontal = 0.dp).clip(RoundedCornerShape(14.dp)).background(fill)
                            .padding(horizontal = gutter)
                    } else Modifier,
                )
                .clickable(enabled = page != null) { page?.let(onNavigate) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                title, color = if (page == null) muted else ink, fontSize = fontSize, lineHeight = 1.35.em,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f),
            )
            if (page != null) Text(formatNumber(book.printed(page), lang), color = muted, fontSize = 17.sp)
        }
    }
}

// ── Search ─────────────────────────────────────────────────────────────

/** Search Book: hits above, the field and its ✕ in a bar at the foot (the reference's layout). */
@Composable
internal fun SearchSheet(
    book: Book,
    lang: String,
    query: String,
    results: List<SearchHit>,
    maxHeight: Dp,
    onQuery: (String) -> Unit,
    onPick: (SearchHit) -> Unit,
    onClose: () -> Unit,
) {
    val dark = HogwartsTheme.colors.isDark
    val surface = if (dark) Color(0xFF1C1C1E) else Color(0xFFFEFEFF)
    val capsule = if (dark) Color(0xFF2C2C2E) else Color(0xFFFDFDFD)
    val ink = if (dark) Color(0xFFF2F2F7) else Color.Black
    val placeholder = if (dark) Color(0xFF98989D) else Color(0xFF717171)
    val muted = HogwartsTheme.colors.mutedForeground
    val context = LocalContext.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(250)
        runCatching { focus.requestFocus() }
    }
    val speechIntent = remember(lang) {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == "ar") "ar-SA" else "en-US")
    }
    val canDictate = remember { speechIntent.resolveActivity(context.packageManager) != null }
    val dictation = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let(onQuery)
    }
    val trimmed = query.trim()
    // The sheet stops 47 dp under the status bar, keyboard up or down: the
    // container is clear and the card inside carries the surface.
    ReaderSheet(onClose, Color.Transparent, RoundedCornerShape(0.dp)) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = maxHeight).fillMaxHeight().statusBarsPadding().padding(top = 47.dp)
                .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp)).background(surface).imePadding(),
        ) {
            Text(
                stringResource(R.string.reader_search_book), color = ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 12.dp),
            )
            LazyColumn(Modifier.weight(1f).padding(horizontal = 20.dp)) {
                if (trimmed.length >= 2 && results.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.reader_no_results), color = muted, fontSize = 14.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        )
                    }
                } else if (trimmed.length >= 2) {
                    item {
                        Text(
                            stringResource(R.string.reader_results_count, formatNumber(results.size, lang)),
                            color = muted, fontSize = 12.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        )
                    }
                    itemsIndexed(results) { _, r ->
                        Column(
                            Modifier.fillMaxWidth().clickable { onPick(r) }
                                .padding(horizontal = 4.dp, vertical = 12.dp),
                        ) {
                            Text(
                                buildAnnotatedString {
                                    append(r.before)
                                    withStyle(SpanStyle(background = Color(0xFFFFFF00), color = Color.Black)) { append(r.hit) }
                                    append(r.after)
                                },
                                color = ink, fontFamily = BrandFonts.ArabicText, fontSize = 16.sp, lineHeight = 1.7.em,
                            )
                            r.page?.let {
                                Text(
                                    stringResource(R.string.reader_page_n, formatNumber(book.printed(it), lang)),
                                    color = muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(HogwartsTheme.colors.border))
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().background(surface).navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Box(
                    Modifier.weight(1f).height(48.dp).shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.06f))
                        .clip(CircleShape).background(capsule),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(
                        ReaderIcons.SearchBold, null, tint = ink,
                        modifier = Modifier.padding(start = 18.dp).size(19.dp),
                    )
                    BasicTextField(
                        value = query,
                        onValueChange = onQuery,
                        singleLine = true,
                        textStyle = TextStyle(color = ink, fontSize = 17.sp, fontFamily = bookUi),
                        cursorBrush = SolidColor(ink),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { results.firstOrNull()?.let(onPick) }),
                        modifier = Modifier.fillMaxWidth().padding(start = 48.dp, end = 44.dp).focusRequester(focus),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text(stringResource(R.string.reader_search_in_this_book), color = placeholder, fontSize = 17.sp)
                            }
                            inner()
                        },
                    )
                    if (canDictate) {
                        Box(
                            Modifier.align(Alignment.CenterEnd).padding(end = 11.dp).size(36.dp).clip(CircleShape)
                                .clickable(role = Role.Button) { runCatching { dictation.launch(speechIntent) } },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(ReaderIcons.Mic, stringResource(R.string.reader_dictate), tint = ink, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Box(
                    Modifier.size(48.dp).shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.06f))
                        .clip(CircleShape).background(capsule)
                        .clickable(role = Role.Button) { if (query.isNotEmpty()) onQuery("") else onClose() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(ReaderIcons.X, stringResource(R.string.reader_clear), tint = ink, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

// ── Themes & Settings ──────────────────────────────────────────────────

/**
 * Themes & Settings — a card inset 10 dp from the screen's edges: a band
 * with the title, the size and light/dark capsules and the brightness
 * slider, over a body of six theme cards and the Customize button.
 */
@Composable
internal fun SettingsSheet(
    prefs: ReaderPrefs,
    lang: String,
    onPrefs: ((ReaderPrefs) -> ReaderPrefs) -> Unit,
    onCustomize: () -> Unit,
    onClose: () -> Unit,
) {
    val c = HogwartsTheme.colors
    val fg = c.foreground
    val ar = lang == "ar"
    ReaderSheet(onClose, Color.Transparent, RoundedCornerShape(0.dp)) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(10.dp)
                .clip(RoundedCornerShape(14.dp)),
        ) {
            Column(Modifier.fillMaxWidth().background(c.background.copy(alpha = 0.97f)).padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 16.dp)) {
                BandHead(stringResource(R.string.reader_themes_settings), onClose)
                Row(Modifier.fillMaxWidth().padding(top = 11.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Capsule(Modifier.weight(19f)) {
                        CapsuleButton(stringResource(R.string.reader_smaller), prefs.scaleIdx > 0, Modifier.weight(1f), {
                            onPrefs { it.copy(scaleIdx = (it.scaleIdx - 1).coerceAtLeast(0)) }
                        }) {
                            Text(if (ar) stringResource(R.string.reader_size_smaller) else "A", color = fg, fontSize = 16.sp, fontFamily = bookUi)
                        }
                        Box(Modifier.align(Alignment.CenterVertically).width(1.dp).height(36.dp).background(fg.copy(alpha = 0.16f)))
                        CapsuleButton(stringResource(R.string.reader_larger), prefs.scaleIdx < ReaderPrefs.SCALES.lastIndex, Modifier.weight(1f), {
                            onPrefs { it.copy(scaleIdx = (it.scaleIdx + 1).coerceAtMost(ReaderPrefs.SCALES.lastIndex)) }
                        }) {
                            Text(if (ar) stringResource(R.string.reader_size_larger) else "A", color = fg, fontSize = 23.sp, fontFamily = bookUi)
                        }
                    }
                    Capsule(Modifier.weight(13f)) {
                        CapsuleButton(stringResource(R.string.reader_light_mode), true, Modifier.weight(1f).alpha(if (!prefs.dark) 1f else 0.34f), {
                            onPrefs { it.copy(dark = false) }
                        }) { Icon(ReaderIcons.Sun, null, tint = fg, modifier = Modifier.size(22.dp)) }
                        CapsuleButton(stringResource(R.string.reader_dark_mode), true, Modifier.weight(1f).alpha(if (prefs.dark) 1f else 0.34f), {
                            onPrefs { it.copy(dark = true) }
                        }) { Icon(ReaderIcons.Moon, null, tint = fg, modifier = Modifier.size(22.dp)) }
                    }
                }
                SliderRow(
                    small = ReaderIcons.Sun, large = ReaderIcons.Sun,
                    fraction = (prefs.brightness - ReaderPrefs.BRIGHTNESS_MIN).toFloat() / (ReaderPrefs.BRIGHTNESS_MAX - ReaderPrefs.BRIGHTNESS_MIN),
                    steps = 0,
                    onFraction = { f ->
                        val v = ReaderPrefs.BRIGHTNESS_MIN + (f * (ReaderPrefs.BRIGHTNESS_MAX - ReaderPrefs.BRIGHTNESS_MIN)).roundToInt()
                        onPrefs { it.copy(brightness = v) }
                    },
                )
            }
            Column(
                Modifier.fillMaxWidth().background(fg.copy(alpha = 0.04f).compositeOver(c.background))
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 19.dp),
            ) {
                ReaderThemeName.entries.chunked(3).forEachIndexed { row, names ->
                    if (row > 0) Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        names.forEach { t -> ThemeCard(t, prefs.theme == t, ar, Modifier.weight(1f)) { onPrefs { p -> p.copy(theme = t) } } }
                    }
                }
                Row(
                    Modifier.padding(top = 19.dp).fillMaxWidth().height(52.dp).clip(CircleShape)
                        .background(fg.copy(alpha = 0.07f)).clickable(onClick = onCustomize),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    Icon(LucideIcons.Settings, null, tint = fg, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.reader_customize), color = fg, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, fontFamily = bookUi)
                }
            }
        }
    }
}

@Composable
private fun BandHead(title: String, onClose: () -> Unit) {
    val c = HogwartsTheme.colors
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title, color = c.foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em,
            fontFamily = bookUi, modifier = Modifier.weight(1f),
        )
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(c.foreground.copy(alpha = 0.06f))
                .clickable(role = Role.Button, onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(ReaderIcons.X, stringResource(R.string.reader_close), tint = c.mutedForeground, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun Capsule(modifier: Modifier, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier.height(48.dp).clip(CircleShape).background(HogwartsTheme.colors.foreground.copy(alpha = 0.09f)),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun CapsuleButton(label: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier.fillMaxHeight().clip(CircleShape).alpha(if (enabled) 1f else 0.3f)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun ThemeCard(theme: ReaderThemeName, selected: Boolean, ar: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val (bg, ink) = themeSwatch(theme)
    val weight = if (theme == ReaderThemeName.Bold) FontWeight.Bold else FontWeight.Normal
    val name = stringResource(
        when (theme) {
            ReaderThemeName.Original -> R.string.reader_theme_original
            ReaderThemeName.Quiet -> R.string.reader_theme_quiet
            ReaderThemeName.Paper -> R.string.reader_theme_paper
            ReaderThemeName.Bold -> R.string.reader_theme_bold
            ReaderThemeName.Calm -> R.string.reader_theme_calm
            ReaderThemeName.Focus -> R.string.reader_theme_focus
        },
    )
    Column(
        modifier.aspectRatio(1f)
            .then(if (selected) Modifier.border(2.5.dp, HogwartsTheme.colors.foreground, RoundedCornerShape(20.dp)) else Modifier)
            .clip(RoundedCornerShape(20.dp)).background(bg).clickable(onClick = onClick).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        if (ar) {
            Text(name, color = ink, fontSize = 22.sp, lineHeight = 1.3.em, fontWeight = weight, fontFamily = BrandFonts.ArabicText)
        } else {
            Text("Aa", color = ink, fontSize = 24.sp, lineHeight = 1.15.em, fontWeight = weight, fontFamily = BrandFonts.ArabicText)
            Text(name, color = ink, fontSize = 15.sp, lineHeight = 1.2.em, fontWeight = weight, fontFamily = bookUi)
        }
    }
}

/** `.book-slider`: a thin rail, the done part in ink, a 36×23 pill thumb. [steps] > 0 snaps. */
@Composable
private fun SliderRow(
    small: androidx.compose.ui.graphics.vector.ImageVector,
    large: androidx.compose.ui.graphics.vector.ImageVector,
    fraction: Float,
    steps: Int,
    onFraction: (Float) -> Unit,
) {
    val c = HogwartsTheme.colors
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    fun snap(f: Float) = if (steps > 0) (f * steps).roundToInt().toFloat() / steps else f
    Row(
        Modifier.fillMaxWidth().padding(top = 15.dp).height(23.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(small, null, tint = c.foreground, modifier = Modifier.size(14.dp))
        BoxWithConstraints(
            Modifier.weight(1f).fillMaxHeight()
                .pointerInputSlider(rtl, ::snap, onFraction),
            contentAlignment = Alignment.CenterStart,
        ) {
            val f = fraction.coerceIn(0f, 1f)
            val thumbW = 36.dp
            Box(Modifier.fillMaxWidth().height(5.5.dp).clip(CircleShape).background(c.foreground.copy(alpha = 0.14f)))
            Box(Modifier.fillMaxWidth(f).height(5.5.dp).clip(CircleShape).background(c.foreground))
            Box(
                Modifier.offset(x = (maxWidth - thumbW) * f).width(thumbW).height(23.dp)
                    .shadow(2.dp, CircleShape).clip(CircleShape)
                    .background(c.foreground.copy(alpha = 0.09f).compositeOver(c.background)),
            )
        }
        Icon(large, null, tint = c.foreground, modifier = Modifier.size(20.dp))
    }
}

private fun Modifier.pointerInputSlider(rtl: Boolean, snap: (Float) -> Float, onFraction: (Float) -> Unit): Modifier =
    this.then(
        Modifier.pointerInput(rtl) {
            fun at(x: Float) = snap((((if (rtl) size.width - x else x) / size.width)).coerceIn(0f, 1f))
            detectTapGestures { onFraction(at(it.x)) }
        }.pointerInput(rtl) {
            fun at(x: Float) = snap((((if (rtl) size.width - x else x) / size.width)).coerceIn(0f, 1f))
            detectDragGestures { change, _ ->
                change.consume()
                onFraction(at(change.position.x))
            }
        },
    )

// ── Customize ──────────────────────────────────────────────────────────

/** Customize: the face (Thmanyah or Rubik, each set in itself) and the line spacing. */
@Composable
internal fun CustomizeSheet(
    prefs: ReaderPrefs,
    maxHeight: Dp,
    onPrefs: ((ReaderPrefs) -> ReaderPrefs) -> Unit,
    onClose: () -> Unit,
) {
    val c = HogwartsTheme.colors
    ReaderSheet(onClose, c.background, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)) {
        Column(Modifier.fillMaxWidth().height(maxHeight * 0.94f)) {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    stringResource(R.string.reader_customize), color = c.foreground, fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Box(
                    Modifier.align(Alignment.TopEnd).size(36.dp).clip(CircleShape).background(c.muted)
                        .clickable(role = Role.Button, onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Icon(ReaderIcons.X, stringResource(R.string.reader_close), tint = c.foreground, modifier = Modifier.size(20.dp)) }
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().background(c.foreground.copy(alpha = 0.04f).compositeOver(c.background))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                SheetHeading(stringResource(R.string.reader_font))
                Row(
                    Modifier.padding(top = 19.dp).fillMaxWidth().clip(CircleShape)
                        .background(c.foreground.copy(alpha = 0.07f)).padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf(false to R.string.reader_font_serif, true to R.string.reader_font_sans).forEach { (sans, label) ->
                        val on = prefs.sans == sans
                        Box(
                            Modifier.weight(1f).height(42.dp)
                                .then(if (on) Modifier.shadow(1.dp, CircleShape) else Modifier)
                                .clip(CircleShape).background(if (on) c.background else Color.Transparent)
                                .clickable { onPrefs { it.copy(sans = sans) } },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(label), color = c.foreground, fontSize = 17.sp,
                                fontFamily = if (sans) BrandFonts.LatinText else BrandFonts.ArabicText,
                            )
                        }
                    }
                }
                SheetHeading(stringResource(R.string.reader_line_spacing))
                SliderRow(
                    small = ReaderIcons.AlignJustify, large = ReaderIcons.AlignJustify,
                    fraction = prefs.leadingIdx / 2f, steps = 2,
                    onFraction = { f -> onPrefs { it.copy(leadingIdx = (f * 2).roundToInt().coerceIn(0, 2)) } },
                )
            }
        }
    }
}

@Composable
private fun SheetHeading(text: String) {
    Text(
        text, color = HogwartsTheme.colors.mutedForeground, fontSize = 12.8.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.06.em, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

