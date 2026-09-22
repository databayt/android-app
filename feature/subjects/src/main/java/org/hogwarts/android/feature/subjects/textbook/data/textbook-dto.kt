package org.hogwarts.android.feature.subjects.textbook.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.hogwarts.android.feature.subjects.textbook.domain.Block
import org.hogwarts.android.feature.subjects.textbook.domain.Book
import org.hogwarts.android.feature.subjects.textbook.domain.BookCover
import org.hogwarts.android.feature.subjects.textbook.domain.BookPage
import org.hogwarts.android.feature.subjects.textbook.domain.BookSection
import org.hogwarts.android.feature.subjects.textbook.domain.Opener
import org.hogwarts.android.feature.subjects.textbook.domain.TextbookLoad
import org.hogwarts.android.feature.subjects.textbook.domain.TocChapter
import org.hogwarts.android.feature.subjects.textbook.domain.TocLesson

/** `GET /api/mobile/textbooks/{slug}` — see the route for the shape. */
@Serializable
data class TextbookDto(
    val status: String,
    val slug: String? = null,
    @SerialName("pdf_url") val pdfUrl: String? = null,
    @SerialName("asset_base_url") val assetBaseUrl: String? = null,
    val meta: MetaDto? = null,
    val cover: CoverDto? = null,
    val toc: List<TocChapterDto> = emptyList(),
    val sections: List<SectionDto> = emptyList(),
)

@Serializable
data class MetaDto(
    val title: String,
    val edition: String? = null,
    val lang: String = "ar",
    val dir: String = "rtl",
    val offset: Int? = null,
)

@Serializable
data class CoverDto(
    val url: String? = null,
    val stage: String? = null,
    @SerialName("grade_line") val gradeLine: String? = null,
)

@Serializable
data class TocChapterDto(
    val id: String,
    val name: String,
    val page: Int? = null,
    val lessons: List<TocLessonDto> = emptyList(),
)

@Serializable
data class TocLessonDto(val id: String, val name: String, val page: Int? = null)

@Serializable
data class SectionDto(
    val kind: String,
    val title: String,
    val kicker: String? = null,
    val pages: List<PageDto> = emptyList(),
)

@Serializable
data class PageDto(
    val number: Int? = null,
    val empty: Boolean = false,
    val opener: OpenerDto? = null,
    val blocks: List<BlockDto> = emptyList(),
)

@Serializable
data class OpenerDto(val kicker: String? = null, val title: String, val level: String = "chapter")

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed class BlockDto {
    @Serializable @SerialName("heading")
    data class Heading(val level: Int = 1, val text: String) : BlockDto()

    @Serializable @SerialName("paragraph")
    data class Paragraph(val text: String) : BlockDto()

    @Serializable @SerialName("list")
    data class ListBlock(val ordered: Boolean = false, val items: List<String> = emptyList()) : BlockDto()

    @Serializable @SerialName("table")
    data class Table(val rows: List<List<String>> = emptyList()) : BlockDto()

    @Serializable @SerialName("rule")
    data object Rule : BlockDto()

    @Serializable @SerialName("image")
    data class Image(val alt: String = "", val src: String) : BlockDto()
}

fun TextbookDto.toDomain(): TextbookLoad {
    if (status != "ok" || meta == null) {
        return if (status == "noText") TextbookLoad.NoText(pdfUrl) else TextbookLoad.Unavailable(pdfUrl)
    }
    return TextbookLoad.Ready(
        Book(
            slug = slug.orEmpty(),
            pdfUrl = pdfUrl.orEmpty(),
            assetBaseUrl = assetBaseUrl.orEmpty(),
            title = meta.title,
            edition = meta.edition,
            lang = meta.lang,
            rtl = meta.dir == "rtl",
            offset = meta.offset,
            cover = BookCover(cover?.url, cover?.stage, cover?.gradeLine),
            toc = toc.map { ch ->
                TocChapter(ch.id, ch.name, ch.page, ch.lessons.map { TocLesson(it.id, it.name, it.page) })
            },
            sections = sections.map { s ->
                BookSection(
                    kind = s.kind,
                    title = s.title,
                    kicker = s.kicker,
                    pages = s.pages.map { p ->
                        BookPage(
                            number = p.number,
                            empty = p.empty,
                            opener = p.opener?.let { Opener(it.kicker, it.title, it.level == "chapter") },
                            blocks = p.blocks.map { it.toDomain() },
                        )
                    },
                )
            },
        ),
    )
}

private fun BlockDto.toDomain(): Block = when (this) {
    is BlockDto.Heading -> Block.Heading(level, text)
    is BlockDto.Paragraph -> Block.Paragraph(text)
    is BlockDto.ListBlock -> Block.ListBlock(ordered, items)
    is BlockDto.Table -> Block.Table(rows)
    BlockDto.Rule -> Block.Rule
    is BlockDto.Image -> Block.Image(alt, src)
}
