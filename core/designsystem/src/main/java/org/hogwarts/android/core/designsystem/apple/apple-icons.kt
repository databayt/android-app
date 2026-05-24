package org.hogwarts.android.core.designsystem.apple

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning

import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh

/**
 * Hogwarts icon factory mapping SF Symbols to Material Icons.
 * Mirrors iOS `AppleSymbols` from swift-app design system.
 */
object HogwartsIcons {
    // Navigation
    val Home = Icons.Filled.Home
    val Dashboard = Icons.Filled.Dashboard
    val Settings = Icons.Filled.Settings
    val Profile = Icons.Filled.Person
    val Back = Icons.AutoMirrored.Filled.ArrowBack
    val Forward = Icons.AutoMirrored.Filled.ArrowForward
    val Menu = Icons.Filled.Menu
    val Logout = Icons.AutoMirrored.Filled.Logout

    // Actions
    val Add = Icons.Filled.Add
    val Close = Icons.Filled.Close
    val Search = Icons.Filled.Search
    val Filter = Icons.Filled.FilterList
    val Sort = Icons.AutoMirrored.Filled.Sort
    val Share = Icons.Filled.Share
    val Send = Icons.AutoMirrored.Filled.Send
    val Delete = Icons.Filled.Delete
    val Edit = Icons.Filled.Edit

    // Status
    val Checkmark = Icons.Filled.CheckCircle
    val ErrorIcon = Icons.Filled.Error
    val WarningIcon = Icons.Filled.Warning
    val InfoIcon = Icons.Filled.Info
    val Star = Icons.Filled.Star

    // School domain
    val Attendance = Icons.Filled.CheckCircle
    val Grades = Icons.Filled.BarChart
    val Schedule = Icons.Filled.CalendarMonth
    val Timetable = Icons.Filled.Schedule
    val Classes = Icons.Filled.Book
    val Students = Icons.Filled.People
    val Messages = Icons.AutoMirrored.Filled.Chat
    val Notifications = Icons.Filled.Notifications
    val Fees = Icons.Filled.Payments
    val Exams = Icons.Filled.Quiz
    val School = Icons.Filled.School

    // Extended aliases (mapped to available default icons)
    val Bookmark = Icons.Filled.Star
    val BookmarkFilled = Icons.Filled.Star
    val Check = Icons.Filled.CheckCircle
    val ChevronRight = Icons.AutoMirrored.Filled.ArrowForward
    val Document = Icons.Filled.Description
    val Download = Icons.Filled.KeyboardArrowDown
    val History = Icons.Filled.Refresh
    val Link = Icons.Filled.Share
    val Subjects = Icons.Filled.MenuBook
    val Video = Icons.Filled.PlayArrow
}
