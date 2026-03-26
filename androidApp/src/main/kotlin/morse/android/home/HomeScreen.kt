package morse.android.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.android.practice.PracticeLaunchConfig
import morse.android.quest.DailyQuestViewModel
import morse.android.theme.LocalExtendedColors
import morse.android.theme.LocalSpacing
import morse.android.theme.MorseInlineTextStyle
import morse.android.theme.StatValueTextStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLearn: () -> Unit,
    onNavigateToPractice: (PracticeLaunchConfig) -> Unit,
    onNavigateToReference: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAudioDecode: () -> Unit,
    onNavigateToFreestyle: () -> Unit,
    onNavigateToDailyQuest: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "MORSE",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "QUEST",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = spacing.lg, vertical = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ),
                        )
                        .padding(spacing.xl),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                        Text(
                            text = "Lessons",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Learn Morse, one step at a time",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "Each lesson adds a new character. Go at your own pace.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LessonPathPanel(
                            currentPathLessonTitle = state.currentPathLessonTitle,
                            lastPracticedLessonTitle = state.lastPracticedLessonTitle,
                            nextLessonTitle = state.nextLessonTitle,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            Button(
                                onClick = {
                                    onNavigateToPractice(PracticeLaunchConfig.lesson(state.currentPathLessonId))
                                },
                                enabled = state.currentPathLessonId.isNotBlank(),
                            ) {
                                Text("Start Practicing")
                            }
                            OutlinedButton(onClick = onNavigateToLearn) {
                                Text("Browse Lessons")
                            }
                        }
                    }
                }
            }

            DailyQuestCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToDailyQuest,
                completedToday = state.dailyQuestCompleted,
            )

            FreestyleCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToFreestyle,
            )

            ReferenceCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToReference,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Current streak",
                    value = "${state.streakDays}",
                    supporting = "days active",
                    accent = extendedColors.rewardAmber,
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Overall accuracy",
                    value = "${"%.0f".format(state.overallAccuracy)}%",
                    supporting = "all sessions",
                    accent = extendedColors.success,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Lessons unlocked",
                    value = "${state.unlockedLessonCount}/${state.totalLessons}",
                    supporting = "progress map",
                    accent = MaterialTheme.colorScheme.primary,
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Best WPM",
                    value = state.bestWpm.toString(),
                    supporting = "personal best",
                    accent = MaterialTheme.colorScheme.primary,
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    Text("Progress summary", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Your recent results, best accuracy, and streak are collected here so you can gauge whether to keep reinforcing symbols or push the pace in your next session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        BestMetric(
                            modifier = Modifier.weight(1f),
                            label = "Accuracy",
                            value = "${"%.0f".format(state.bestAccuracy)}%",
                            accent = extendedColors.success,
                        )
                        BestMetric(
                            modifier = Modifier.weight(1f),
                            label = "Longest streak",
                            value = "${state.longestStreakDays}d",
                            accent = extendedColors.rewardAmber,
                        )
                    }
                }
            }

            if (state.focusCharacters.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = extendedColors.successContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        Text("Focus characters", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "These symbols have caused the most friction recently. Drill them before pushing speed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            state.focusCharacters.forEach { character ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = RoundedCornerShape(999.dp),
                                ) {
                                    Text(
                                        text = "$character  ${morse.core.MorseAlphabet.characters[character].orEmpty()}",
                                        style = MorseInlineTextStyle,
                                        modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ActionCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Audio Decode",
                copy = "Listen to a live signal source through the microphone and watch the decoded stream settle in real time.",
                cta = "Open Decoder",
                onClick = onNavigateToAudioDecode,
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    supporting: String,
    accent: Color,
) {
    Card(
        modifier = modifier.height(132.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LocalSpacing.current.lg),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
            Text(
                text = value,
                style = StatValueTextStyle,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BestMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = accent,
        )
        Text(text = value, style = StatValueTextStyle)
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    copy: String,
    cta: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LocalSpacing.current.lg),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.sm),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = copy,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(cta, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun FreestyleCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val extendedColors = LocalExtendedColors.current
    val spacing = LocalSpacing.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(extendedColors.rewardAmber),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.lg),
            ) {
                Text(
                    text = ". - . -",
                    style = MaterialTheme.typography.displayLarge,
                    color = extendedColors.rewardAmber.copy(alpha = 0.12f),
                    modifier = Modifier.align(Alignment.TopEnd),
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = "Freestyle",
                        style = MaterialTheme.typography.labelLarge,
                        color = extendedColors.rewardAmber,
                    )
                    Text(
                        text = "Tap freely, hear what you send",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "No lessons, no scoring. Just you and the key.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extendedColors.rewardAmber,
                            contentColor = Color(0xFF1C1C1E),
                        ),
                        modifier = Modifier.padding(top = spacing.xs),
                    ) {
                        Text("Open Freestyle ->", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.secondary),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.lg),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = spacing.xs),
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = "Reference",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = "The full Morse alphabet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Look up any letter or number and hear how it sounds.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                        modifier = Modifier.padding(top = spacing.xs),
                    ) {
                        Text("Open Reference ->", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonPathPanel(
    currentPathLessonTitle: String,
    lastPracticedLessonTitle: String,
    nextLessonTitle: String,
) {
    val spacing = LocalSpacing.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            if (currentPathLessonTitle.isNotBlank()) {
                Text(
                    text = currentPathLessonTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                LessonHint(
                    modifier = Modifier.weight(1f),
                    label = "Last practiced",
                    value = lastPracticedLessonTitle.ifBlank { "Not started yet" },
                )
                LessonHint(
                    modifier = Modifier.weight(1f),
                    label = "Next lesson",
                    value = nextLessonTitle.ifBlank { "All lessons unlocked" },
                )
            }
        }
    }
}

@Composable
private fun LessonHint(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.xxs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DailyQuestCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    completedToday: Boolean = false,
    questViewModel: DailyQuestViewModel = hiltViewModel(),
) {
    val extendedColors = LocalExtendedColors.current
    val spacing = LocalSpacing.current
    val questState by questViewModel.uiState.collectAsState()
    val quest = questState.quest
    val isCompleted = completedToday || questState.isCompleted
    val progressIndex = questState.progressIndex

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(extendedColors.exercisePurple),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.lg),
            ) {
                // Progress ring top-end
                if (quest != null) {
                    val total = quest.questions.size
                    val progress = if (isCompleted) 1f else progressIndex.toFloat() / total.toFloat()
                    val ringColor = extendedColors.exercisePurple
                    val trackColor = extendedColors.exercisePurple.copy(alpha = 0.15f)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.TopEnd)
                            .drawBehind {
                                val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                drawArc(
                                    color = trackColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = stroke,
                                    topLeft = Offset(stroke.width / 2, stroke.width / 2),
                                    size = Size(size.width - stroke.width, size.height - stroke.width),
                                )
                                drawArc(
                                    color = ringColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    style = stroke,
                                    topLeft = Offset(stroke.width / 2, stroke.width / 2),
                                    size = Size(size.width - stroke.width, size.height - stroke.width),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isCompleted) "✓" else "${(progress * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = ringColor,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = "Daily Quest",
                        style = MaterialTheme.typography.labelLarge,
                        color = extendedColors.exercisePurple,
                    )
                    Text(
                        text = "Today's Challenge",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (quest != null) {
                        // Character chips
                        val displayChars = quest.characters.take(10)
                        val overflow = quest.characters.size - displayChars.size
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            items(displayChars) { char ->
                                Surface(
                                    color = extendedColors.exercisePurple.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(999.dp),
                                ) {
                                    Text(
                                        text = char.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = extendedColors.exercisePurple,
                                        modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
                                    )
                                }
                            }
                            if (overflow > 0) {
                                item {
                                    Surface(
                                        color = extendedColors.exercisePurple.copy(alpha = 0.10f),
                                        shape = RoundedCornerShape(999.dp),
                                    ) {
                                        Text(
                                            text = "+$overflow",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = extendedColors.exercisePurple,
                                            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "~${quest.estimatedMinutes} min  •  ${quest.questions.size} questions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (quest.exerciseSummary.isNotEmpty()) {
                            Text(
                                text = quest.exerciseSummary.keys.joinToString("  ·  ") { kind ->
                                    kind.name.lowercase().replaceFirstChar { it.uppercase() }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (isCompleted) {
                        Surface(
                            color = extendedColors.success.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(999.dp),
                        ) {
                            Text(
                                text = "✓ Completed",
                                style = MaterialTheme.typography.labelLarge,
                                color = extendedColors.success,
                                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs),
                            )
                        }
                    } else {
                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extendedColors.exercisePurple,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier.padding(top = spacing.xs),
                        ) {
                            Text(
                                text = if (progressIndex > 0) "Resume Quest ->" else "Start Quest ->",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
