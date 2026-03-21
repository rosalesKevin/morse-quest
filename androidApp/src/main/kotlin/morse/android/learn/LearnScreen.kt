package morse.android.learn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.core.MorseAlphabet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onNavigateToPractice: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LearnViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        is LearnViewModel.UiState.LessonList -> LessonListContent(
            items = s.lessons,
            onSelect = { viewModel.selectLesson(it) },
            onBack = onBack,
        )
        is LearnViewModel.UiState.LessonDetail -> LessonDetailContent(
            lesson = s.lesson,
            isUnlocked = s.isUnlocked,
            onPlayChar = { viewModel.playCharacter(it) },
            onStartPractice = { onNavigateToPractice(s.lesson.id) },
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lessons") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(items) { item ->
                ListItem(
                    headlineContent = { Text(item.lesson.title) },
                    supportingContent = {
                        Text("Characters: ${item.lesson.characters.joinToString()}")
                    },
                    trailingContent = {
                        if (!item.isUnlocked) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.clickable(enabled = item.isUnlocked) { onSelect(item.lesson) },
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonDetailContent(
    lesson: morse.practice.Lesson,
    isUnlocked: Boolean,
    onPlayChar: (Char) -> Unit,
    onStartPractice: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Introduced characters:", style = MaterialTheme.typography.labelLarge)
            lesson.characters.forEach { char ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "$char   ${MorseAlphabet.characters[char] ?: ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { onPlayChar(char) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play $char")
                    }
                }
            }
            if (isUnlocked) {
                Button(
                    onClick = onStartPractice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    Text("Start Practice")
                }
            }
        }
    }
}
