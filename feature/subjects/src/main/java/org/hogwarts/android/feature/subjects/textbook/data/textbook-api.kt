package org.hogwarts.android.feature.subjects.textbook.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TextbookApi {
    @GET("api/mobile/textbooks/{slug}")
    suspend fun getTextbook(
        @Path("slug") slug: String,
        @Query("lang") lang: String,
    ): Response<TextbookDto>
}
