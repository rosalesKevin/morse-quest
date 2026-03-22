package morse.android.practice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import morse.android.theme.MorseTheme
import morse.core.TimingEngine

@Composable
fun MorseTouchpad(
    state: TouchpadState,
    allowWordGap: Boolean,
    modifier: Modifier = Modifier,
    onTap: ((Long) -> Unit)? = null,
    onGapElapsed: ((GapType) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    forceShowDelete: Boolean = false,
) {
    var isPressed by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableLongStateOf(0L) }
    var lastReleaseTime by remember { mutableLongStateOf(0L) }
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "pad-press")

    LaunchedEffect(lastReleaseTime, state.answer) {
        if (lastReleaseTime == 0L || state.answer.isEmpty()) return@LaunchedEffect
        delay(state.letterGapThresholdMs)
        val gapHandler = onGapElapsed ?: { state.onGapElapsed(it) }
        gapHandler(GapType.LETTER)
        if (!allowWordGap) return@LaunchedEffect
        delay(state.wordGapThresholdMs - state.letterGapThresholdMs)
        gapHandler(GapType.WORD)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LiveFeedbackStrip(
            letterGroups = state.letterGroups,
            onDelete = onDelete ?: { state.deleteLast() },
            forceShowDelete = forceShowDelete,
        )

        Spacer(Modifier.height(12.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .scale(scale)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            pressStartTime = System.currentTimeMillis()
                            tryAwaitRelease()
                            isPressed = false
                            val duration = System.currentTimeMillis() - pressStartTime
                            state.recordPress(duration)
                            onTap?.invoke(duration)
                            lastReleaseTime = System.currentTimeMillis()
                        },
                    )
                },
        ) {
            Text(
                text = if (isPressed) "..." else "TAP",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LiveFeedbackStrip(
    letterGroups: List<LetterGroup>,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    forceShowDelete: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            for ((index, group) in letterGroups.withIndex()) {
                if (index > 0) {
                    Spacer(Modifier.width(4.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = group.decoded,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (seg in group.segments) {
                            if (seg.symbol == '.') {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface),
                                )
                            } else {
                                Box(
                                    Modifier
                                        .width(20.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.onSurface),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (letterGroups.isNotEmpty() || forceShowDelete) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Backspace,
                    contentDescription = "Delete last",
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MorseTouchpadPreview() {
    MorseTheme {
        val state = remember { TouchpadState(TimingEngine(20, 20)) }
        MorseTouchpad(state = state, allowWordGap = true)
    }
}
