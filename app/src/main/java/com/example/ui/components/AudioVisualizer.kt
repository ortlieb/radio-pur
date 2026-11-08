package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldLight

@Composable
fun AudioVisualizerBars(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barCount: Int = 12,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 2.5.dp,
    maxHeight: Dp = 24.dp,
    primaryColor: Color = AmberGold,
    secondaryColor: Color = AmberGoldLight
) {
    val displayedAmplitudes = amplitudes.take(barCount).let {
        if (it.size < barCount) it + List(barCount - it.size) { 0.2f } else it
    }

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(barSpacing),
        verticalAlignment = Alignment.Bottom
    ) {
        displayedAmplitudes.forEachIndexed { index, amp ->
            val animatedAmp by animateFloatAsState(
                targetValue = amp.coerceIn(0.12f, 1f),
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
                label = "amp_$index"
            )

            Canvas(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight()
            ) {
                val height = size.height * animatedAmp
                val top = size.height - height
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(secondaryColor, primaryColor),
                        startY = top,
                        endY = size.height
                    ),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, height),
                    cornerRadius = CornerRadius(barWidth.toPx() / 2f, barWidth.toPx() / 2f)
                )
            }
        }
    }
}

@Composable
fun LivePulseBadge(
    modifier: Modifier = Modifier,
    text: String = "LIVE"
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.height(8.dp).width(8.dp)) {
            drawCircle(
                color = Color(0xFF10B981),
                radius = 4.dp.toPx()
            )
        }
    }
}
