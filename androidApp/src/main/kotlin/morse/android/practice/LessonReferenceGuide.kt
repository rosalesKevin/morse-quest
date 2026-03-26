package morse.android.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import morse.android.theme.LocalSpacing
import morse.android.theme.MorseInlineTextStyle
import morse.core.MorseAlphabet
import morse.practice.Lesson

internal data class LessonReferenceItem(
    val character: Char,
    val morse: String,
)

internal fun buildLessonReferenceItems(lesson: Lesson): List<LessonReferenceItem> =
    lesson.characters.distinct().map { character ->
        LessonReferenceItem(
            character = character,
            morse = MorseAlphabet.characters[character].orEmpty(),
        )
    }

@Composable
internal fun LessonReferenceGuide(
    lesson: Lesson,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val items = buildLessonReferenceItems(lesson)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                    Text("Lesson guide", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (expanded) {
                            "Keep the current lesson's characters in view while you practice."
                        } else {
                            "Hidden for now."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onToggleExpanded) {
                    Text(if (expanded) "Hide" else "Show")
                }
            }

            if (expanded) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    items(items) { item ->
                        LessonReferenceChip(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonReferenceChip(item: LessonReferenceItem) {
    val spacing = LocalSpacing.current
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val textColor = MaterialTheme.colorScheme.onSurface

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(
                text = item.character.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
            Text(
                text = item.morse,
                style = MorseInlineTextStyle,
                color = textColor,
            )
        }
    }
}
