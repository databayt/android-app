package org.hogwarts.android.feature.library.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.domain.model.BookCategory
import org.hogwarts.android.feature.library.domain.model.BookPage
import org.hogwarts.android.feature.library.domain.model.CatalogPage
import org.hogwarts.android.feature.library.domain.model.LibraryHome

/** A catalog book as a shelf draws it — `toCatalogCardDto` on the server. */
@Serializable
data class CatalogCardDto(
    val id: String,
    val title: String,
    val author: String,
    val genre: String = "",
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("cover_color") val coverColor: String? = null,
    val rating: Double = 0.0,
    val description: String? = null,
) {
    fun toDomain() = Book(
        id = id,
        title = title,
        author = author,
        isbn = "",
        category = BookCategory.fromString(genre),
        genre = genre,
        description = description.orEmpty(),
        coverImageUrl = coverUrl,
        availableCopies = 0,
        totalCopies = 0,
        shelfLocation = "",
        sectionName = "",
        coverColor = coverColor,
        rating = rating,
    )
}

@Serializable
data class LibraryHomeDto(
    val total: Int = 0,
    val featured: CatalogCardDto? = null,
    val latest: List<CatalogCardDto> = emptyList(),
    @SerialName("featured_shelf") val featuredShelf: List<CatalogCardDto> = emptyList(),
    val literature: List<CatalogCardDto> = emptyList(),
    val science: List<CatalogCardDto> = emptyList(),
) {
    fun toDomain() = LibraryHome(
        total = total,
        featured = featured?.toDomain(),
        latest = latest.map { it.toDomain() },
        featuredShelf = featuredShelf.map { it.toDomain() },
        literature = literature.map { it.toDomain() },
        science = science.map { it.toDomain() },
    )
}

@Serializable
data class CatalogPageDto(
    val data: List<CatalogCardDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("total_pages") val totalPages: Int = 0,
    val genres: List<String> = emptyList(),
) {
    fun toDomain() = CatalogPage(data.map { it.toDomain() }, total, page, totalPages, genres)
}

@Serializable
data class InfoRowDto(val label: String, val value: String)

@Serializable
data class BookPageDto(
    val id: String,
    val title: String,
    val author: String,
    val genre: String = "",
    val rating: Double = 0.0,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("cover_color") val coverColor: String? = null,
    @SerialName("grade_label") val gradeLabel: String? = null,
    @SerialName("grade_level") val gradeLevel: String? = null,
    @SerialName("publication_year") val publicationYear: Int? = null,
    @SerialName("page_count") val pageCount: Int? = null,
    @SerialName("digital_file_url") val digitalFileUrl: String? = null,
    @SerialName("school_book_id") val schoolBookId: String,
    @SerialName("available_copies") val availableCopies: Int = 0,
    @SerialName("total_copies") val totalCopies: Int = 0,
    @SerialName("borrow_record_id") val borrowRecordId: String? = null,
    val about: List<String> = emptyList(),
    val info: List<InfoRowDto> = emptyList(),
    @SerialName("more_by_author") val moreByAuthor: List<CatalogCardDto> = emptyList(),
    val similar: List<CatalogCardDto> = emptyList(),
) {
    fun toDomain() = BookPage(
        id = id, title = title, author = author, genre = genre, rating = rating,
        coverUrl = coverUrl, coverColor = coverColor, gradeLabel = gradeLabel, gradeLevel = gradeLevel,
        publicationYear = publicationYear, pageCount = pageCount, digitalFileUrl = digitalFileUrl,
        schoolBookId = schoolBookId, availableCopies = availableCopies, totalCopies = totalCopies,
        borrowRecordId = borrowRecordId, about = about, info = info.map { it.label to it.value },
        moreByAuthor = moreByAuthor.map { it.toDomain() }, similar = similar.map { it.toDomain() },
    )
}
