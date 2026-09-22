package org.hogwarts.android.feature.library.data.remote

import org.hogwarts.android.feature.library.data.remote.dto.BookDto
import org.hogwarts.android.feature.library.data.remote.dto.BookListResponse
import org.hogwarts.android.feature.library.data.remote.dto.BorrowingDto
import org.hogwarts.android.feature.library.data.remote.dto.BorrowingListResponse
import org.hogwarts.android.feature.library.data.remote.dto.BookPageDto
import org.hogwarts.android.feature.library.data.remote.dto.CatalogPageDto
import org.hogwarts.android.feature.library.data.remote.dto.LibraryHomeDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for library endpoints.
 *
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface LibraryApi {

    @GET("api/mobile/library/books")
    suspend fun getBooks(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): BookListResponse

    @GET("api/mobile/library/books/{id}")
    suspend fun getBookDetail(
        @Path("id") bookId: String
    ): BookDto

    @POST("api/mobile/library/books/{id}/borrow")
    suspend fun borrowBook(
        @Path("id") bookId: String
    ): BorrowingDto

    @POST("api/mobile/library/borrowings/{id}/renew")
    suspend fun renewBorrowing(
        @Path("id") borrowingId: String
    ): BorrowingDto

    @GET("api/mobile/library/my-borrowings")
    suspend fun getMyBorrowings(): BorrowingListResponse

    /** `/library` — the web page's own loader. */
    @GET("api/mobile/library/home")
    suspend fun getHome(@Query("lang") lang: String): LibraryHomeDto

    /** `/library/books` — search, genre and grade level, 20 a page. */
    @GET("api/mobile/library/catalog")
    suspend fun getCatalog(
        @Query("page") page: Int,
        @Query("search") search: String? = null,
        @Query("genre") genre: String? = null,
        @Query("grade_level") gradeLevel: String? = null,
    ): CatalogPageDto

    /** `/library/books/[id]` by catalog id. */
    @GET("api/mobile/library/catalog/{id}")
    suspend fun getBookPage(@Path("id") id: String, @Query("lang") lang: String): BookPageDto

    @POST("api/mobile/library/borrowings/{id}/return")
    suspend fun returnBorrowing(@Path("id") borrowingId: String): BorrowingDto
}
