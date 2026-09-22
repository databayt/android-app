package org.hogwarts.android.feature.subjects.textbook.data

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.subjects.textbook.domain.TextbookLoad
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The book behind `/subjects/{slug}/textbook`. A missing book (404) or a
 * failed load lands on the web's fallback — the message and the PDF — never
 * on an error screen, as the web page does.
 */
@Singleton
class TextbookRepository @Inject constructor(
    private val api: TextbookApi,
    private val tenantContext: TenantContext,
) {
    private val cache = mutableMapOf<String, TextbookLoad.Ready>()

    suspend fun load(slug: String, pdfUrl: String?): TextbookLoad {
        tenantContext.requireSchoolId()
        val lang = Locale.getDefault().language.takeIf { it == "en" } ?: "ar"
        val key = "$slug:$lang"
        cache[key]?.let { return it }
        val response = api.getTextbook(slug, lang)
        val body = response.body() ?: return TextbookLoad.Unavailable(pdfUrl)
        val load = body.toDomain()
        if (load is TextbookLoad.Ready) cache[key] = load
        return load
    }
}
