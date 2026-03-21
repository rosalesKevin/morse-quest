package morse.android.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import morse.android.theme.LocalExtendedColors
import morse.practice.Exercise

enum class ModeStripIcon { HEADPHONES, TAP, PUZZLE, BROADCAST, ZAP }

data class ModeStripConfig(
    val label: String,
    val hint: String,
    val icon: ModeStripIcon,
    val accentColor: @Composable () -> Color,
) {
    companion object {
        fun from(exercise: Exercise): ModeStripConfig = when (exercise) {
            is Exercise.ListenAndIdentify -> ModeStripConfig(
                label = "Listen & Identify",
                hint = "Type the letter you hear",
                icon = ModeStripIcon.HEADPHONES,
                accentColor = { LocalExtendedColors.current.exercisePurple },
            )
            is Exercise.ReadAndTap -> ModeStripConfig(
                label = "Read & Tap",
                hint = "Tap the Morse for this character",
                icon = ModeStripIcon.TAP,
                accentColor = { MaterialTheme.colorScheme.primary },
            )
            is Exercise.DecodeWord -> ModeStripConfig(
                label = "Decode",
                hint = "Type the word these signals spell",
                icon = ModeStripIcon.PUZZLE,
                accentColor = { LocalExtendedColors.current.exerciseTeal },
            )
            is Exercise.EncodeWord -> ModeStripConfig(
                label = "Encode",
                hint = "Tap the Morse for this word",
                icon = ModeStripIcon.BROADCAST,
                accentColor = { LocalExtendedColors.current.exerciseOrange },
            )
            is Exercise.SpeedChallenge -> ModeStripConfig(
                label = "Speed Round",
                hint = "Transcribe what you hear at speed",
                icon = ModeStripIcon.ZAP,
                accentColor = { LocalExtendedColors.current.rewardAmber },
            )
        }
    }
}

private fun ModeStripIcon.toImageVector(): ImageVector = when (this) {
    ModeStripIcon.HEADPHONES -> Icons.Default.Hearing
    ModeStripIcon.TAP -> Icons.Default.TouchApp
    ModeStripIcon.PUZZLE -> Icons.Default.Extension
    ModeStripIcon.BROADCAST -> Icons.Default.CellTower
    ModeStripIcon.ZAP -> Icons.Default.Bolt
}

@Composable
fun ModeStrip(
    config: ModeStripConfig,
    hintVisible: Boolean,
    onToggleHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = config.accentColor()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(accent.copy(alpha = 0.12f))
            .clickable(onClick = onToggleHint)
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = config.icon.toImageVector(),
                contentDescription = config.label,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = config.label,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
            )
        }
        AnimatedVisibility(
            visible = hintVisible,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Text(
                text = config.hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
