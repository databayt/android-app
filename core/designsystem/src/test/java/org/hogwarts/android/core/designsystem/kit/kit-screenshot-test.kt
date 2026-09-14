package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the phone kit the way the web's phone pages compose it, in both
 * directions and themes. Record with `./gradlew :core:designsystem:recordRoborazziDebug`,
 * verify with `verifyRoborazziDebug`. Goldens live in src/test/screenshots.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h1700dp-xxhdpi")
class KitScreenshotTest {

    @Test fun kit_en_light() = capture("kit_en_light", rtl = false, dark = false)
    @Test fun kit_ar_light() = capture("kit_ar_light", rtl = true, dark = false)
    @Test fun kit_ar_dark() = capture("kit_ar_dark", rtl = true, dark = true)

    private fun capture(name: String, rtl: Boolean, dark: Boolean) {
        captureRoboImage("src/test/screenshots/$name.png") {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HogwartsTheme(darkTheme = dark) { KitSample(rtl) }
            }
        }
    }
}

@Composable
private fun KitSample(ar: Boolean) {
    val t = { en: String, arText: String -> if (ar) arText else en }
    Column(
        Modifier
            .fillMaxWidth()
            .background(HogwartsTheme.colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        BrandBanner(
            eyebrow = t("Next up", "التالي"),
            headline = buildAnnotatedString {
                append(t("Mark ", "حضّر "))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(t("Grade 5 · A", "الصف الخامس · أ")) }
                append(t(" before 9:00", " قبل ٩:٠٠"))
            },
            actions = {
                BrandPill(t("Open", "افتح"), onClick = {})
                BrandPill(t("Later", "لاحقاً"), onClick = {}, ghost = true)
            },
        )
        Column {
            SectionHeader(t("Attendance", "الحضور"), linkLabel = t("See all", "عرض الكل"), onLinkClick = {})
            StatPanel(
                items = listOf(
                    StatItem("present", t("Present", "حاضر"), "412", tone = StatTone.Positive),
                    StatItem("absent", t("Absent", "غائب"), "18", tone = StatTone.Negative),
                    StatItem("late", t("Late", "متأخر"), "9", tone = StatTone.Warning),
                    StatItem("rate", t("Rate", "النسبة"), "94%", hint = t("of 439 students", "من ٤٣٩ طالباً")),
                ),
            )
        }
        AppTileGrid(
            items = listOf(
                AppTileItem("att", t("Attendance", "الحضور"), {}, art = TileArt.Attendance),
                AppTileItem("grades", t("Grades", "الدرجات"), {}, art = TileArt.Grades, badge = 3),
                AppTileItem("exams", t("Exams", "الاختبارات"), {}, art = TileArt.Exams),
                AppTileItem("wallet", t("Finance", "المالية"), {}, art = TileArt.Wallet),
                AppTileItem("warn", t("Early warning", "الإنذار المبكر"), {}, tint = TileTint.Orange),
            ),
        )
        PageNav(
            items = listOf(PageNavItem("o", t("Overview", "نظرة عامة")), PageNavItem("r", t("Records", "السجلات")), PageNavItem("e", t("Excuses", "الأعذار"), badge = 2)),
            selectedKey = "o",
            onSelect = {},
        )
        ListRows(
            rows = listOf(
                { ListRow(t("Mathematics", "الرياضيات"), art = { DateTile(t("SUN", "الأحد"), "14") }, description = t("Chapter 3 quiz", "اختبار الفصل الثالث"), meta = t("Room 12 · 8:00", "قاعة ١٢ · ٨:٠٠"), onClick = {}) },
                { ListRow(t("Science", "العلوم"), art = { TileFace(art = TileArt.Subject) }, description = t("Lab report due", "تقرير المختبر"), trailing = { PillButton(t("Open", "افتح"), {}, variant = PillVariant.Muted) }) },
            ),
        )
        InfoRows(
            heading = t("Information", "المعلومات"),
            rows = listOf(InfoRow(t("Class", "الصف"), t("Grade 5 · A", "الخامس · أ")), InfoRow(t("Teacher", "المعلم"), t("Ahmed Ali", "أحمد علي"))),
        )
        ItemGrid(count = 3) { i, m ->
            ItemCard(
                title = listOf(t("Term 1 tuition", "رسوم الفصل الأول"), t("Bus fee", "رسوم النقل"), t("Books", "الكتب"))[i],
                eyebrow = "INV-00${i + 1}",
                value = listOf("1,500 SDG", "300 SDG", "120 SDG")[i],
                meta = t("Due Sep 30", "يستحق ٣٠ سبتمبر"),
                modifier = m,
                onClick = {},
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillButton(t("Save", "حفظ"), {})
            PillButton(t("Cancel", "إلغاء"), {}, variant = PillVariant.Outline)
        }
    }
}
