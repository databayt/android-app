package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.R
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Opens "Report an issue" for the web page a screen mirrors — the path is the
 * page's locale-less href (`/subjects`, `/live`…), so a report is filed
 * against the page a browser user would name. Provided by the app shell;
 * without one (previews, tests) the link does nothing.
 */
fun interface ReportIssueOpener {
    fun open(pagePath: String)
}

val LocalReportIssue = staticCompositionLocalOf<ReportIssueOpener> { ReportIssueOpener { } }

/**
 * `report-issue-footer.tsx`: the underlined "Report an issue" at the foot of
 * every school page — 14sp, medium, muted, 32dp above it and 16dp below.
 */
@Composable
fun ReportIssueFooter(pagePath: String, modifier: Modifier = Modifier) {
    val opener = LocalReportIssue.current
    Text(
        text = stringResource(R.string.report_issue_trigger),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        textDecoration = TextDecoration.Underline,
        color = HogwartsTheme.colors.mutedForeground,
        modifier = modifier
            .padding(top = 32.dp, bottom = 16.dp)
            .clickable(role = Role.Button) { opener.open(pagePath) },
    )
}
