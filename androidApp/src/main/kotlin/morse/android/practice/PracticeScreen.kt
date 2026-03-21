package morse.android.practice

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
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
) {
    val spacing = LocalSpacing.current
    val keyboard = LocalSoftwareKeyboardController.current
    var localAnswer by rememberSaveable(state.index) { mutableStateOf(state.answer) }
    val readAndTap = state.exercise as? Exercise.ReadAndTap
    val tapComposer = remember(state.index, readAndTap?.expectedMorse) {
        MorseTapComposer(
            timingEngine = morse.core.TimingEngine(),
            expectedPattern = readAndTap?.expectedMorse.orEmpty(),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Exercise ${state.index + 1} / ${state.total}") })
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

            ExercisePrompt(exercise = state.exercise, onPlayAudio = onPlayAudio)

            if (state.result == null) {
                if (readAndTap != null) {
                    TapInputCard(
                        expectedPattern = readAndTap.expectedMorse,
                        composer = tapComposer,
                        onAnswerChanged = {
                            localAnswer = it
                            onUpdateAnswer(it)
                        },
                    )
                } else {
                    OutlinedTextField(
                        value = localAnswer,
                        onValueChange = {
                            localAnswer = it
                            onUpdateAnswer(it)
                        },
                        label = { Text("Your answer") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboard?.hide()
                            onSubmit()
                        }),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    if (readAndTap != null) {
                        OutlinedButton(
                            onClick = {
                                tapComposer.clear()
                                localAnswer = ""
                                onUpdateAnswer("")
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Clear")
                        }
                    }
                    Button(
                        onClick = {
                            keyboard?.hide()
                            onSubmit()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Submit")
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
                    PromptLabel("Listen and identify the character")
                    PromptButton(onPlayAudio)
                }
                is Exercise.ReadAndTap -> {
                    PromptLabel("Tap the signal for")
                    Text(exercise.character.toString(), style = MaterialTheme.typography.displayLarge)
                    Text(
                        text = "Press and hold in rhythm. Short holds become dots; longer holds become dashes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is Exercise.DecodeWord -> {
                    PromptLabel("Decode this Morse sequence")
                    Text(exercise.morse, style = MorseDisplayTextStyle)
                    PromptButton(onPlayAudio)
                }
                is Exercise.EncodeWord -> {
                    PromptLabel("Encode this word in Morse")
                    Text(exercise.word, style = MaterialTheme.typography.displayLarge)
                }
                is Exercise.SpeedChallenge -> {
                    PromptLabel("Speed challenge")
                    Text(
                        text = "Type what you hear at ${exercise.targetWpm} WPM.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    PromptButton(onPlayAudio)
                }
            }
        }
    }
}

@Composable
private fun PromptLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PromptButton(onPlayAudio: () -> Unit) {
    IconButton(onClick = onPlayAudio) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Play prompt")
    }
}

@Composable
private fun TapInputCard(
    expectedPattern: String,
    composer: MorseTapComposer,
    onAnswerChanged: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current
    var isPressed by remember { mutableStateOf(false) }
    var pressStartedAt by remember { mutableLongStateOf(0L) }
    var holdDurationMs by remember { mutableLongStateOf(0L) }
    var keyboardPressed by remember { mutableStateOf(false) }
    val dashThreshold = remember { morse.core.TimingEngine().dashDurationMs / 2 }
    val animatedGlow by animateColorAsState(
        targetValue = if (isPressed) extendedColors.signalGlow else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(160),
        label = "tap_glow",
    )

    LaunchedEffect(isPressed, pressStartedAt) {
        while (isPressed) {
            holdDurationMs = SystemClock.elapsedRealtime() - pressStartedAt
            delay(16L)
        }
        holdDurationMs = 0L
    }

    fun startPress() {
        if (isPressed) return
        pressStartedAt = SystemClock.elapsedRealtime()
        isPressed = true
    }

    fun endPress() {
        if (!isPressed) return
        val duration = (SystemClock.elapsedRealtime() - pressStartedAt).coerceAtLeast(1L)
        isPressed = false
        composer.recordPress(duration)
        onAnswerChanged(composer.answer)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text("Expected rhythm", style = MaterialTheme.typography.labelLarge)
            GhostPatternRow(
                expectedPattern = expectedPattern,
                currentAnswer = composer.answer,
                firstMismatchIndex = composer.firstMismatchIndex,
            )

            TimelineRow(composer = composer)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(
                        width = if (holdDurationMs >= dashThreshold) 4.dp else 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(28.dp),
                    )
                    .background(animatedGlow, RoundedCornerShape(28.dp))
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (event.key != Key.Spacebar) return@onPreviewKeyEvent false
                        when (event.type) {
                            KeyEventType.KeyDown -> {
                                if (!keyboardPressed) {
                                    keyboardPressed = true
                                    startPress()
                                }
                                true
                            }
                            KeyEventType.KeyUp -> {
                                keyboardPressed = false
                                endPress()
                                true
                            }
                            else -> false
                        }
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            startPress()
                            waitForUpOrCancellation()
                            endPress()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                val fillScale = (holdDurationMs.toFloat() / (dashThreshold * 2f)).coerceIn(0.4f, 1.15f)
                Box(
                    modifier = Modifier
                        .size((72f * fillScale).dp)
                        .background(
                            color = extendedColors.signalAmber.copy(alpha = if (isPressed) 0.28f else 0.08f),
                            shape = CircleShape,
                        ),
                )
                Text(
                    text = if (isPressed) "Signal live" else "Tap or hold to send Morse",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Text(
                text = composer.answer.ifBlank { "No signal captured yet" },
                style = MorseDisplayTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun GhostPatternRow(
    expectedPattern: String,
    currentAnswer: String,
    firstMismatchIndex: Int,
) {
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current

    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
        expectedPattern.forEachIndexed { index, symbol ->
            val matched = currentAnswer.getOrNull(index) == symbol
            val mismatched = firstMismatchIndex == index
            val width = if (symbol == '.') 18.dp else 40.dp
            val background = when {
                mismatched -> MaterialTheme.colorScheme.errorContainer
                matched -> extendedColors.signalAmber.copy(alpha = 0.85f)
                else -> MaterialTheme.colorScheme.surfaceContainer
            }

            Box(
                modifier = Modifier
                    .width(width)
                    .height(12.dp)
                    .background(background, RoundedCornerShape(999.dp))
                    .border(
                        width = 1.dp,
                        color = if (mismatched) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(999.dp),
                    ),
            )
        }
    }
}

@Composable
private fun TimelineRow(composer: MorseTapComposer) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        composer.segments.takeLast(12).forEach { segment ->
            Box(
                modifier = Modifier
                    .width(if (segment.symbol == '.') 24.dp else 48.dp)
                    .height(16.dp)
                    .background(
                        color = if (segment.matchesHint) {
                            LocalExtendedColors.current.signalAmber.copy(alpha = 0.9f)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        },
                        shape = RoundedCornerShape(999.dp),
                    ),
            )
        }
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
                text = if (isCorrect) "Correct signal" else "Signal drift detected",
                style = MaterialTheme.typography.titleLarge,
                color = accent,
            )
            if (!isCorrect) {
                Text(
                    text = "Expected answer",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = state.result?.expectedText.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                )
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
        topBar = { TopAppBar(title = { Text("Session Complete") }) },
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
