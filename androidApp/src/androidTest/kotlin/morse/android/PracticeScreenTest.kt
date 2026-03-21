package morse.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PracticeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() { hiltRule.inject() }

    @Test
    fun navigateToPracticeAndSeeFirstExercise() {
        composeRule.onNodeWithText("Start Practicing").performClick()
        composeRule.onNodeWithText("Start session").performClick()
        composeRule.onNodeWithText("Submit").assertIsDisplayed()
    }

    @Test
    fun clearRemovesCapturedTapInput() {
        composeRule.onNodeWithText("Start Practicing").performClick()
        composeRule.onNodeWithText("Start session").performClick()
        composeRule.onNodeWithText("Submit").performClick()
        composeRule.onNodeWithText("Next").performClick()

        composeRule.onNodeWithText("No signal captured yet").assertIsDisplayed()
        composeRule.onNodeWithText("Tap or hold to send Morse").performTouchInput {
            down(center)
            advanceEventTime(50)
            up()
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Clear").performClick()

        composeRule.onNodeWithText("No signal captured yet").assertIsDisplayed()
    }
}
