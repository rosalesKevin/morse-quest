package morse.android.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.android.practice.PracticeLaunchConfig
import morse.android.practice.QuickStartDifficulty
import morse.android.theme.LocalExtendedColors
import morse.android.theme.LocalSpacing
import morse.android.theme.MorseInlineTextStyle
import morse.android.theme.StatValueTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLearn: () -> Unit,
    onNavigateToPractice: (PracticeLaunchConfig) -> Unit,
    onNavigateToReference: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAudioDecode: () -> Unit,
    onNavigateToFreestyle: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current
    val showQuickStartSheet = remember { mutableStateOf(false) }
    val selectedDifficulty = remember { mutableStateOf(QuickStartDifficulty.EASY) }
    val selectedWpm = remember { mutableIntStateOf(state.quickStartDefaultWpm) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    TextButton(onClick = onNavigateToSettings) {
                        Text("Settings")
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
                        if (state.quickPracticeLessonId.isNotBlank() && state.quickPracticeLessonTitle.isNotBlank()) {
                            ContinueBanner(
                                lessonTitle = state.quickPracticeLessonTitle,
                                onClick = {
                                    onNavigateToPractice(PracticeLaunchConfig.lesson(state.quickPracticeLessonId))
                                },
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            Button(onClick = {
                                selectedDifficulty.value = QuickStartDifficulty.EASY
                                selectedWpm.intValue = state.quickStartDefaultWpm
                                showQuickStartSheet.value = true
                            }) {
                                Text("Start Practicing")
                            }
                            OutlinedButton(onClick = onNavigateToLearn) {
                                Text("Browse Lessons")
                            }
                        }
                    }
                }
            }

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

    if (showQuickStartSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showQuickStartSheet.value = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            QuickStartSheet(
                selectedDifficulty = selectedDifficulty.value,
                selectedWpm = selectedWpm.intValue,
                onSelectDifficulty = { selectedDifficulty.value = it },
                onWpmChanged = { selectedWpm.intValue = it },
                onStart = {
                    showQuickStartSheet.value = false
                    onNavigateToPractice(
                        PracticeLaunchConfig.quickStart(
                            difficulty = selectedDifficulty.value,
                            wpmOverride = selectedWpm.intValue,
                        ),
                    )
                },
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
    accent: androidx.compose.ui.graphics.Color,
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
    accent: androidx.compose.ui.graphics.Color,
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
private fun QuickStartSheet(
    selectedDifficulty: QuickStartDifficulty,
    selectedWpm: Int,
    onSelectDifficulty: (QuickStartDifficulty) -> Unit,
    onWpmChanged: (Int) -> Unit,
    onStart: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg)
            .padding(bottom = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        Text("Quick Start", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Start slower and build up as your ear adjusts. This only changes the session you are about to start.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            QuickStartDifficulty.entries.forEach { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { onSelectDifficulty(difficulty) },
                    label = { Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Text("Playback speed: $selectedWpm WPM", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = selectedWpm.toFloat(),
                onValueChange = { onWpmChanged(it.toInt()) },
                valueRange = 5f..40f,
                steps = 34,
            )
        }
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start session")
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
            // Amber top stripe
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
                // Decorative background text
                Text(
                    text = "· — · —",
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
                        Text("Open Freestyle →", fontWeight = FontWeight.Bold)
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
            // Secondary top stripe
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
                // Decorative icon top-right
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
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
                        Text("Open Reference →", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueBanner(
    lessonTitle: String,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(
                text = "Continue from",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = lessonTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        TextButton(onClick = onClick) {
            Text(
                text = "Resume →",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
