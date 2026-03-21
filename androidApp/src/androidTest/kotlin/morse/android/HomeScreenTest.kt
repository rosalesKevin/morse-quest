package morse.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() { hiltRule.inject() }

    @Test
    fun homeShowsStreakWidget() {
        composeRule.onNodeWithText("Current streak").assertIsDisplayed()
    }

    @Test
    fun homeShowsAccuracyWidget() {
        composeRule.onNodeWithText("Overall accuracy").assertIsDisplayed()
    }

    @Test
    fun homeShowsLessonProgress() {
        composeRule.onNodeWithText("Lessons unlocked").assertIsDisplayed()
    }

    @Test
    fun startPracticingOpensQuickStartSheet() {
        composeRule.onNodeWithText("Start Practicing").performClick()
        composeRule.onNodeWithText("Quick Start").assertIsDisplayed()
        composeRule.onNodeWithText("Start session").assertIsDisplayed()
    }
}
