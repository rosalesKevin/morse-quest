package morse.android.freestyle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import morse.android.practice.MorseTouchpad
import morse.android.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreestyleScreen(
    onBack: () -> Unit,
    viewModel: FreestyleViewModel = hiltViewModel(),
) {
    val decodedText by viewModel.decodedText.collectAsState()
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showReference by remember { mutableStateOf(false) }
    val textScrollState = rememberScrollState()

    // Auto-scroll text canvas to bottom when new content arrives
    LaunchedEffect(decodedText) {
        textScrollState.animateScrollTo(textScrollState.maxValue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Freestyle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showReference = true }) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Morse reference")
                    }
                    IconButton(onClick = {
                        val snapshot = decodedText
                        viewModel.onClearAll()
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Canvas cleared",
                                actionLabel = "Undo",
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreText(snapshot)
                            }
                        }
                    }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            // Upper region: text canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(spacing.lg)
                    .verticalScroll(textScrollState),
            ) {
                Text(
                    text = if (decodedText.isEmpty()) "Start tapping…" else decodedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (decodedText.isEmpty())
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                )
            }

            // Lower region: touchpad
            // forceShowDelete keeps the backspace button visible even when the in-progress
            // buffer is empty, so the user can delete the last committed char (AC7).
            MorseTouchpad(
                state = viewModel.touchpadState,
                allowWordGap = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.lg)
                    .padding(bottom = spacing.lg),
                onTap = viewModel::onTap,
                onGapElapsed = viewModel::onGapElapsed,
                onDelete = viewModel::onDeleteLast,
                forceShowDelete = decodedText.isNotEmpty(),
            )
        }
    }

    if (showReference) {
        MorseReferencePanel(onDismiss = { showReference = false })
    }
}
