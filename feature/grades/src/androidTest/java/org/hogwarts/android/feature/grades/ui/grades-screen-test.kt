package org.hogwarts.android.feature.grades.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.grades.domain.model.AssessmentType
import org.hogwarts.android.feature.grades.domain.model.GradeRecord
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GradesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleGrades = listOf(
        GradeRecord(
            id = "grade-1",
            studentId = "student-1",
            subjectId = "subj-1",
            subjectName = "Defense Against the Dark Arts",
            assessmentType = AssessmentType.EXAM,
            assessmentName = "Patronus Exam",
            score = 95f,
            maxScore = 100f,
            grade = "A",
            date = LocalDate.of(2025, 1, 20),
            term = "Fall 2025"
        ),
        GradeRecord(
            id = "grade-2",
            studentId = "student-1",
            subjectId = "subj-2",
            subjectName = "Potions",
            assessmentType = AssessmentType.QUIZ,
            assessmentName = "Veritaserum Quiz",
            score = 88f,
            maxScore = 100f,
            grade = "B+",
            date = LocalDate.of(2025, 1, 18),
            term = "Fall 2025"
        )
    )

    @Test
    fun gradesScreen_displaysFilterChips() {
        composeTestRule.setContent {
            HogwartsTheme {
                GradesContent(
                    uiState = GradesUiState(
                        isLoading = false,
                        records = sampleGrades
                    ),
                    onFilterChanged = {},
                    onRetry = {},
                    getFilteredRecords = { sampleGrades }
                )
            }
        }

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quiz").assertIsDisplayed()
    }

    @Test
    fun gradesScreen_displaysGradeRecords() {
        composeTestRule.setContent {
            HogwartsTheme {
                GradesContent(
                    uiState = GradesUiState(
                        isLoading = false,
                        records = sampleGrades
                    ),
                    onFilterChanged = {},
                    onRetry = {},
                    getFilteredRecords = { sampleGrades }
                )
            }
        }

        composeTestRule.onNodeWithText("Patronus Exam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Veritaserum Quiz").assertIsDisplayed()
    }

    @Test
    fun gradesScreen_showsEmptyStateWhenNoRecords() {
        composeTestRule.setContent {
            HogwartsTheme {
                GradesContent(
                    uiState = GradesUiState(
                        isLoading = false,
                        records = emptyList()
                    ),
                    onFilterChanged = {},
                    onRetry = {},
                    getFilteredRecords = { emptyList() }
                )
            }
        }

        composeTestRule.onNodeWithText("No grade records").assertIsDisplayed()
    }
}
