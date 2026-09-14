package org.hogwarts.android.shell

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.hogwarts.android.R
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.MenuControl
import org.hogwarts.android.core.designsystem.kit.MenuLink
import org.hogwarts.android.core.designsystem.kit.MenuPopover
import org.hogwarts.android.core.designsystem.kit.MenuSection
import org.hogwarts.android.core.designsystem.kit.PlatformHeader
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.announcements.navigation.Announcements
import org.hogwarts.android.feature.announcements.navigation.AnnouncementDetail
import org.hogwarts.android.feature.attendance.navigation.Attendance
import org.hogwarts.android.feature.dashboard.navigation.Dashboard
import org.hogwarts.android.feature.events.navigation.EventsList
import org.hogwarts.android.feature.exams.navigation.ExamDetail
import org.hogwarts.android.feature.exams.navigation.Exams
import org.hogwarts.android.feature.exams.navigation.ExamsUpcoming
import org.hogwarts.android.feature.exams.navigation.QuestionBank
import org.hogwarts.android.feature.fees.navigation.Fees
import org.hogwarts.android.feature.grades.navigation.Grades
import org.hogwarts.android.feature.guardian.navigation.GuardianChildren
import org.hogwarts.android.feature.messaging.navigation.Messaging
import org.hogwarts.android.feature.notifications.navigation.NotificationPreferences
import org.hogwarts.android.feature.notifications.navigation.Notifications
import org.hogwarts.android.feature.notifications.navigation.NotificationsUnread
import org.hogwarts.android.feature.profile.navigation.Profile
import org.hogwarts.android.feature.settings.navigation.Settings
import org.hogwarts.android.feature.students.navigation.StudentsList
import org.hogwarts.android.feature.subjects.navigation.Subjects
import org.hogwarts.android.feature.timetable.navigation.Timetable

/**
 * The signed-in frame: the web's phone header + Menu over the app's screens.
 *
 * The header shows on destinations already rebuilt on the phone kit
 * ([isShellDestination]); screens still carrying their own top bar keep it
 * until their P3 wave lands, so no screen ever shows two headers.
 */
@Composable
fun AppShell(
    navController: NavHostController,
    content: @Composable (Modifier) -> Unit,
    viewModel: ShellViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val showHeader = destination != null && isShellDestination(destination)
    val context = LocalContext.current
    val lang = currentLocale().language.takeIf { it == "en" } ?: "ar"
    val isDark = HogwartsTheme.colors.isDark

    LaunchedEffect(destination) { viewModel.refreshSession() }

    val hrefOpener = remember(state.role, state.schoolDomain, lang, navController) {
        HrefOpener { href ->
            val route = routeForHref(href, state.role)
            if (route != null) {
                navController.navigate(route) { launchSingleTop = true }
            } else {
                WebHandoff.open(context, WebHandoff.url(state.schoolDomain, lang, href))
            }
        }
    }

    fun go(item: PlatformNavItem) {
        val route = nativeRoute(item.key, state.role)
        if (route != null) {
            navController.navigate(route) { launchSingleTop = true }
        } else {
            WebHandoff.open(context, WebHandoff.url(state.schoolDomain, lang, item.href))
        }
    }

    Box(Modifier.fillMaxSize().background(HogwartsTheme.colors.background)) {
        Column(Modifier.fillMaxSize()) {
            if (showHeader) {
                PlatformHeader(
                    menuLabel = stringResource(R.string.menu_title),
                    toggleDescription = stringResource(R.string.menu_toggle),
                    menuOpen = state.menuOpen,
                    onToggleMenu = { viewModel.setMenuOpen(!state.menuOpen) },
                    modifier = Modifier.statusBarsPadding(),
                )
            }
            CompositionLocalProvider(LocalHrefOpener provides hrefOpener) {
                content(Modifier.weight(1f))
            }
        }

        if (showHeader) {
            val home = stringResource(R.string.menu_home)
            val links = visibleNav(state.role, state.enabledModules)
                .filter { it.key != "dashboard" }
                .map { item -> MenuLink(item.key, stringResource(item.titleRes), onClick = { go(item) }) }
            MenuPopover(
                visible = state.menuOpen,
                onDismiss = { viewModel.setMenuOpen(false) },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = HEADER_HEIGHT),
                controls = listOf(
                    MenuControl("language", Icons.Outlined.Language, stringResource(R.string.menu_language), onClick = {
                        val next = if (lang == "ar") "en" else "ar"
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next))
                    }),
                    MenuControl(
                        "theme",
                        if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        stringResource(R.string.menu_theme),
                        onClick = { viewModel.cycleTheme(isDark) },
                    ),
                    MenuControl("notifications", Icons.Outlined.Notifications, stringResource(R.string.menu_notifications), onClick = {
                        viewModel.setMenuOpen(false)
                        navController.navigate(Notifications) { launchSingleTop = true }
                    }),
                    MenuControl("messages", Icons.Outlined.MailOutline, stringResource(R.string.menu_messages), onClick = {
                        viewModel.setMenuOpen(false)
                        navController.navigate(Messaging) { launchSingleTop = true }
                    }),
                    MenuControl("account", Icons.Outlined.Person, stringResource(R.string.menu_account), onClick = {
                        viewModel.setMenuOpen(false)
                        navController.navigate(Profile) { launchSingleTop = true }
                    }),
                ),
                sections = listOf(
                    MenuSection(
                        title = stringResource(R.string.menu_title),
                        links = listOf(MenuLink("home", home, onClick = {
                            navController.navigate(Dashboard) { popUpTo<Dashboard> { inclusive = false }; launchSingleTop = true }
                        })) + links,
                    ),
                ),
            )
        }
    }
}

private val HEADER_HEIGHT = 49.dp

private fun isShellDestination(destination: androidx.navigation.NavDestination): Boolean =
    destination.hasRoute<Dashboard>() ||
        destination.hasRoute<Attendance>() ||
        destination.hasRoute<Announcements>() ||
        destination.hasRoute<AnnouncementDetail>() ||
        destination.hasRoute<Settings>() ||
        destination.hasRoute<Notifications>() ||
        destination.hasRoute<NotificationsUnread>() ||
        destination.hasRoute<NotificationPreferences>() ||
        destination.hasRoute<Timetable>() ||
        destination.hasRoute<Fees>() ||
        destination.hasRoute<Exams>() ||
        destination.hasRoute<ExamsUpcoming>() ||
        destination.hasRoute<ExamDetail>() ||
        destination.hasRoute<QuestionBank>()

/**
 * Native screens for menu keys. A key without one (school configuration,
 * sales, compliance, transport, live, library until its API exists…) opens
 * the web page instead.
 */
private val NATIVE_TIMETABLE_ROLES = setOf(UserRole.STUDENT, UserRole.TEACHER, UserRole.GUARDIAN)

internal fun nativeRoute(key: String, role: UserRole?): Any? = when (key) {
    "dashboard" -> Dashboard
    "announcements" -> Announcements
    "finance" -> Fees
    "grades" -> Grades
    "subjects" -> Subjects
    "students" -> StudentsList
    "exams" -> Exams
    "events" -> EventsList
    "attendance" -> Attendance
    // Staff timetables need school-wide data the mobile API lacks; they stay on the web.
    "timetable" -> if (role in NATIVE_TIMETABLE_ROLES) Timetable else null
    "parentPortal" -> if (role == UserRole.GUARDIAN) GuardianChildren else null
    "profile" -> Profile
    "settings" -> Settings
    else -> null
}
