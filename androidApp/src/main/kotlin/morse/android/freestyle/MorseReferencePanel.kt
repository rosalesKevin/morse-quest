package morse.android.freestyle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import morse.android.theme.LocalSpacing
import morse.core.MorseAlphabet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MorseReferencePanel(
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var query by remember { mutableStateOf("") }
    val spacing = LocalSpacing.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg)
                .padding(bottom = spacing.xxl)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Text("Morse Reference", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = query,
                onValueChange = { if (it.length <= 1) query = it.uppercase() },
                placeholder = { Text("Search A–Z, 0–9 …") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            val letters = MorseAlphabet.characters.entries
                .filter { it.key.isLetter() }
                .filter { query.isEmpty() || it.key.toString() == query }
                .sortedBy { it.key }

            val numbers = MorseAlphabet.characters.entries
                .filter { it.key.isDigit() }
                .filter { query.isEmpty() || it.key.toString() == query }
                .sortedBy { it.key }

            val punctuation = MorseAlphabet.characters.entries
                .filter { !it.key.isLetterOrDigit() }
                .filter { query.isEmpty() || it.key.toString() == query }
                .sortedBy { it.key }

            if (letters.isNotEmpty()) {
                ReferenceSection(title = "Letters", entries = letters)
            }
            if (numbers.isNotEmpty()) {
                ReferenceSection(title = "Numbers", entries = numbers)
            }
            if (punctuation.isNotEmpty()) {
                ReferenceSection(title = "Punctuation", entries = punctuation)
            }
            if (letters.isEmpty() && numbers.isEmpty() && punctuation.isEmpty()) {
                Text(
                    "No match for \"$query\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReferenceSection(
    title: String,
    entries: List<Map.Entry<Char, String>>,
) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            entries.forEach { (char, pattern) ->
                ReferenceEntry(char = char, pattern = pattern)
            }
        }
    }
}

@Composable
private fun ReferenceEntry(char: Char, pattern: String) {
    val spacing = LocalSpacing.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
    ) {
        Text(
            text = char.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            pattern.forEach { symbol ->
                if (symbol == '.') {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface),
                    )
                } else {
                    Box(
                        Modifier
                            .width(14.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.onSurface),
                    )
                }
            }
        }
    }
}
