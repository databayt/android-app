package org.hogwarts.android.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.hogwarts.android.core.designsystem.R

/**
 * Brand font roles, resolved in one place so the licensed face drops in without
 * touching screens.
 *
 * The web uses Thmanyah for Arabic (licensed, not redistributable). Until a
 * mobile license lands the app ships open fonts: Noto Sans Arabic for RTL text
 * and Rubik for Latin text (the web's heading fallback).
 */
object BrandFonts {

    /** Arabic text (RTL): body and headings. */
    val ArabicText: FontFamily = FontFamily(
        Font(R.font.noto_sans_arabic_regular, FontWeight.Normal),
        Font(R.font.noto_sans_arabic_medium, FontWeight.Medium),
        Font(R.font.noto_sans_arabic_semibold, FontWeight.SemiBold),
        Font(R.font.noto_sans_arabic_bold, FontWeight.Bold)
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
