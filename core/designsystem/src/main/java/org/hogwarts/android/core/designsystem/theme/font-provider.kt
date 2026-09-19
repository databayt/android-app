package org.hogwarts.android.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.hogwarts.android.core.designsystem.R

/**
 * Brand font roles, resolved in one place so screens never name a face.
 *
 * Arabic is set in **Thmanyah Serif Text** (خط ثمانية), the same face the web
 * dashboard uses — `hogwarts/src/app/globals.css` maps `--font-sans` to
 * `var(--font-thmanyah-text)` under `:root[dir="rtl"]`, and the subdomain
 * layout inherits it. Not the Sans and not the Serif Display: those are the
 * marketing voice.
 *
 * The license permits embedding in an app but forbids redistribution, so the
 * TTFs never enter this public repo. `scripts/fetch-thmanyah.mjs` puts them in
 * `core/designsystem/.thmanyah/` per machine and the module's `stageThmanyah`
 * task stages them into a generated resource dir; on a checkout that never ran
 * the script it stages Noto Sans Arabic under the same five names. So these
 * ids always resolve — what differs is which outlines ship.
 *
 * Latin stays on Rubik, the web's own Latin fallback.
 */
object BrandFonts {

    /**
     * Arabic text (RTL): body and headings — the five weights the web loads
     * (`fonts.ts` declares 300/400/500/700/900 of the serif text family).
     * SemiBold is not one of them on either side; Compose resolves a request
     * for it to the nearest face, as the browser does.
     */
    val ArabicText: FontFamily = FontFamily(
        Font(R.font.thmanyah_serif_text_300, FontWeight.Light),
        Font(R.font.thmanyah_serif_text_400, FontWeight.Normal),
        Font(R.font.thmanyah_serif_text_500, FontWeight.Medium),
        Font(R.font.thmanyah_serif_text_700, FontWeight.Bold),
        Font(R.font.thmanyah_serif_text_900, FontWeight.Black)
    )

    /** Latin text (LTR): body and headings. */
    val LatinText: FontFamily = FontFamily(
        Font(R.font.rubik_regular, FontWeight.Normal),
        Font(R.font.rubik_medium, FontWeight.Medium),
        Font(R.font.rubik_semibold, FontWeight.SemiBold),
        Font(R.font.rubik_bold, FontWeight.Bold)
    )

    fun forDirection(isRtl: Boolean): FontFamily = if (isRtl) ArabicText else LatinText
}
