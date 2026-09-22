package org.hogwarts.android.feature.subjects.textbook.engine

/**
 * The web reader's typography (`reader.css`), in pixels for one setting of
 * size and leading. The paginator measures with it and the page renders with
 * it, so what was counted is what is drawn.
 *
 * [base] is the flow's font size — `1.125rem × scale` — and every `em` below
 * is relative to the element's own font, as CSS resolves it.
 */
class ReaderMetrics(
    val base: Float,
    /** `--book-lh`: 1.6 · 1.9 · 2.2. */
    val leading: Float,
    /** One dp in px, for the few rules set in rem/px rather than em. */
    val dp: Float,
    val pageWidth: Float,
    val pageHeight: Float,
) {
    fun size(role: TextRole): Float = base * when (role) {
        TextRole.Body, TextRole.TocChapter -> 1f
        TextRole.Heading1 -> 1.3f
        TextRole.Heading2 -> 1.15f
        TextRole.Heading3 -> 1.05f
        TextRole.Folio -> 0.68f
        TextRole.Kicker -> 0.78f
        TextRole.ChapterTitle -> 1.9f
        TextRole.LessonTitle -> 1.4f
        TextRole.TableCell, TextRole.TableHead -> 0.9f
        TextRole.TocLesson -> 0.92f
    }

    fun lineHeight(role: TextRole): Float = size(role) * when (role) {
        TextRole.Heading1, TextRole.Heading2, TextRole.Heading3 -> 1.4f
        TextRole.ChapterTitle -> 1.3f
        TextRole.LessonTitle -> 1.35f
        TextRole.TableCell, TextRole.TableHead -> 1.5f
        TextRole.TocChapter, TextRole.TocLesson -> 1.6f
        else -> leading
    }

    fun heading(level: Int): TextRole = when {
        level <= 1 -> TextRole.Heading1
        level == 2 -> TextRole.Heading2
        else -> TextRole.Heading3
    }

    // ── Block boxes ────────────────────────────────────────────────────
    val paragraphIndent get() = 1.5f * base
    val paragraphAfter get() = 0.6f * base
    fun headingBefore(role: TextRole) = 1.2f * size(role)
    fun headingAfter(role: TextRole) = 0.5f * size(role)
    val listMargin get() = 0.6f * base
    val listItemGap get() = 0.25f * base
    val listIndent get() = 1.6f * base
    val ruleMargin get() = 1.2f * base
    val figureMargin get() = 0.75f * base

    /** A figure is a whole scanned page, fitted to the column and capped at the page less 3rem. */
    val figureHeight get() = minOf(pageWidth / PAGE_ASPECT, pageHeight - 48f * dp)
    val tableMargin get() = 0.8f * size(TextRole.TableCell)
    val tableMaxHeight get() = pageHeight - 32f * dp
    val cellPadV get() = 0.3f * size(TextRole.TableCell)
    val cellPadH get() = 0.55f * size(TextRole.TableCell)
    val folioAfter get() = 0.4f * size(TextRole.Folio)

    // ── Openers ────────────────────────────────────────────────────────
    fun openerTop(chapter: Boolean) = (if (chapter) 2.2f else 1.4f) * base
    fun openerBottom(chapter: Boolean) = (if (chapter) 0.8f else 0.4f) * base
    val kickerAfter get() = 0.5f * size(TextRole.Kicker)
    fun titleAfter(chapter: Boolean) =
        if (chapter) 0.5f * size(TextRole.ChapterTitle) else 0.4f * size(TextRole.LessonTitle)
    /** The rope ornament: 8.5em wide, drawn on a 200×20 box. */
    val ornamentWidth get() = 8.5f * base
    val ornamentHeight get() = ornamentWidth / 10f

    // ── Contents page ──────────────────────────────────────────────────
    val tocTop get() = 0.5f * base
    fun tocRowPadV(lesson: Boolean) = 0.3f * size(if (lesson) TextRole.TocLesson else TextRole.TocChapter)
    val tocLessonIndent get() = 2.2f * size(TextRole.TocLesson)
    /** Room a contents row keeps beside its title: padding, gaps, number, a leader's minimum and the folio. */
    fun tocTitleWidth(lesson: Boolean): Float {
        val em = size(if (lesson) TextRole.TocLesson else TextRole.TocChapter)
        val beside = if (lesson) 0.5f + 1.2f + 1f + 2.2f else 0.5f + 1.8f + 1.36f + 1f + 2.2f
        return pageWidth - beside * em - (if (lesson) tocLessonIndent else 0f)
    }

    val bodyLine get() = lineHeight(TextRole.Body)

    companion object {
        /** A scanned page, portrait A4. */
        const val PAGE_ASPECT = 0.707f
    }
}

enum class TextRole {
    Body, Heading1, Heading2, Heading3, Folio, Kicker, ChapterTitle, LessonTitle,
    TableCell, TableHead, TocChapter, TocLesson,
}
