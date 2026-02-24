package com.rafario.lahrecetah.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.positionAwareImePadding() = composed {
    var consumePadding by remember { mutableIntStateOf(0) }
    onGloballyPositioned { coordinates ->
        val rootCoordinate = coordinates.findRootCoordinates()
        val bottom = coordinates.positionInWindow().y + coordinates.size.height

        consumePadding = (rootCoordinate.size.height - bottom).toInt()
    }
        .consumeWindowInsets(PaddingValues(bottom = (consumePadding / LocalDensity.current.density).dp))
        .imePadding()
}

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 6.dp,
    cornerRadius: Dp = 20.dp,
    on: Dp = 10.dp,
    off: Dp = 6.dp
): Modifier = this.then(
    Modifier.drawBehind {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(on.toPx(), off.toPx())
            )
        )

        drawRoundRect(
            color = color,
            size = size,
            style = stroke,
            cornerRadius = CornerRadius(
                cornerRadius.toPx(),
                cornerRadius.toPx()
            )
        )
    }
)