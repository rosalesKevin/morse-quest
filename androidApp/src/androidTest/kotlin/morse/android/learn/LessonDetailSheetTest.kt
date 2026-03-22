package morse.android.learn

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import morse.android.theme.MorseTheme
import morse.practice.LessonCatalog
import org.junit.Rule
import org.junit.Test

class LessonDetailSheetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun startPracticeRemainsVisibleForLongReviewLessonContent() {
        val reviewLesson = LessonCatalog.defaultLessons()[5]
        val item = LearnViewModel.LessonItem(
            lesson = reviewLesson,
            visualState = LessonVisualState.Available,
            masteryPercent = 0,
        )
        val allLessons = LessonCatalog.defaultLessons().map {
            LearnViewModel.LessonItem(
                lesson = it,
                visualState = LessonVisualState.Available,
                masteryPercent = 0,
            )
        }

        composeRule.setContent {
            MorseTheme {
                Box(modifier = Modifier.height(320.dp)) {
                    LessonDetailSheetContent(
                        item = item,
                        allLessons = allLessons,
                        onPlayChar = {},
                        onStartPractice = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("Start Practice").assertIsDisplayed()
    }
}
