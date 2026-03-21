package morse.android.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.android.persistence.AudioProfile
import morse.android.persistence.ThemeMode
import morse.android.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val spacing = LocalSpacing.current
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            SettingsSection(
                title = "Appearance",
                copy = "Keep the station matched to system theme or lock it to a dedicated light or dark surface.",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.updateThemeMode(mode) },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }

            SettingsSection(
                title = "Tone profile",
                copy = "Choose the playback character you want for lessons and practice.",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    AudioProfile.entries.forEach { profile ->
                        FilterChip(
                            selected = settings.audioProfile == profile,
                            onClick = { viewModel.updateAudioProfile(profile) },
                            label = { Text(profile.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }

            SettingsSection(
                title = "Timing",
                copy = "Tune speed and pitch without leaving the session flow.",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Text("Speed: ${settings.wpm} WPM", style = MaterialTheme.typography.titleMedium)
                        Slider(
                            value = settings.wpm.toFloat(),
                            onValueChange = { viewModel.updateWpm(it.toInt()) },
                            valueRange = 5f..40f,
                            steps = 34,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Text("Pitch: ${"%.0f".format(settings.toneFrequencyHz)} Hz", style = MaterialTheme.typography.titleMedium)
                        Slider(
                            value = settings.toneFrequencyHz,
                            onValueChange = { viewModel.updateToneFrequency(it) },
                            valueRange = 400f..900f,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            SettingsSection(
                title = "Feedback",
                copy = "Use haptics when you want a tactile timing cue alongside the audio tone.",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Haptic feedback", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Mirror signal timing with device vibration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = settings.hapticsEnabled,
                        onCheckedChange = { viewModel.updateHapticsEnabled(it) },
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    Text("Reset progress", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                    Text(
                        text = "Erase saved sessions, streaks, and unlock state. This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Button(onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Reset")
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset progress") },
            text = { Text("This clears all saved sessions and unlock state on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetProgress()
                        showResetDialog = false
                    },
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    copy: String,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalSpacing.current.lg),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.md),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = copy,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}
