package com.halidodat.neuralnetworkvisualizer.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.halidodat.neuralnetworkvisualizer.Grid


@Composable
fun CanvasGrid(
    grid: Grid,
    pixelSize: Int,
    gridColor: Color = Color.DarkGray,
    gridLineWeight: Float = 2F,
    modifier: Modifier = Modifier,
    callback: ((grid: Grid, x: Int, y: Int) -> Unit)? = null
) {
    val canvasWidth = pixelSize * grid.width
    val canvasHeight = pixelSize * grid.height

    var state by remember { mutableIntStateOf(0) }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var modifier = modifier
        .padding(4.dp)
        .size(canvasWidth.dp, canvasHeight.dp);
    if (callback != null) {
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    offsetX = offset.x
                    offsetY = offset.y
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y

                    val pixelWidth = this.size.width / grid.width
                    val pixelHeight = this.size.height / grid.height

                    val x = offsetX.toInt() / pixelWidth
                    val y = offsetY.toInt() / pixelHeight

                    if (x >= 0 && x < grid.width && y >= 0 && y < grid.height) {
                        callback(grid, x, y)
                        state += 1;
                    }
                }
            )
        }
    }

    Box(modifier = modifier) {
        key(state) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var width = this.size.width
                var height = this.size.height

                val pixelWidth = this.size.width / grid.width;
                val pixelHeight = this.size.height / grid.height;

                for (y in 0..<grid.height) {
                    for (x in 0..<grid.width) {
                        val value = grid.get(x, y)

                        drawRect(
                            color = Color(value.toInt(), value.toInt(), value.toInt()),
                            topLeft = Offset(x * pixelWidth, y * pixelHeight),
                            size = Size(pixelWidth, pixelHeight),
                        );
                    }
                }

                if (gridLineWeight != 0F) {
                    for (i in 0..grid.width) {
                        //vertical lines
                        drawLine(
                            color = gridColor,
                            strokeWidth = gridLineWeight,
                            start = Offset(i * pixelWidth, 0.0f),
                            end = Offset(i * pixelWidth, width.coerceAtLeast(height))
                        );
                    }
                    for (i in 0..grid.height) {
                        //horizontal lines
                        drawLine(
                            color = gridColor,
                            strokeWidth = gridLineWeight,
                            start = Offset(0.0f, i * pixelHeight),
                            end = Offset(width.coerceAtLeast(height), i * pixelHeight)
                        );
                    }
                }
            }
        }
    }

}
