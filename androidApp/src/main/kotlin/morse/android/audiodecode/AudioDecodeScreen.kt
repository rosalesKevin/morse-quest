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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.android.theme.LocalExtendedColors
import morse.android.theme.LocalSpacing
import morse.android.theme.MorseDisplayTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDecodeScreen(
    onBack: () -> Unit,
    viewModel: AudioDecodeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
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
                title = { Text("Audio Decode") },
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
                .padding(horizontal = spacing.lg, vertical = spacing.xl)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ),
                        )
                        .padding(spacing.xl),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                        Text(
                            text = "Capture a live tone source, inspect the Morse stream, and confirm the decoded text before it drifts.",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        ListeningIndicator(isListening = state.isListening)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text("Capture sensitivity", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Raise sensitivity for quiet rooms. Lower it when ambient noise starts creating false positives.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Sensitivity: ${"%.0f".format(state.sensitivity * 100)}%",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = state.sensitivity,
                        onValueChange = viewModel::updateSensitivity,
                        valueRange = 0.01f..0.30f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            AnimatedVisibility(visible = state.permissionDenied) {
                StatusCard(
                    title = "Microphone permission required",
                    body = "Grant microphone access so the decoder can capture tone events from the environment.",
                    accent = MaterialTheme.colorScheme.error,
                )
            }

            state.error?.let { error ->
                StatusCard(
                    title = "Capture error",
                    body = error,
                    accent = MaterialTheme.colorScheme.error,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                DecodeOutputCard(
                    modifier = Modifier.weight(1f),
                    label = "Morse stream",
                    text = state.morseText,
                    placeholder = "Waiting for tone events",
                    textStyle = MorseDisplayTextStyle,
                )
                DecodeOutputCard(
                    modifier = Modifier.weight(1f),
                    label = "Decoded text",
                    text = state.decodedText,
                    placeholder = "Decoded output appears here",
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text("Operator notes", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "If the Morse lane keeps smearing, lower sensitivity or move the device closer to the tone source before restarting capture.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.sm))
        }
    }
}

@Composable
private fun ListeningIndicator(isListening: Boolean) {
    val spacing = LocalSpacing.current
    val extendedColors = LocalExtendedColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        if (isListening) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "alpha",
            )
            Surface(
                color = extendedColors.signalAmber.copy(alpha = 0.18f),
                shape = RoundedCornerShape(999.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Listening",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp).alpha(alpha),
                    )
                    Text(
                        "Listening live",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(999.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Icon(
                        Icons.Default.MicOff,
                        contentDescription = "Not listening",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        "Microphone idle",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    body: String,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(LocalSpacing.current.lg),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.xs),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = accent)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DecodeOutputCard(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    placeholder: String,
    textStyle: TextStyle,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(LocalSpacing.current.lg),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.sm),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text.ifEmpty { placeholder },
                style = if (text.isEmpty()) MaterialTheme.typography.bodyMedium else textStyle,
                color = if (text.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
