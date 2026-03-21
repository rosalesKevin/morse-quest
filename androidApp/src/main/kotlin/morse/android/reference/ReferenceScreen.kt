package morse.android.reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import morse.android.theme.LocalSpacing
import morse.android.theme.MorseInlineTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceScreen(
    onBack: () -> Unit,
    viewModel: ReferenceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current
    val sections = buildReferenceSections(state.entries, state.query)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reference") },
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
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::updateQuery,
                label = { Text("Search character or pattern") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            Text("Alphabet and number sheet", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Tap any character to hear it. Use search when you want one exact symbol, or scan the grouped sheet when you are learning patterns.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                items(sections) { section ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
                            )
                            section.entries.forEachIndexed { index, entry ->
                                ReferenceEntryRow(
                                    entry = entry,
                                    onPlay = { viewModel.playCharacter(entry.character) },
                                )
                                if (index != section.entries.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceEntryRow(
    entry: ReferenceViewModel.ReferenceEntry,
    onPlay: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            Text(
                text = entry.character.toString(),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = entry.morse,
                style = MorseInlineTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onPlay) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play ${entry.character}")
        }
    }
}
