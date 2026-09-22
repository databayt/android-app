package org.hogwarts.android.feature.library.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.library.R
import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.domain.model.BookPage
import org.hogwarts.android.feature.library.ui.components.BookJacket
import org.hogwarts.android.feature.library.ui.components.LibraryIcons
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The marketing green the web's book hero stands on, and the ink pinned on it (both fixed, never themed). */
private val GROUND = Color(0xFF00BC6D)
private val INK = Color(0xFF050505)

/**
 * `/library/books/[id]` — a book, read the way Apple Books reads one
 * (`book-detail/content.tsx`). The top is the green ground, edge to edge:
 * the jacket, the grade it is written for, the title, the author, the rating
 * and the card that borrows it. Below, the page turns ordinary: About, the
 * Information list, then two shelves of what to read next.
 */
@Composable
fun BookDetailScreen(
    @Suppress("UNUSED_PARAMETER") onNavigateBack: () -> Unit,
    onOpenBook: (String) -> Unit = {},
    onBrowse: (search: String?, gradeLevel: String?) -> Unit = { _, _ -> },
    onOpenUrl: (String) -> Unit = {},
    viewModel: BookDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val page = state.page
    Box(Modifier.fillMaxSize().background(HogwartsTheme.colors.background)) {
        when {
            page != null -> BookPageContent(
                page = page,
                working = state.isWorking,
                onBorrow = viewModel::borrow,
                onReturn = viewModel::giveBack,
                onOpenBook = onOpenBook,
                onBrowse = onBrowse,
                onOpenUrl = onOpenUrl,
            )
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.error != null -> Text(
                state.error.orEmpty(), color = HogwartsTheme.colors.mutedForeground,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
        }
        state.notice?.let { BorrowAlert(it, onDismiss = viewModel::dismissNotice) }
    }
}

@Composable
private fun BookPageContent(
    page: BookPage,
    working: Boolean,
    onBorrow: () -> Unit,
    onReturn: () -> Unit,
    onOpenBook: (String) -> Unit,
    onBrowse: (String?, String?) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 40.dp),
    ) {
        BookHero(page, working, onBorrow, onReturn, onBrowse, onOpenUrl)
        // Below the colour the page is ordinary again — one narrow column,
        // each section divided from the next by a rule rather than a gap.
        val sections = buildList<@Composable () -> Unit> {
            if (page.about.isNotEmpty()) add { BookAbout(page.about) }
            if (page.info.isNotEmpty()) add { BookInfoList(page.info) }
            if (page.moreByAuthor.isNotEmpty()) {
                add { BookShelfRow(stringResource(R.string.library_more_by) + " " + page.author, page.moreByAuthor, onOpenBook) }
            }
            if (page.similar.isNotEmpty()) add { BookShelfRow(stringResource(R.string.library_similar_books), page.similar, onOpenBook) }
        }
        Column(Modifier.fillMaxWidth().widthIn(max = 576.dp).padding(start = 24.dp, end = 24.dp, top = 32.dp)) {
            sections.forEachIndexed { i, section ->
                if (i > 0) {
                    Spacer(Modifier.height(32.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(HogwartsTheme.colors.border))
                    Spacer(Modifier.height(32.dp))
                }
                section()
            }
        }
    }
}

@Composable
private fun BookHero(
    page: BookPage,
    working: Boolean,
    onBorrow: () -> Unit,
    onReturn: () -> Unit,
    onBrowse: (String?, String?) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Column(
        Modifier.fillMaxWidth().background(GROUND).padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BookJacket(
            page.coverUrl, page.coverColor, page.title, page.author,
            modifier = Modifier.width(160.dp).height(240.dp)
                .shadow(18.dp, RoundedCornerShape(6.dp), ambientColor = INK.copy(alpha = 0.28f), spotColor = INK.copy(alpha = 0.28f)),
        )
        // The shelf this book belongs to — the grade it is written for. A link,
        // because it draws a chevron: it opens the listing on that grade.
        if (page.gradeLabel != null && page.gradeLevel != null) {
            Row(
                Modifier.padding(top = 24.dp).clickable(role = Role.Button) { onBrowse(null, page.gradeLevel) }
                    .drawBehind {
                        drawLine(INK.copy(alpha = 0.25f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                    }
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    page.gradeLabel.uppercase(), color = INK.copy(alpha = 0.8f), fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 0.12.em,
                )
                Chevron(14, rtl, INK.copy(alpha = 0.8f))
            }
        }
        Text(
            page.title, color = INK, fontSize = 30.sp, lineHeight = 1.25.em, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, style = androidx.compose.ui.text.TextStyle(lineBreak = LineBreak.Heading),
            modifier = Modifier.padding(top = 20.dp),
        )
        // The listing's search matches title or author, so this lands on the author's books.
        Row(
            Modifier.padding(top = 8.dp).clickable(role = Role.Button) { onBrowse(page.author, null) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(page.author, color = INK.copy(alpha = 0.85f), fontSize = 18.sp)
            Chevron(16, rtl, INK.copy(alpha = 0.85f))
        }
        // One star and the number; an unrated book shows its genre alone.
        Row(
            Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (page.rating > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(LibraryIcons.StarFilled, null, tint = INK, modifier = Modifier.size(16.dp))
                    Text(String.format(Locale.US, "%.1f", page.rating), color = INK.copy(alpha = 0.75f), fontSize = 15.sp)
                }
                Text("·", color = INK.copy(alpha = 0.75f), fontSize = 15.sp)
            }
            Text(page.genre, color = INK.copy(alpha = 0.75f), fontSize = 15.sp)
        }
        ActionCard(page, working, onBorrow, onReturn, onOpenUrl)
    }
}

@Composable
private fun Chevron(size: Int, rtl: Boolean, tint: Color) {
    Icon(
        LibraryIcons.ChevronRight, null, tint = tint,
        modifier = Modifier.size(size.dp).scale(scaleX = if (rtl) -1f else 1f, scaleY = 1f),
    )
}

/** The action card: what this is, when and how long, then the two things you can do. */
@Composable
private fun ActionCard(page: BookPage, working: Boolean, onBorrow: () -> Unit, onReturn: () -> Unit, onOpenUrl: (String) -> Unit) {
    val pagesWord = stringResource(R.string.library_pages_unit)
    val format = listOfNotNull(page.publicationYear?.toString(), page.pageCount?.let { "$it $pagesWord" })
    Column(
        Modifier.padding(top = 28.dp).fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(INK.copy(alpha = 0.10f)).padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.library_book_label), color = INK, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Icon(LibraryIcons.Info, null, tint = INK, modifier = Modifier.size(15.dp).alpha(0.6f))
        }
        if (format.isNotEmpty()) {
            Text(format.joinToString(" · "), color = INK.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val borrowed = page.borrowRecordId != null
            if (borrowed) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(LibraryIcons.Check, null, tint = INK.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.library_borrowed_this_book), color = INK.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Read: the digital copy. Disabled rather than dropped when there is none,
                // so the card keeps its shape on every book.
                Pill(
                    label = stringResource(R.string.library_read), primary = false,
                    enabled = page.digitalFileUrl != null, dim = page.digitalFileUrl == null,
                    icon = true, modifier = Modifier.weight(1f),
                ) { page.digitalFileUrl?.let(onOpenUrl) }
                when {
                    borrowed -> Pill(
                        stringResource(if (working) R.string.library_returning else R.string.library_return_short),
                        primary = true, enabled = !working, modifier = Modifier.weight(1f), onClick = onReturn,
                    )
                    page.availableCopies == 0 -> Pill(
                        stringResource(R.string.library_unavailable_pill), primary = false, enabled = false,
                        modifier = Modifier.weight(1f),
                    ) {}
                    else -> Pill(
                        stringResource(if (working) R.string.library_borrowing else R.string.library_borrow),
                        primary = true, enabled = !working, modifier = Modifier.weight(1f), onClick = onBorrow,
                    )
                }
            }
        }
    }
}

/** 56 dp tall and fully round — the reference's Sample/Get pair; pinned dark-on-light on the green. */
@Composable
private fun Pill(
    label: String,
    primary: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    dim: Boolean = false,
    icon: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier.height(56.dp).clip(CircleShape)
            .background(if (primary) Color.White else INK.copy(alpha = 0.12f))
            .alpha(if (dim) 0.45f else if (!enabled && primary) 0.5f else 1f)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        if (icon) Icon(LibraryIcons.BookOpen, null, tint = INK, modifier = Modifier.size(16.dp))
        Text(label, color = INK, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** "About This Book": the paragraphs as one block, clamped at three lines, More opening the rest in place. */
@Composable
private fun BookAbout(paragraphs: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    var clamped by remember { mutableStateOf(false) }
    val c = HogwartsTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.library_about_this_book), color = c.foreground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            paragraphs.joinToString("\n\n"), color = c.mutedForeground, fontSize = 16.sp, lineHeight = 1.625.em,
            maxLines = if (expanded) Int.MAX_VALUE else 3, overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) clamped = it.hasVisualOverflow },
        )
        if (clamped || expanded) {
            Text(
                stringResource(if (expanded) R.string.library_less else R.string.library_more),
                color = c.foreground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(role = Role.Button) { expanded = !expanded },
            )
        }
    }
}

/** The Information list: label and value, a hairline between each pair. */
@Composable
private fun BookInfoList(rows: List<Pair<String, String>>) {
    val c = HogwartsTheme.colors
    Column {
        Text(
            stringResource(R.string.library_information), color = c.foreground, fontSize = 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        rows.forEachIndexed { i, (label, value) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(label, color = c.mutedForeground, fontSize = 14.sp)
                Text(value, color = c.foreground, fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
            if (i < rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
        }
    }
}

/** A shelf: a heading with a chevron, then jackets running off the edge. */
@Composable
private fun BookShelfRow(heading: String, books: List<Book>, onOpenBook: (String) -> Unit) {
    val c = HogwartsTheme.colors
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(heading, color = c.foreground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Chevron(20, rtl, c.mutedForeground)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(books, key = { it.id }) { book ->
                Column(Modifier.width(112.dp).clickable(role = Role.Button) { onOpenBook(book.id) }) {
                    BookJacket(
                        book.coverImageUrl, book.coverColor, book.title, book.author,
                        modifier = Modifier.width(112.dp).height(168.dp).shadow(4.dp, RoundedCornerShape(6.dp)),
                        titleSize = 13.sp,
                    )
                    Text(
                        book.title, color = c.foreground, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(book.author, color = c.mutedForeground, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/**
 * `borrow-alert.tsx`: the system-style alert that answers a borrow or a
 * return — a glyph, a title and one sentence in a frosted 250-dp card,
 * gone on its own after five seconds.
 */
@Composable
private fun BorrowAlert(notice: BorrowNotice, onDismiss: () -> Unit) {
    val c = HogwartsTheme.colors
    LaunchedEffect(notice) {
        delay(5000)
        onDismiss()
    }
    val locale = Locale.getDefault()
    val (title, body, error) = when (notice) {
        is BorrowNotice.Borrowed -> Triple(
            stringResource(R.string.library_borrowed_title),
            stringResource(R.string.library_borrowed_body, notice.dueDate.format(DateTimeFormatter.ofPattern("d MMMM", localeFor(locale)))),
            false,
        )
        BorrowNotice.Returned -> Triple(stringResource(R.string.library_returned_title), stringResource(R.string.library_returned_body), false)
        is BorrowNotice.BorrowFailed -> Triple(stringResource(R.string.library_borrow_failed), stringResource(R.string.library_unexpected_error), true)
        is BorrowNotice.ReturnFailed -> Triple(stringResource(R.string.library_return_failed), stringResource(R.string.library_unexpected_error), true)
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.width(250.dp).shadow(24.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp))
                .background(c.muted.copy(alpha = 0.97f)).padding(horizontal = 24.dp, vertical = 41.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AlertGlyph(error, c.foreground.copy(alpha = 0.65f), c.muted)
            Text(title, color = c.foreground, fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 33.dp))
            Text(body, color = c.foreground, fontSize = 17.sp, lineHeight = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 11.dp))
        }
    }
}

/** The dates the web formats with `ar-EG`-style month names; Arabic reads its own. */
private fun localeFor(locale: Locale): Locale = if (locale.language == "ar") Locale.forLanguageTag("ar") else locale

@Composable
private fun AlertGlyph(error: Boolean, ink: Color, cut: Color) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    if (error) {
        Canvas(Modifier.size(57.dp)) {
            val s = size.width / 57f
            drawCircle(ink, 27f * s, Offset(28.5f * s, 28.5f * s))
            drawLine(cut, Offset(28.5f * s, 15f * s), Offset(28.5f * s, 32f * s), 4.5f * s, StrokeCap.Round)
            drawCircle(cut, 2.8f * s, Offset(28.5f * s, 41.5f * s))
        }
    } else {
        // A checked item over a list; the layout mirrors in Arabic, the tick never does.
        Canvas(Modifier.width(61.dp).height(57.dp).scale(scaleX = if (rtl) -1f else 1f, scaleY = 1f)) {
            val s = size.width / 61f
            drawCircle(ink, 15f * s, Offset(15f * s, 15f * s))
            val tick = androidx.compose.ui.graphics.Path().apply {
                val x = { v: Float -> if (rtl) (30f - v) * s else v * s }
                moveTo(x(8.5f), 15.5f * s); lineTo(x(13f), 20f * s); lineTo(x(22f), 11f * s)
            }
            drawPath(tick, cut, style = Stroke(3.4f * s, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            listOf(Triple(37.5f, 59.3f, 7.6f), Triple(37.5f, 59.3f, 21.9f), Triple(1.7f, 59.3f, 39.2f), Triple(1.7f, 59.3f, 54.4f)).forEach { (a, b, y) ->
                drawLine(ink, Offset(a * s, y * s), Offset(b * s, y * s), 3.4f * s, StrokeCap.Round)
            }
        }
    }
}
