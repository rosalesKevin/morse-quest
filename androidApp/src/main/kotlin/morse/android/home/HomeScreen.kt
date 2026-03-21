package morse.android.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.android.theme.LocalExtendedColors
import morse.android.theme.LocalSpacing
import morse.android.theme.MorseInlineTextStyle
import morse.android.theme.StatValueTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLearn: () -> Unit,
    onNavigateToPractice: (String) -> Unit,
    onNavigateToReference: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAudioDecode: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current
    val quickPracticeLessonId = state.quickPracticeLessonId.ifBlank { "lesson-1" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signal Station") },
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
                            text = "Train your ear. Build fluent timing. Keep the signal clean.",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "A focused Morse workspace for learning patterns, sharpening rhythm, and tracking real progress.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            Button(onClick = { onNavigateToPractice(quickPracticeLessonId) }) {
                                Text("Quick Practice")
                            }
                            OutlinedButton(onClick = onNavigateToLearn) {
                                Text("Open Skill Tree")
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Current streak",
                    value = "${state.streakDays}",
                    supporting = "days active",
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Overall accuracy",
                    value = "${"%.0f".format(state.overallAccuracy)}%",
                    supporting = "all sessions",
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
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Best WPM",
                    value = state.bestWpm.toString(),
                    supporting = "personal best",
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
                    Text("Personal bests", style = MaterialTheme.typography.titleLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        BestMetric(
                            modifier = Modifier.weight(1f),
                            label = "Accuracy",
                            value = "${"%.0f".format(state.bestAccuracy)}%",
                        )
                        BestMetric(
                            modifier = Modifier.weight(1f),
                            label = "Longest streak",
                            value = "${state.longestStreakDays}d",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Reference",
                    copy = "Look up any character or play it back instantly.",
                    cta = "Open",
                    onClick = onNavigateToReference,
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Learn",
                    copy = "Move through lessons visually instead of hunting a flat list.",
                    cta = "Browse",
                    onClick = onNavigateToLearn,
                )
            }
            ActionCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Audio Decode",
                copy = "Capture a live tone source through the microphone and watch the Morse stream resolve in real time.",
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
