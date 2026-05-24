package org.hogwarts.android.feature.attendance.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.attendance.domain.model.AttendanceRecord
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStatus
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class AttendanceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleRecords = listOf(
        AttendanceRecord(
            id = "att-1",
            studentId = "student-1",
            studentName = "Harry Potter",
            classId = "class-1",
            className = "Defense Against the Dark Arts",
            date = LocalDate.of(2025, 1, 15),
            status = AttendanceStatus.PRESENT
        ),
        AttendanceRecord(
            id = "att-2",
            studentId = "student-1",
            studentName = "Harry Potter",
            classId = "class-2",
            className = "Potions",
            date = LocalDate.of(2025, 1, 15),
            status = AttendanceStatus.ABSENT,
            note = "Quidditch practice"
        )
    )

    @Test
    fun attendanceScreen_displaysFilterChips() {
        composeTestRule.setContent {
            HogwartsTheme {
                AttendanceContent(
                    uiState = AttendanceUiState(
                        isLoading = false,
                        records = sampleRecords
                    ),
                    onFilterChanged = {},
                    onRetry = {},
                    getFilteredRecords = { sampleRecords }
                )
            }
        }

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Present").assertIsDisplayed()
        composeTestRule.onNodeWithText("Absent").assertIsDisplayed()
    }

    @Test
    fun attendanceScreen_displaysAttendanceRecords() {
        composeTestRule.setContent {
            HogwartsTheme {
                AttendanceContent(
                    uiState = AttendanceUiState(
                        isLoading = false,
                        records = sampleRecords
                    ),
                    onFilterChanged = {},
                    onRetry = {},
                    getFilteredRecords = { sampleRecords }
                )
            }
        }

        composeTestRule.onNodeWithText("Defense Against the Dark Arts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Potions").assertIsDisplayed()
    }

    @Test
    fun attendanceScreen_showsEmptyStateWhenNoRecords() {
        composeTestRule.setContent {
            HogwartsTheme {
                AttendanceContent(
                    uiState = AttendanceUiState(
                        isLoading = false,
                        records = emptyList()
                    ),
                    onFilterChanged = {},
                    onRetry = {},
                    getFilteredRecords = { emptyList() }
                )
            }
        }

        composeTestRule.onNodeWithText("No attendance records").assertIsDisplayed()
    }

    @Test
    fun attendanceScreen_showsErrorState() {
        composeTestRule.setContent {
            HogwartsTheme {
                AttendanceContent(
                    uiState = AttendanceUiState(
                        isLoading = false,
                        error = "Network error"
                    ),
                    onFilterChanged = {},
                    onRetry = {},
                    getFilteredRecords = { emptyList() }
                )
            }
        }

        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }
}
