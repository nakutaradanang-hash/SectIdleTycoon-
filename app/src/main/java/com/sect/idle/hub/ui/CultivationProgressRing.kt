package com.sect.idle.hub.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * CultivationProgressRing - Material 3 CircularProgressIndicator tracking a disciple's
 * current cultivation energy / realm accumulation relative to their next rank-up breakthrough threshold.
 *
 * Highly optimized for low-end Android devices (itel A70, 1GB RAM) with minimal recomposition cost.
 */
@Composable
fun CultivationProgressRing(
    currentEnergy: Int,
    maxThreshold: Int,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 5.dp,
    primaryColor: Color = Color(0xFF00E5FF),
    trackColor: Color = Color(0xFF1E293B),
    showPercentage: Boolean = true
) {
    val progressFraction by remember(currentEnergy, maxThreshold) {
        derivedStateOf {
            if (maxThreshold <= 0) 0f
            else (currentEnergy.toFloat() / maxThreshold.toFloat()).coerceIn(0f, 1f)
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "CultivationRingAnimation"
    )

    val percentageInt by remember(progressFraction) {
        derivedStateOf {
            (progressFraction * 100f).toInt()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("cultivation_progress_ring")
    ) {
        // Subtle ambient aura backing for Xianxia aesthetic
        Box(
            modifier = Modifier
                .size(size * 0.85f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // Material 3 Circular Progress Indicator (Determinate)
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .size(size)
                .testTag("m3_circular_progress_indicator"),
            color = primaryColor,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )

        if (showPercentage) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$percentageInt%",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = if (size < 48.dp) 9.sp else 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor
                )
            }
        }
    }
}
