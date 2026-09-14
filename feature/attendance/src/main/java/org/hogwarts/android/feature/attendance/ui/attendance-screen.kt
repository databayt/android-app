package org.hogwarts.android.feature.attendance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.PageNav
import org.hogwarts.android.core.designsystem.kit.PageNavItem
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.feature.attendance.navigation.AttendanceAnalytics
import org.hogwarts.android.feature.attendance.navigation.AttendanceGamification
import org.hogwarts.android.feature.attendance.navigation.AttendanceHallPass
import org.hogwarts.android.feature.attendance.navigation.AttendanceInterventions
import org.hogwarts.android.feature.attendance.navigation.AttendanceKiosk
import org.hogwarts.android.feature.attendance.navigation.AttendanceMethodSettings
import org.hogwarts.android.feature.attendance.ui.mine.MineScreen
import org.hogwarts.android.feature.attendance.ui.overview.StaffOverviewScreen
import org.hogwarts.android.feature.attendance.ui.quick.QuickAttendanceScreen
import javax.inject.Inject

/** Which landing the signed-in role gets — `attendance/page.tsx`. */
enum class AttendanceLanding { Quick, StaffOverview, Mine }

data class AttendanceUiState(val role: UserRole?) {
    val landing: AttendanceLanding
        get() = when (role) {
            UserRole.TEACHER -> AttendanceLanding.Quick
            UserRole.ADMIN, UserRole.STAFF, UserRole.DEVELOPER -> AttendanceLanding.StaffOverview
            else -> AttendanceLanding.Mine
        }
}

@HiltViewModel
class AttendanceViewModel @Inject constructor(tenantContext: TenantContext) : ViewModel() {
    private val _uiState = MutableStateFlow(AttendanceUiState(tenantContext.userRole))
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()
}

/** The section's sub-pages — `attendance/permissions.ts`, keeping only those with a native screen. */
enum class AttendanceTab { Overview, Interventions, Analytics, Settings }

internal fun tabsForRole(role: UserRole?): List<AttendanceTab> {
    val staff = role in setOf(UserRole.DEVELOPER, UserRole.ADMIN, UserRole.TEACHER, UserRole.STAFF)
    val view = staff || role == UserRole.STUDENT || role == UserRole.GUARDIAN
    if (!view) return emptyList()
    return buildList {
        add(AttendanceTab.Overview)
        // Web: Manual, QR Code (staff) / Records (student, guardian), Excuses — no native screen yet.
        if (staff) {
            // Web: Early Warning — no native screen yet.
            add(AttendanceTab.Interventions)
            add(AttendanceTab.Analytics)
            // Web: Reports — no native screen yet.
        }
        if (role?.isAdmin == true) add(AttendanceTab.Settings)
    }
}

/** Where a tab or quick-access tile leads. */
internal fun AttendanceTab.route(): Any? = when (this) {
    AttendanceTab.Overview -> null
    AttendanceTab.Interventions -> AttendanceInterventions
    AttendanceTab.Analytics -> AttendanceAnalytics
    AttendanceTab.Settings -> AttendanceMethodSettings
}

enum class AttendanceTile(val route: Any) { HallPass(AttendanceHallPass), Gamification(AttendanceGamification), Kiosk(AttendanceKiosk) }

/**
 * `/attendance` on a phone: the role's landing under the section's tabs.
 * The app shell draws the platform header above it, as the web layout does.
 */
@Composable
fun AttendanceScreen(
    onNavigate: (Any) -> Unit,
    onOpenMessages: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nav: @Composable () -> Unit = { AttendanceTabs(tabsForRole(state.role), onNavigate) }
    when (state.landing) {
        AttendanceLanding.Quick -> QuickAttendanceScreen(nav = nav, onOpenMessages = onOpenMessages)
        AttendanceLanding.StaffOverview -> StaffOverviewScreen(nav = nav, onNavigate = onNavigate)
        AttendanceLanding.Mine -> MineScreen(nav = nav)
    }
}

@Composable
internal fun AttendanceTabs(tabs: List<AttendanceTab>, onNavigate: (Any) -> Unit) {
    if (tabs.isEmpty()) return
    PageNav(
        items = tabs.map { PageNavItem(it.name, stringResource(it.labelRes())) },
        selectedKey = AttendanceTab.Overview.name,
        onSelect = { item -> AttendanceTab.valueOf(item.key).route()?.let(onNavigate) },
    )
}

private fun AttendanceTab.labelRes(): Int = when (this) {
    AttendanceTab.Overview -> R.string.attendance_tab_overview
    AttendanceTab.Interventions -> R.string.attendance_tab_interventions
    AttendanceTab.Analytics -> R.string.attendance_tab_analytics
    AttendanceTab.Settings -> R.string.attendance_tab_settings
}

/**
 * The page frame: 16dp gutters, the tabs, then the landing 24dp below
 * (`space-y-6` in `attendance/layout.tsx`). [bottomBar] floats over the
 * bottom edge like the web's fixed save bar.
 */
@Composable
internal fun AttendancePage(
    nav: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(HogwartsTheme.colors.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = if (bottomBar != null) 112.dp else 32.dp),
        ) {
            nav()
            Spacer(Modifier.height(24.dp))
            content()
        }
        if (bottomBar != null) {
            Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) { bottomBar() }
        }
    }
}
