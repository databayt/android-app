package org.hogwarts.android.feature.exams.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.exams.ui.landing.AdminLandingScreen
import org.hogwarts.android.feature.exams.ui.landing.GuardianLandingScreen
import org.hogwarts.android.feature.exams.ui.landing.StudentLandingScreen
import org.hogwarts.android.feature.exams.ui.landing.TeacherLandingScreen
import javax.inject.Inject

/** Which landing the signed-in role gets — the role switch at the top of `exams/content.tsx`. */
enum class ExamsLanding { Admin, Teacher, Student, Guardian }

internal fun landingFor(role: UserRole?): ExamsLanding = when (role) {
    UserRole.STUDENT -> ExamsLanding.Student
    UserRole.GUARDIAN -> ExamsLanding.Guardian
    UserRole.TEACHER -> ExamsLanding.Teacher
    else -> ExamsLanding.Admin
}

/** The signed-in role, read once for the section's pages. */
@HiltViewModel
class ExamsRoleViewModel @Inject constructor(tenantContext: TenantContext) : ViewModel() {
    val role: UserRole? = tenantContext.userRole
}

/**
 * `/exams` on a phone: the role's landing under the section's tabs.
 * [onNavigate] receives this module's routes; [onOpenHref] gets every other
 * web path of the section, for the shell to open.
 */
@Composable
fun ExamsScreen(
    onNavigate: (Any) -> Unit,
    onOpenHref: (String) -> Unit,
    roleViewModel: ExamsRoleViewModel = hiltViewModel(),
) {
    val role = roleViewModel.role
    val links = remember(role, onNavigate, onOpenHref) { ExamsLinks(role, onNavigate, onOpenHref) }
    val tabs: @Composable () -> Unit = { ExamsTabs(role, ExamsTab.Overview, links) }
    when (landingFor(role)) {
        ExamsLanding.Admin -> AdminLandingScreen(tabs = tabs, links = links, onNavigate = onNavigate)
        ExamsLanding.Teacher -> TeacherLandingScreen(tabs = tabs, links = links)
        ExamsLanding.Student -> StudentLandingScreen(tabs = tabs, links = links)
        ExamsLanding.Guardian -> GuardianLandingScreen(tabs = tabs, links = links)
    }
}
