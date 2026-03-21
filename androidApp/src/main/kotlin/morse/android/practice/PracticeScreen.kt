package morse.android.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.practice.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    onFinished: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        is PracticeViewModel.UiState.Exercise -> ExerciseContent(
            state = s,
            onUpdateAnswer = viewModel::updateAnswer,
            onSubmit = viewModel::submitAnswer,
            onNext = viewModel::nextExercise,
            onPlayAudio = viewModel::playCurrentExercise,
        )
        is PracticeViewModel.UiState.Summary -> SummaryContent(
            state = s,
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
    var localAnswer by rememberSaveable(state.index) { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("${state.index + 1} / ${state.total}") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinearProgressIndicator(
                progress = { (state.index + 1).toFloat() / state.total },
                modifier = Modifier.fillMaxWidth(),
            )
            ExercisePrompt(exercise = state.exercise, onPlayAudio = onPlayAudio)

            if (state.result == null) {
                OutlinedTextField(
                    value = localAnswer,
                    onValueChange = { newValue ->
                        localAnswer = newValue
                        onUpdateAnswer(newValue)
                    },
                    label = { Text("Your answer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
                    Text("Submit")
                }
            } else {
                val isCorrect = state.result.isCorrect
                val bg = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = bg.copy(alpha = 0.15f)),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isCorrect) "Correct!" else "Incorrect",
                            style = MaterialTheme.typography.titleMedium,
                            color = bg,
                        )
                        if (!isCorrect) {
                            Text("Expected: ${state.result.expectedText}")
                        }
                    }
                }
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
private fun ExercisePrompt(exercise: Exercise, onPlayAudio: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (exercise) {
                is Exercise.ListenAndIdentify -> {
                    Text("Listen and identify the character:", style = MaterialTheme.typography.labelLarge)
                    IconButton(onClick = onPlayAudio) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                }
                is Exercise.ReadAndTap -> {
                    Text("Tap the Morse code for:", style = MaterialTheme.typography.labelLarge)
                    Text(exercise.character.toString(), style = MaterialTheme.typography.headlineMedium)
                    Text("Enter dots and dashes (e.g. .-)", style = MaterialTheme.typography.bodySmall)
                }
                is Exercise.DecodeWord -> {
                    Text("Decode this Morse:", style = MaterialTheme.typography.labelLarge)
                    Text(exercise.morse, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = onPlayAudio) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                }
                is Exercise.EncodeWord -> {
                    Text("Encode this word in Morse:", style = MaterialTheme.typography.labelLarge)
                    Text(exercise.word, style = MaterialTheme.typography.headlineMedium)
                }
                is Exercise.SpeedChallenge -> {
                    Text("Speed challenge — type what you hear:", style = MaterialTheme.typography.labelLarge)
                    IconButton(onClick = onPlayAudio) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
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
    Scaffold(
        topBar = { TopAppBar(title = { Text("Session Complete") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(state.lesson.title, style = MaterialTheme.typography.titleLarge)
            Text(
                "${"%.0f".format(state.score.accuracy)}% accuracy",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text("${state.score.correct} / ${state.score.total} correct")
            if (state.mistakes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Characters to review:", style = MaterialTheme.typography.labelLarge)
                state.mistakes.forEach { mistake ->
                    Text("${mistake.character} — ${mistake.count} mistake(s)")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Done")
            }
        }
    }
}
