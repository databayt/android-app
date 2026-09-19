package org.hogwarts.android.shell

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.core.designsystem.icon.ToolbarIcons
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
                // `backdrop-blur` on the web's popover. Compose has no
                // backdrop filter, so the page behind is what blurs — which
                // is the same picture. The header stays sharp, as it does on
                // the web, because the popover opens below it.
                content(Modifier.weight(1f).blur(if (state.menuOpen) 8.dp else 0.dp))
            }
        }

        if (showHeader) {
            val home = stringResource(R.string.menu_home)
            // The web's menu opens with two different places, not one:
            // "Home" is the school's own site root (`/ar`) and "Overview" is
            // the dashboard (`/ar/dashboard`). This used to send Home to the
            // dashboard and drop the overview link, which lost the site.
            val links = visibleNav(state.role, state.enabledModules)
                .map { item -> MenuLink(item.key, stringResource(item.titleRes), onClick = { go(item) }) }
            MenuPopover(
                visible = state.menuOpen,
                onDismiss = { viewModel.setMenuOpen(false) },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = HEADER_HEIGHT),
                // The web's toolbar, in its order: search, language, theme,
                // bell, mail, avatar. Its search opens a command palette the
                // app does not have, so that control is not here — five, not
                // six. Restore it alongside a search surface.
                controls = listOf(
                    MenuControl("language", ToolbarIcons.Languages, stringResource(R.string.menu_language), onClick = {
                        val next = if (lang == "ar") "en" else "ar"
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next))
                    }),
                    MenuControl(
                        "theme",
                        // One glyph in both themes, as `mode-switcher.tsx` draws it.
                        icon = ToolbarIcons.Contrast,
                        description = stringResource(R.string.menu_theme),
                        onClick = { viewModel.cycleTheme(isDark) },
                    ),
                    MenuControl("notifications", ToolbarIcons.Bell, stringResource(R.string.menu_notifications), onClick = {
                        viewModel.setMenuOpen(false)
                        navController.navigate(Notifications) { launchSingleTop = true }
                    }),
                    MenuControl("messages", ToolbarIcons.Mail, stringResource(R.string.menu_messages), onClick = {
                        viewModel.setMenuOpen(false)
                        navController.navigate(Messaging) { launchSingleTop = true }
                    }),
                    // `UserButton` renders the account control as the reader's
                    // own avatar, not a person glyph — their photo when there
                    // is one, their initials when there is not.
                    MenuControl(
                        "account",
                        description = stringResource(R.string.menu_account),
                        onClick = {
                            viewModel.setMenuOpen(false)
                            navController.navigate(Profile) { launchSingleTop = true }
                        },
                        content = {
                            UserAvatar(
                                name = state.userName.orEmpty(),
                                imageUrl = null,
                                size = 24.dp,
                                containerColor = HogwartsTheme.colors.primary,
                                contentColor = HogwartsTheme.colors.primaryForeground,
                            )
                        },
                    ),
                ),
                sections = listOf(
                    MenuSection(
                        title = stringResource(R.string.menu_title),
                        links = listOf(MenuLink("home", home, onClick = {
                            WebHandoff.open(context, WebHandoff.url(state.schoolDomain, lang, "/"))
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
