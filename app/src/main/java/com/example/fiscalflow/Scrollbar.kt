package com.example.fiscalflow

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt

private val GutterColor = Color(0xFFCBD5E1)
private val TrackColor = Color(0xFF94A3B8)
private val ThumbColor = Color(0xFF172A46)
private val ArrowColor = Color(0xFF172A46)
private val BarShape = RoundedCornerShape(50)
val ScrollBarWidth = 24.dp

class AppScrollbarHost {
    var column by mutableStateOf<ScrollState?>(null)
    var list by mutableStateOf<LazyListState?>(null)
    var grid by mutableStateOf<LazyGridState?>(null)
}

val LocalAppScrollbarHost = staticCompositionLocalOf<AppScrollbarHost> {
    error("AppScrollbarHost is missing")
}

@Composable
fun ProvideAppScrollbar(content: @Composable () -> Unit) {
    val host = remember { AppScrollbarHost() }
    val fallback = rememberScrollState()
    CompositionLocalProvider(LocalAppScrollbarHost provides host) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F0FA))
        ) {
            FiscalFlowBubbles()
            Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                content()
            }
            val barModifier = Modifier.fillMaxHeight()
            val listState = host.list
            val gridState = host.grid
            val columnState = host.column
            when {
                listState != null -> AppVerticalScrollbar(
                    listState = listState,
                    modifier = barModifier
                )
                gridState != null -> AppVerticalScrollbar(
                    gridState = gridState,
                    modifier = barModifier
                )
                else -> AppVerticalScrollbar(
                    scrollState = columnState ?: fallback,
                    modifier = barModifier
                )
            }
            }
        }
    }
}

@Composable
fun RegisterColumnScrollbar(state: ScrollState) {
    val host = LocalAppScrollbarHost.current
    DisposableEffect(state) {
        host.column = state
        onDispose {
            if (host.column === state) host.column = null
        }
    }
}

@Composable
fun RegisterListScrollbar(state: LazyListState) {
    val host = LocalAppScrollbarHost.current
    DisposableEffect(state) {
        host.list = state
        onDispose {
            if (host.list === state) host.list = null
        }
    }
}

@Composable
fun RegisterGridScrollbar(state: LazyGridState) {
    val host = LocalAppScrollbarHost.current
    DisposableEffect(state) {
        host.grid = state
        onDispose {
            if (host.grid === state) host.grid = null
        }
    }
}

@Composable
fun ScrollableScreen(
    modifier: Modifier = Modifier,
    fillViewport: Boolean = false,
    applyImePadding: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    RegisterColumnScrollbar(scrollState)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .then(if (applyImePadding) Modifier.imePadding() else Modifier)
    ) {
        val viewportHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .then(if (fillViewport) Modifier.heightIn(min = viewportHeight) else Modifier)
                .padding(contentPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

@Composable
fun AppVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val max = scrollState.maxValue.coerceAtLeast(0)
    val fraction = if (max == 0) 0f else scrollState.value / max.toFloat()
    val scope = rememberCoroutineScope()

    SlimScrollbar(
        modifier = modifier,
        scrollable = scrollState,
        thumbFraction = fraction,
        thumbSizeFraction = if (max == 0) 1f else (1f / (1f + max / 800f)).coerceIn(0.2f, 1f),
        onScrollFraction = { frac ->
            scope.launch {
                scrollState.scrollTo((frac * max).roundToInt().coerceIn(0, max))
            }
        }
    )
}

@Composable
fun AppVerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val info = listState.layoutInfo
    val visible = info.visibleItemsInfo
    val total = info.totalItemsCount
    val viewport = (info.viewportEndOffset - info.viewportStartOffset).coerceAtLeast(1)
    val avgSize = if (visible.isNotEmpty()) {
        visible.map { it.size }.average().toFloat().coerceAtLeast(1f)
    } else {
        1f
    }
    val estimatedContent = (avgSize * total).coerceAtLeast(viewport.toFloat())
    val maxScroll = (estimatedContent - viewport).coerceAtLeast(1f)
    val scrolled = listState.firstVisibleItemIndex * avgSize +
        listState.firstVisibleItemScrollOffset
    val fraction = (scrolled / maxScroll).coerceIn(0f, 1f)
    val thumbSize = (viewport / estimatedContent).coerceIn(0.2f, 1f)
    val scope = rememberCoroutineScope()

    SlimScrollbar(
        modifier = modifier,
        scrollable = listState,
        thumbFraction = fraction,
        thumbSizeFraction = thumbSize,
        onScrollFraction = { frac ->
            scope.launch {
                val target = frac * maxScroll
                val index = (target / avgSize).toInt()
                    .coerceIn(0, (total - 1).coerceAtLeast(0))
                val offset = (target - index * avgSize).roundToInt().coerceAtLeast(0)
                listState.scrollToItem(index, offset)
            }
        }
    )
}

@Composable
fun AppVerticalScrollbar(
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    columns: Int = 2
) {
    val info = gridState.layoutInfo
    val visible = info.visibleItemsInfo
    val total = info.totalItemsCount
    val viewport = (info.viewportEndOffset - info.viewportStartOffset).coerceAtLeast(1)
    val avgHeight = if (visible.isNotEmpty()) {
        visible.map { it.size.height }.average().toFloat().coerceAtLeast(1f)
    } else {
        1f
    }
    val rowCount = ceil(total / columns.coerceAtLeast(1).toFloat())
    val estimatedContent = (rowCount * avgHeight).coerceAtLeast(viewport.toFloat())
    val maxScroll = (estimatedContent - viewport).coerceAtLeast(1f)
    val row = gridState.firstVisibleItemIndex / columns.coerceAtLeast(1)
    val scrolled = row * avgHeight + gridState.firstVisibleItemScrollOffset
    val fraction = (scrolled / maxScroll).coerceIn(0f, 1f)
    val thumbSize = (viewport / estimatedContent).coerceIn(0.2f, 1f)
    val scope = rememberCoroutineScope()

    SlimScrollbar(
        modifier = modifier,
        scrollable = gridState,
        thumbFraction = fraction,
        thumbSizeFraction = thumbSize,
        onScrollFraction = { frac ->
            scope.launch {
                val target = frac * maxScroll
                val targetRow = (target / avgHeight).toInt().coerceAtLeast(0)
                val index = (targetRow * columns).coerceIn(0, (total - 1).coerceAtLeast(0))
                val offset = (target - targetRow * avgHeight).roundToInt().coerceAtLeast(0)
                gridState.scrollToItem(index, offset)
            }
        }
    )
}

@Composable
private fun SlimScrollbar(
    scrollable: ScrollableState,
    thumbFraction: Float,
    thumbSizeFraction: Float,
    onScrollFraction: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val step = with(density) { 160.dp.toPx() }
    var dragging by remember { mutableStateOf(false) }
    val dragY = remember { mutableFloatStateOf(0f) }

    Row(
        modifier = modifier
            .width(ScrollBarWidth)
            .fillMaxHeight()
            .background(GutterColor)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(ThumbColor)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        ScrollArrow(
            icon = Icons.Filled.ArrowDropUp,
            contentDescription = "Scroll up",
            onClick = { scope.launch { scrollable.animateScrollBy(-step) } }
        )

        BoxWithConstraints(
            modifier = Modifier
                .width(12.dp)
                .weight(1f)
                .clip(BarShape)
                .background(TrackColor)
        ) {
            val trackH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
            val minThumb = with(density) { 28.dp.toPx() }
            val thumbH = (trackH * thumbSizeFraction.coerceIn(0.2f, 1f))
                .coerceAtLeast(minThumb)
                .coerceAtMost(trackH)
            val travel = (trackH - thumbH).coerceAtLeast(0f)
            val composedY = thumbFraction.coerceIn(0f, 1f) * travel
            if (!dragging) {
                dragY.floatValue = composedY
            }
            val thumbY = dragY.floatValue

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(travel, thumbH) {
                        detectTapGestures { offset ->
                            if (travel <= 0f) return@detectTapGestures
                            val frac = ((offset.y - thumbH / 2f) / travel).coerceIn(0f, 1f)
                            onScrollFraction(frac)
                        }
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, thumbY.roundToInt()) }
                    .width(12.dp)
                    .height(with(density) { thumbH.toDp() })
                    .clip(BarShape)
                    .background(ThumbColor)
                    .pointerInput(travel) {
                        detectDragGestures(
                            onDragStart = { dragging = true },
                            onDragEnd = { dragging = false },
                            onDragCancel = { dragging = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (travel <= 0f) return@detectDragGestures
                                val nextY = (dragY.floatValue + dragAmount.y)
                                    .coerceIn(0f, travel)
                                dragY.floatValue = nextY
                                onScrollFraction(nextY / travel)
                            }
                        )
                    }
            )
        }

        ScrollArrow(
            icon = Icons.Filled.ArrowDropDown,
            contentDescription = "Scroll down",
            onClick = { scope.launch { scrollable.animateScrollBy(step) } }
        )
        }
    }
}

@Composable
private fun ScrollArrow(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(pressed) {
        if (pressed) {
            onClick()
            delay(360)
            while (true) {
                onClick()
                delay(70)
            }
        }
    }

    Box(
        modifier = Modifier
            .size(18.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = ArrowColor,
            modifier = Modifier.size(18.dp)
        )
    }
}
