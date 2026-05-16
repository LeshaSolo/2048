package com.example.noblenumbers.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.example.noblenumbers.game.model.MoveDirection
import kotlin.math.abs

fun Modifier.nobleSwipeInput(
    onSwipe: (MoveDirection) -> Unit,
    swipeThresholdPx: Float,
): Modifier = pointerInput(onSwipe, swipeThresholdPx) {
    val dragOffsetState = mutableStateOf(Offset.Zero)
    detectDragGestures(
        onDragStart = { dragOffsetState.value = Offset.Zero },
        onDrag = { change, dragAmount ->
            change.consume()
            dragOffsetState.value += dragAmount
        },
        onDragEnd = {
            val dragOffset = dragOffsetState.value
            val x = dragOffset.x
            val y = dragOffset.y
            if (maxOf(abs(x), abs(y)) >= swipeThresholdPx) {
                onSwipe(
                    if (abs(x) > abs(y)) {
                        if (x > 0) MoveDirection.Right else MoveDirection.Left
                    } else {
                        if (y > 0) MoveDirection.Down else MoveDirection.Up
                    },
                )
            }
            dragOffsetState.value = Offset.Zero
        },
        onDragCancel = { dragOffsetState.value = Offset.Zero },
    )
}
