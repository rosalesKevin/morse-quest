package morse.android.quest

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.android.practice.GuidedWordInput
import morse.android.practice.MorseTouchpad
import morse.android.practice.SingleCharacterSlot
import morse.android.theme.LocalExtendedColors
import morse.android.theme.LocalSpacing
import morse.android.theme.MorseDisplayTextStyle
import morse.practice.Exercise
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun DailyQuestScreen(
    onBack: () -> Unit,
    viewModel: DailyQuestViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var sessionStarted by rememberSaveable { mutableStateOf(false) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = sessionStarted) {
        sessionStarted = false
    }

    when {
        state.quest == null -> LoadingContent()
        state.isCompleted -> CompletionContent(
            correctCount = state.correctCount,
            totalQuestions = state.quest!!.questions.size,
            onDone = onBack,
        )
        !sessionStarted -> LobbyContent(
            state = state,
            onDifficultySelected = viewModel::onDifficultySelected,
            onBegin = {
                currentIndex = state.progressIndex
                viewModel.refreshTouchpadForQuestion(state.progressIndex)
                sessionStarted = true
            },
            onBack = onBack,
        )
        else -> {
            val quest = state.quest!!
            ExerciseContent(
                quest = quest,
                currentIndex = currentIndex,
                viewModel = viewModel,
                onQuestionAnswered = { index, correct ->
                    viewModel.onQuestionAnswered(index, correct)
                    val nextIndex = index + 1
                    if (nextIndex >= quest.questions.size) {
                        viewModel.onQuestCompleted()
                    } else {
                        currentIndex = nextIndex
                        viewModel.refreshTouchpadForQuestion(nextIndex)
                    }
                },
                onBack = { sessionStarted = false },
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading quest…", style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LobbyContent(
    state: DailyQuestViewModel.UiState,
    onDifficultySelected: (DailyQuestDifficulty) -> Unit,
    onBegin: () -> Unit,
    onBack: () -> Unit,
) {
    val quest = state.quest ?: return
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Quest") },
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = spacing.lg, vertical = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Text(
                text = today,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Difficulty picker
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
                    Text("Difficulty", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        DailyQuestDifficulty.entries.forEach { difficulty ->
                            FilterChip(
                                selected = state.selectedDifficulty == difficulty,
                                onClick = { onDifficultySelected(difficulty) },
                                label = {
                                    Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() })
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = extendedColors.exercisePurple.copy(alpha = 0.2f),
                                    selectedLabelColor = extendedColors.exercisePurple,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = state.selectedDifficulty == difficulty,
                                    selectedBorderColor = extendedColors.exercisePurple,
                                ),
                            )
                        }
                    }
                }
            }

            // Summary card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        SummaryMetric(modifier = Modifier.weight(1f), label = "Questions", value = "${quest.questions.size}")
                        SummaryMetric(modifier = Modifier.weight(1f), label = "Est. time", value = "~${quest.estimatedMinutes} min")
                    }

                    if (quest.exerciseSummary.isNotEmpty()) {
                        Text(
                            text = quest.exerciseSummary.entries.joinToString("  ·  ") { (kind, count) ->
                                "${kind.name.lowercase().replaceFirstChar { it.uppercase() }} ×$count"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Character chips
                    val displayChars = quest.characters.take(10)
                    val overflow = quest.characters.size - displayChars.size
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        items(displayChars) { char ->
                            QuestCharChip(char.toString())
                        }
                        if (overflow > 0) {
                            item { QuestCharChip("+$overflow") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBegin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extendedColors.exercisePurple,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                ),
            ) {
                Text(
                    text = if (state.progressIndex > 0) "Resume ->" else "Begin ->",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(modifier: Modifier = Modifier, label: String, value: String) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.xxs)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuestCharChip(label: String) {
    val extendedColors = LocalExtendedColors.current
    val spacing = LocalSpacing.current
    Surface(
        color = extendedColors.exercisePurple.copy(alpha = 0.10f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = extendedColors.exercisePurple,
            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseContent(
    quest: DailyQuest,
    currentIndex: Int,
    viewModel: DailyQuestViewModel,
    onQuestionAnswered: (index: Int, correct: Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val exercise = quest.questions.getOrNull(currentIndex) ?: return
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current
    val keyboard = LocalSoftwareKeyboardController.current

    var localAnswer by rememberSaveable(currentIndex) { mutableStateOf("") }
    var showResult by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var lastIsCorrect by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showEmptyError by rememberSaveable(currentIndex) { mutableStateOf(false) }

    // Auto-play audio for listen/decode exercises
    LaunchedEffect(currentIndex) {
        if (exercise is Exercise.ListenAndIdentify || exercise is Exercise.DecodeWord) {
            viewModel.playCurrentExercise(currentIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Daily Quest")
                        Text(
                            text = "${currentIndex + 1} / ${quest.questions.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
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
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / quest.questions.size.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = extendedColors.exercisePurple,
            )

            QuestExercisePrompt(
                exercise = exercise,
                onPlayAudio = { viewModel.playCurrentExercise(currentIndex) },
            )

            if (!showResult) {
                when (exercise) {
                    is Exercise.ListenAndIdentify -> {
                        SingleCharacterSlot(
                            value = localAnswer,
                            onValueChange = {
                                localAnswer = it
                                showEmptyError = false
                            },
                            onSubmit = {
                                keyboard?.hide()
                                val correct = localAnswer.trim().uppercase() == exercise.answer.toString()
                                lastIsCorrect = correct
                                showResult = true
                            },
                        )
                        if (showEmptyError) {
                            Text(
                                text = "Enter a letter before submitting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    is Exercise.ReadAndTap -> {
                        viewModel.touchpadState?.let { pad ->
                            MorseTouchpad(state = pad, allowWordGap = false)
                        }
                        Button(
                            onClick = {
                                val tapped = viewModel.touchpadState?.answer ?: ""
                                val correct = canonicalizeMorse(tapped) == exercise.expectedMorse
                                lastIsCorrect = correct
                                showResult = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Submit") }
                    }
                    is Exercise.DecodeWord -> {
                        GuidedWordInput(
                            value = localAnswer,
                            onValueChange = {
                                localAnswer = it
                                showEmptyError = false
                            },
                            onSubmit = {
                                if (localAnswer.isBlank()) {
                                    showEmptyError = true
                                } else {
                                    keyboard?.hide()
                                    val correct = localAnswer.trim().uppercase() == exercise.answer.uppercase()
                                    lastIsCorrect = correct
                                    showResult = true
                                }
                            },
                            expectedLength = exercise.answer.length,
                            placeholder = "Type the decoded word…",
                        )
                        if (showEmptyError) {
                            Text(
                                text = "Type the decoded word before submitting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        Button(
                            onClick = {
                                if (localAnswer.isBlank()) {
                                    showEmptyError = true
                                } else {
                                    keyboard?.hide()
                                    val correct = localAnswer.trim().uppercase() == exercise.answer.uppercase()
                                    lastIsCorrect = correct
                                    showResult = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Submit") }
                    }
                    is Exercise.EncodeWord -> {
                        viewModel.touchpadState?.let { pad ->
                            MorseTouchpad(state = pad, allowWordGap = false)
                        }
                        Button(
                            onClick = {
                                val tapped = viewModel.touchpadState?.answer ?: ""
                                val correct = canonicalizeMorse(tapped) == exercise.expectedMorse
                                lastIsCorrect = correct
                                showResult = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Submit") }
                    }
                    else -> Unit // SpeedChallenge excluded from Daily Quest
                }
            } else {
                QuestResultCard(isCorrect = lastIsCorrect, exercise = exercise)
                Button(
                    onClick = { onQuestionAnswered(currentIndex, lastIsCorrect) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Next") }
            }
        }
    }
}

@Composable
private fun QuestExercisePrompt(
    exercise: Exercise,
    onPlayAudio: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            when (exercise) {
                is Exercise.ListenAndIdentify -> {
                    IconButton(onClick = onPlayAudio) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play prompt")
                    }
                }
                is Exercise.ReadAndTap -> {
                    Text(exercise.character.toString(), style = MaterialTheme.typography.displayLarge)
                }
                is Exercise.DecodeWord -> {
                    Text(exercise.morse, style = MorseDisplayTextStyle)
                    IconButton(onClick = onPlayAudio) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play prompt")
                    }
                }
                is Exercise.EncodeWord -> {
                    Text(exercise.word, style = MaterialTheme.typography.displayLarge)
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun QuestResultCard(isCorrect: Boolean, exercise: Exercise) {
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current
    val background = if (isCorrect) extendedColors.successContainer else MaterialTheme.colorScheme.errorContainer
    val accent = if (isCorrect) extendedColors.success else MaterialTheme.colorScheme.error

    Card(
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = if (isCorrect) "Correct" else "Incorrect",
                style = MaterialTheme.typography.titleLarge,
                color = accent,
            )
            if (!isCorrect) {
                val expected = when (exercise) {
                    is Exercise.ListenAndIdentify -> exercise.answer.toString()
                    is Exercise.ReadAndTap -> exercise.expectedMorse
                    is Exercise.DecodeWord -> exercise.answer
                    is Exercise.EncodeWord -> exercise.expectedMorse
                    else -> ""
                }
                if (expected.isNotBlank()) {
                    Text(
                        text = "Expected: $expected",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompletionContent(
    correctCount: Int,
    totalQuestions: Int,
    onDone: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current
    val accuracy = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions * 100).roundToInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Daily Quest") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacing.lg, vertical = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = extendedColors.successContainer),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Quest Complete!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.success,
                    )
                    Text(
                        text = "$accuracy%",
                        style = MaterialTheme.typography.displayLarge,
                        color = extendedColors.success,
                    )
                    Text(
                        text = "$correctCount / $totalQuestions correct",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Back to Home") }
        }
    }
}

private fun canonicalizeMorse(answer: String): String =
    answer
        .replace(Regex("\\s*/\\s*"), " / ")
        .trim()
        .replace(Regex("\\s+"), " ")
