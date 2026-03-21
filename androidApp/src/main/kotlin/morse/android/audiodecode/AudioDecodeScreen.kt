package morse.android.audiodecode

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDecodeScreen(
    onBack: () -> Unit,
    viewModel: AudioDecodeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onPermissionGranted()
            viewModel.startListening()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Decode Audio") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ListeningIndicator(isListening = state.isListening)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!state.isListening) {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Text(" Start")
                    }
                } else {
                    OutlinedButton(
                        onClick = viewModel::stopListening,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.MicOff, contentDescription = null)
                        Text(" Stop")
                    }
                }
                OutlinedButton(
                    onClick = viewModel::reset,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reset")
                }
            }

            Column {
                Text(
                    "Sensitivity: ${"%.0f".format(state.sensitivity * 100)}%",
                    style = MaterialTheme.typography.labelMedium,
                )
                Slider(
                    value = state.sensitivity,
                    onValueChange = viewModel::updateSensitivity,
                    valueRange = 0.01f..0.30f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AnimatedVisibility(visible = state.permissionDenied) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Microphone permission is required. Grant it in app settings.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            state.error?.let { error ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Error: $error",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            DecodeOutputCard(label = "Morse", text = state.morseText)
            DecodeOutputCard(label = "Text", text = state.decodedText)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ListeningIndicator(isListening: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isListening) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "alpha",
            )
            Icon(
                Icons.Default.Mic,
                contentDescription = "Listening",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp).alpha(alpha),
            )
            Text(
                "Listening…",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Icon(
                Icons.Default.MicOff,
                contentDescription = "Not listening",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp),
            )
            Text(
                "Not listening",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun DecodeOutputCard(label: String, text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text.ifEmpty { "—" },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
