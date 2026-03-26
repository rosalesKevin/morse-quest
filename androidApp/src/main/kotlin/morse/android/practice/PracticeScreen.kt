package morse.android.practice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.reflect.KClass
import morse.android.theme.LocalExtendedColors
import morse.android.theme.LocalSpacing
import morse.android.theme.MorseDisplayTextStyle
import morse.android.theme.MorseInlineTextStyle
import morse.practice.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    onFinished: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    when (val current = state) {
        is PracticeViewModel.UiState.Exercise -> ExerciseContent(
            state = current,
            onUpdateAnswer = viewModel::updateAnswer,
            onSubmit = viewModel::submitAnswer,
            onNext = viewModel::nextExercise,
            onPlayAudio = viewModel::playCurrentExercise,
            viewModel = viewModel,
        )
        is PracticeViewModel.UiState.Summary -> SummaryContent(
            state = current,
            onDone = onFinished,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseContent(
    state: PracticeViewModel.UiState.Exercise,
    onUpdateAnswer: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onPlayAudio: () -> Unit,
    viewModel: PracticeViewModel,
) {
    val spacing = LocalSpacing.current
    val keyboard = LocalSoftwareKeyboardController.current
    var localAnswer by rememberSaveable(state.index) { mutableStateOf(state.answer) }
    var lessonGuideExpanded by rememberSaveable(state.lesson.id) { mutableStateOf(true) }
    var showEmptyError by rememberSaveable(state.index) { mutableStateOf(false) }

    val config = remember(state.exercise) { ModeStripConfig.from(state.exercise) }
    val hintVisible = viewModel.isHintVisible(state.exercise::class)
    var previousExerciseType by remember { mutableStateOf<KClass<out Exercise>?>(null) }
    val isTypeChange = previousExerciseType != null && previousExerciseType != state.exercise::class
    LaunchedEffect(state.exercise::class) {
        previousExerciseType = state.exercise::class
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.lesson.title)
                        Text(
                            text = "Practice ${state.index + 1} / ${state.total}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                progress = { (state.index + 1).toFloat() / state.total.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedContent(
                targetState = state.exercise::class,
                transitionSpec = {
                    if (isTypeChange) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        fadeIn() togetherWith fadeOut()
                    }
                },
                label = "mode-strip-transition",
            ) { _ ->
                ModeStrip(
                    config = config,
                    hintVisible = hintVisible,
                    onToggleHint = { viewModel.toggleHint(state.exercise::class) },
                )
            }

            ExercisePrompt(exercise = state.exercise, onPlayAudio = onPlayAudio)

            LessonReferenceGuide(
                lesson = state.lesson,
                expanded = lessonGuideExpanded,
                onToggleExpanded = { lessonGuideExpanded = !lessonGuideExpanded },
            )

            if (state.result == null) {
                when (val exercise = state.exercise) {
                    is Exercise.ReadAndTap -> {
                        viewModel.touchpadState?.let { pad ->
                            MorseTouchpad(state = pad, allowWordGap = false)
                        }
                        Button(
                            onClick = { keyboard?.hide(); onSubmit() },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Submit") }
                    }
                    is Exercise.EncodeWord -> {
                        viewModel.touchpadState?.let { pad ->
                            MorseTouchpad(state = pad, allowWordGap = false)
                        }
                        Button(
                            onClick = { keyboard?.hide(); onSubmit() },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Submit") }
                    }
                    is Exercise.ListenAndIdentify -> {
                        SingleCharacterSlot(
                            value = localAnswer,
                            onValueChange = {
                                localAnswer = it
                                showEmptyError = false
                                onUpdateAnswer(it)
                            },
                            onSubmit = { keyboard?.hide(); onSubmit() },
                        )
                        if (showEmptyError) {
                            Text(
                                text = "Enter a letter before submitting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    is Exercise.DecodeWord -> {
                        GuidedWordInput(
                            value = localAnswer,
                            onValueChange = {
                                localAnswer = it
                                showEmptyError = false
                                onUpdateAnswer(it)
                            },
                            onSubmit = {
                                if (localAnswer.isBlank()) {
                                    showEmptyError = true
                                } else {
                                    keyboard?.hide(); onSubmit()
                                }
                            },
                            expectedLength = exercise.answer.length,
                            placeholder = "Type the decoded word...",
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
                                    keyboard?.hide(); onSubmit()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Submit") }
                    }
                    is Exercise.SpeedChallenge -> {
                        GuidedWordInput(
                            value = localAnswer,
                            onValueChange = {
                                localAnswer = it
                                showEmptyError = false
                                onUpdateAnswer(it)
                            },
                            onSubmit = {
                                if (localAnswer.isBlank()) {
                                    showEmptyError = true
                                } else {
                                    keyboard?.hide(); onSubmit()
                                }
                            },
                            expectedLength = null,
                            placeholder = "Transcribe here...",
                        )
                        if (showEmptyError) {
                            Text(
                                text = "Transcribe what you hear before submitting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        Button(
                            onClick = {
                                if (localAnswer.isBlank()) {
                                    showEmptyError = true
                                } else {
                                    keyboard?.hide(); onSubmit()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Submit") }
                    }
                }
            } else {
                ResultCard(state = state)
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
private fun ExercisePrompt(
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
                    PromptButton(onPlayAudio)
                }
                is Exercise.ReadAndTap -> {
                    Text(exercise.character.toString(), style = MaterialTheme.typography.displayLarge)
                }
                is Exercise.DecodeWord -> {
                    Text(exercise.morse, style = MorseDisplayTextStyle)
                    PromptButton(onPlayAudio)
                }
                is Exercise.EncodeWord -> {
                    Text(exercise.word, style = MaterialTheme.typography.displayLarge)
                }
                is Exercise.SpeedChallenge -> {
                    Text(
                        text = "At ${exercise.targetWpm} WPM",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    PromptButton(onPlayAudio)
                }
            }
        }
    }
}

@Composable
private fun PromptButton(onPlayAudio: () -> Unit) {
    IconButton(onClick = onPlayAudio) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Play prompt")
    }
}

@Composable
private fun ResultCard(state: PracticeViewModel.UiState.Exercise) {
    val spacing = LocalSpacing.current
    val isCorrect = state.result?.isCorrect == true
    val background = if (isCorrect) {
        LocalExtendedColors.current.successContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val accent = if (isCorrect) {
        LocalExtendedColors.current.success
    } else {
        MaterialTheme.colorScheme.error
    }

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
                text = if (isCorrect) "Correct" else "Try again",
                style = MaterialTheme.typography.titleLarge,
                color = accent,
            )
            if (!isCorrect) {
                val result = state.result
                if (result != null) {
                    Text(
                        text = "Expected: ${result.expectedText}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (result.expectedAnswer != result.expectedText) {
                        Text(
                            text = result.expectedAnswer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SummaryContent(
    state: PracticeViewModel.UiState.Summary,
    onDone: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.lesson.title)
                        Text(
                            text = "Session summary",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
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
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text(state.lesson.title, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "${"%.0f".format(state.score.accuracy)}% accuracy",
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Text(
                        text = "${state.score.correct} / ${state.score.total} correct at ${"%.0f".format(state.score.wpm)} WPM",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.mistakes.isNotEmpty()) {
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
                        Text("Characters to review", style = MaterialTheme.typography.titleLarge)
                        state.mistakes.forEach { mistake ->
                            Text(
                                text = "${mistake.character}  ${mistake.count} mistake(s)",
                                style = MorseInlineTextStyle,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Done")
            }
        }
    }
}
