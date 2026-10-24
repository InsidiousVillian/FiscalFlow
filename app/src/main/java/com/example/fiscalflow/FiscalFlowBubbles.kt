package com.example.fiscalflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val BubbleNavy = Color(0xFF1E3A8A)
private val BubblePale = Color(0xFF9DB4E8)
private val BubbleMid = Color(0xFF5B7FD6)

@Composable
fun FiscalFlowBubbles() {
    Box(modifier = Modifier.fillMaxSize()) {
        Bubble(160.dp, BubbleNavy, Alignment.TopStart, -70.dp, -55.dp)
        Bubble(48.dp, BubbleNavy, Alignment.TopStart, 28.dp, 118.dp)
        Bubble(28.dp, BubbleNavy.copy(alpha = 0.85f), Alignment.TopStart, 86.dp, 42.dp)
        Bubble(200.dp, BubbleNavy, Alignment.TopEnd, 70.dp, -80.dp)
        Bubble(36.dp, Color.White.copy(alpha = 0.92f), Alignment.TopEnd, -18.dp, 96.dp)
        Bubble(52.dp, Color.White.copy(alpha = 0.9f), Alignment.CenterStart, 10.dp, -70.dp)
        Bubble(22.dp, Color.White.copy(alpha = 0.75f), Alignment.CenterEnd, -28.dp, -20.dp)
        Bubble(90.dp, BubblePale.copy(alpha = 0.45f), Alignment.CenterStart, -28.dp, 70.dp)
        Bubble(34.dp, BubbleMid.copy(alpha = 0.55f), Alignment.CenterEnd, 8.dp, 90.dp)
        Bubble(64.dp, BubbleMid.copy(alpha = 0.55f), Alignment.BottomStart, 36.dp, -110.dp)
        Bubble(220.dp, BubblePale.copy(alpha = 0.40f), Alignment.BottomEnd, 80.dp, 70.dp)
        Bubble(26.dp, Color.White.copy(alpha = 0.7f), Alignment.BottomStart, 18.dp, -48.dp)
        Bubble(18.dp, BubbleNavy.copy(alpha = 0.35f), Alignment.BottomEnd, -70.dp, -160.dp)
    }
}

@Composable
private fun BoxScope.Bubble(
    size: Dp,
    color: Color,
    alignment: Alignment,
    x: Dp,
    y: Dp
) {
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(x = x, y = y)
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}
