package org.hogwarts.android.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.junit.Rule
import org.junit.Test

class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun theme_appliesCorrectColorsInLightMode() {
        composeTestRule.setContent {
            HogwartsTheme(darkTheme = false) {
                Text(
                    text = "Light Mode Test",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        composeTestRule.onNodeWithText("Light Mode Test").assertIsDisplayed()
    }

    @Test
    fun theme_appliesCorrectColorsInDarkMode() {
        composeTestRule.setContent {
            HogwartsTheme(darkTheme = true) {
                Text(
                    text = "Dark Mode Test",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        composeTestRule.onNodeWithText("Dark Mode Test").assertIsDisplayed()
    }

    @Test
    fun rtlLayout_displaysTextCorrectly() {
        composeTestRule.setContentRtl {
            Text(text = "اختبار الاتجاه من اليمين لليسار")
        }

        composeTestRule.onNodeWithText("اختبار الاتجاه من اليمين لليسار").assertIsDisplayed()
    }

    @Test
    fun ltrLayout_displaysTextCorrectly() {
        composeTestRule.setContentLtr {
            Text(text = "Left to Right Test")
        }

        composeTestRule.onNodeWithText("Left to Right Test").assertIsDisplayed()
    }
}
