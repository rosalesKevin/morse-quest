package morse.android.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.android.theme.LocalExtendedColors
import morse.android.theme.LocalSpacing
import morse.android.theme.MorseInlineTextStyle
import morse.core.MorseAlphabet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onNavigateToPractice: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LearnViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    when (val current = state) {
        is LearnViewModel.UiState.LessonList -> LessonListContent(
            items = current.lessons,
            onSelect = { viewModel.selectLesson(it) },
            onBack = onBack,
        )
        is LearnViewModel.UiState.LessonDetail -> LessonDetailContent(
            item = current.lessonItem,
            onPlayChar = { viewModel.playCharacter(it) },
            onStartPractice = { onNavigateToPractice(current.lessonItem.lesson.id) },
            onBack = { viewModel.back() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonListContent(
    items: List<LearnViewModel.LessonItem>,
    onSelect: (morse.practice.Lesson) -> Unit,
    onBack: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skill Tree") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ),
                            )
                            .padding(spacing.xl),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            Text(
                                text = "Progress through lessons in sequence. Mastery unlocks the next relay on the line.",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "Available lessons pulse forward. Locked lessons hold position until your timing is ready.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            itemsIndexed(items) { index, item ->
                LessonTreeRow(
                    index = index,
                    item = item,
                    onClick = { onSelect(item.lesson) },
                )
            }
        }
    }
}

@Composable
private fun LessonTreeRow(
    index: Int,
    item: LearnViewModel.LessonItem,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val alignEnd = index % 2 == 1

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clickable(enabled = item.isUnlocked, onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (alignEnd) {
                LessonSummaryCard(modifier = Modifier.weight(1f), item = item)
                LessonNode(item = item, lessonNumber = index + 1)
            } else {
                LessonNode(item = item, lessonNumber = index + 1)
                LessonSummaryCard(modifier = Modifier.weight(1f), item = item)
            }
        }
    }
}

@Composable
private fun LessonNode(
    item: LearnViewModel.LessonItem,
    lessonNumber: Int,
) {
    val extendedColors = LocalExtendedColors.current
    val colors = when (item.visualState) {
        LessonVisualState.Locked -> Triple(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.outline,
        )
        LessonVisualState.Available -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary,
        )
        LessonVisualState.InProgress -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        LessonVisualState.Mastered -> Triple(
            extendedColors.success,
            extendedColors.success,
            MaterialTheme.colorScheme.surface,
        )
    }

    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .size(64.dp)
                .border(BorderStroke(2.dp, colors.second), CircleShape),
            color = colors.first,
            shape = CircleShape,
        ) {
            Box(contentAlignment = Alignment.Center) {
                when (item.visualState) {
                    LessonVisualState.Locked -> Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked lesson $lessonNumber",
                        tint = colors.third,
                    )
                    LessonVisualState.Mastered -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mastered lesson $lessonNumber",
                        tint = colors.third,
                    )
                    else -> Text(
                        text = lessonNumber.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.third,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (item.visualState == LessonVisualState.InProgress) {
            LinearProgressIndicator(
                progress = { item.masteryPercent / 100f },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(52.dp)
                    .padding(bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun LessonSummaryCard(
    modifier: Modifier = Modifier,
    item: LearnViewModel.LessonItem,
) {
    val spacing = LocalSpacing.current
    val label = when (item.visualState) {
        LessonVisualState.Locked -> "Locked"
        LessonVisualState.Available -> "Ready"
        LessonVisualState.InProgress -> "In progress"
        LessonVisualState.Mastered -> "Mastered"
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(item.lesson.title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = item.lesson.characters.joinToString("  ") { it.toString() },
                style = MorseInlineTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (item.visualState == LessonVisualState.InProgress || item.visualState == LessonVisualState.Mastered) {
                Text(
                    text = "${item.masteryPercent}% best accuracy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonDetailContent(
    item: LearnViewModel.LessonItem,
    onPlayChar: (Char) -> Unit,
    onStartPractice: () -> Unit,
    onBack: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val headerColor = when (item.visualState) {
        LessonVisualState.Mastered -> LocalExtendedColors.current.successContainer
        LessonVisualState.InProgress -> MaterialTheme.colorScheme.primaryContainer
        LessonVisualState.Available -> MaterialTheme.colorScheme.surfaceVariant
        LessonVisualState.Locked -> MaterialTheme.colorScheme.surfaceContainer
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.lesson.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacing.lg, vertical = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = headerColor),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text("Introduced characters", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = item.lesson.characters.joinToString(" ") { it.toString() },
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Text(
                        text = item.lesson.characters.joinToString("  ") { MorseAlphabet.characters[it].orEmpty() },
                        style = MorseInlineTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (item.visualState != LessonVisualState.Locked) {
                        LinearProgressIndicator(
                            progress = { item.masteryPercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    item.lesson.characters.forEachIndexed { index, char ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.lg, vertical = spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                                Text(char.toString(), style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = MorseAlphabet.characters[char].orEmpty(),
                                    style = MorseInlineTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { onPlayChar(char) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play $char")
                            }
                        }
                        if (index != item.lesson.characters.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }

            if (item.isUnlocked) {
                Button(
                    onClick = onStartPractice,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Start Practice")
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(
                        text = "Complete the previous lesson thresholds to unlock this relay.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.lg),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.fillMaxHeight())
        }
    }
}
