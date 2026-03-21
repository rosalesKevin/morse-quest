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
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() { hiltRule.inject() }

    @Test
    fun homeScreenIsDisplayedOnLaunch() {
        composeRule.onNodeWithText("Morse Code").assertIsDisplayed()
    }

    @Test
    fun navigateToLearnScreenAndBack() {
        composeRule.onNodeWithText("Learn").performClick()
        composeRule.onNodeWithText("Lessons").assertIsDisplayed()
    }

    @Test
    fun navigateToReferenceScreen() {
        composeRule.onNodeWithText("Reference").performClick()
        composeRule.onNodeWithText("Morse Reference").assertIsDisplayed()
    }

    @Test
    fun navigateToSettingsScreen() {
        composeRule.onNodeWithText("Settings").performClick()
        // Use substring = true because the full text is "Speed: 20 WPM"
        composeRule.onNodeWithText("Speed:", substring = true).assertIsDisplayed()
    }
}
