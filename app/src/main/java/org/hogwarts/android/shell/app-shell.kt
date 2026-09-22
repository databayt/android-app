package org.hogwarts.android.shell

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import org.hogwarts.android.core.designsystem.kit.ReportIssueOpener
import org.hogwarts.android.core.designsystem.kit.LocalReportIssue
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
import org.hogwarts.android.shell.search.Search
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
import org.hogwarts.android.feature.library.navigation.LibraryCatalog
import org.hogwarts.android.feature.live.navigation.LiveHome
import org.hogwarts.android.feature.lumos.navigation.LumosCatalog
import org.hogwarts.android.feature.lumos.navigation.LumosHome
import org.hogwarts.android.feature.subjects.navigation.SubjectDetail
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
    onLogout: () -> Unit,
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

    // "Report an issue": the report is filed against the web URL of the page
    // the screen mirrors, the same page a browser user would be reporting.
    val reportViewModel: ReportIssueViewModel = hiltViewModel()
    val report by reportViewModel.uiState.collectAsStateWithLifecycle()
    val reportOpener = remember(state.schoolDomain, lang) {
        ReportIssueOpener { path -> reportViewModel.open(WebHandoff.url(state.schoolDomain, lang, path).toString()) }
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
            CompositionLocalProvider(
                LocalHrefOpener provides hrefOpener,
                LocalReportIssue provides reportOpener,
            ) {
                // `backdrop-blur` on the web's popover. Compose has no
                // backdrop filter, so the page behind is what blurs — which
                // is the same picture. The header stays sharp, as it does on
                // the web, because the popover opens below it.
                content(Modifier.weight(1f).blur(if (state.menuOpen) 8.dp else 0.dp))
            }
        }

        if (report.pageUrl != null) {
            val configuration = LocalConfiguration.current
            val sentWithId = stringResource(R.string.report_sent_body_with_id)
            val sentPlain = stringResource(R.string.report_sent_body)
            ReportIssueSheet(
                state = report,
                onDescription = reportViewModel::onDescription,
                onSend = {
                    reportViewModel.send(
                        viewport = "${configuration.screenWidthDp}x${configuration.screenHeightDp}",
                        rtl = lang == "ar",
                        sentBody = { id -> if (id != null) sentWithId.format(id.toString()) else sentPlain },
                    )
                },
                onClose = reportViewModel::close,
            )
        }
        report.sentBody?.let { body -> ReportSentAlert(body = body, onDismiss = reportViewModel::dismissSent) }

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
                // bell, mail, avatar.
                controls = listOf(
                    MenuControl("search", ToolbarIcons.Search, stringResource(R.string.search_title), onClick = {
                        viewModel.setMenuOpen(false)
                        navController.navigate(Search) { launchSingleTop = true }
                    }),
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
                    // `UserButton variant="platform"` — the avatar opens the
                    // account menu; it is not a link to the profile.
                    MenuControl(
                        "account",
                        description = stringResource(R.string.menu_account),
                        content = {
                            AccountControl(
                                userName = state.userName,
                                role = state.role,
                                onOpenHref = { href ->
                                    viewModel.setMenuOpen(false)
                                    hrefOpener.open(href)
                                },
                                onLogout = {
                                    viewModel.setMenuOpen(false)
                                    onLogout()
                                },
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
        // Subjects dropped its own TopAppBar when it was rebuilt on the web's
        // layout, so the platform header is now its chrome — the P3 migration
        // the note above describes.
        destination.hasRoute<Subjects>() ||
        // The subject page too: on the web it sits under the same header,
        // with no back bar of its own — system back returns to the list.
        destination.hasRoute<SubjectDetail>() ||
        destination.hasRoute<LibraryCatalog>() ||
        destination.hasRoute<LumosHome>() ||
        destination.hasRoute<LumosCatalog>() ||
        destination.hasRoute<LiveHome>() ||
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
    // The library module has existed since its wave; until its endpoints
    // landed it had nothing to show, so the menu handed /library to the
    // browser. It answers now.
    "library" -> LibraryCatalog
    // Same story as the library: 45 files of course player that the menu sent
    // to the browser because its endpoints were not there yet.
    "lumos" -> LumosHome
    // New module. The web block is ten thousand lines; this is its landing —
    // what is on now, what was, and the way into a room.
    "liveClasses" -> LiveHome
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
