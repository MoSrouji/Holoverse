package com.example.holoverse.ui.whiteboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class WhiteboardManager {
    private val _elements = mutableStateListOf<WhiteboardElement>()
    val elements: List<WhiteboardElement> = _elements

    var currentTool by mutableStateOf(WhiteboardTool.PEN)
    var currentStyle by mutableStateOf(WhiteboardStyle())

    private var currentPath = Path()
    private var startOffset = Offset.Zero

    fun onTouchStart(offset: Offset) {
        startOffset = offset
        when (currentTool) {
            WhiteboardTool.PEN -> {
                currentPath = Path().apply { moveTo(offset.x, offset.y) }
                _elements.add(WhiteboardElement.Freehand(currentPath, currentStyle.color, currentStyle.strokeWidth))
            }
            WhiteboardTool.ERASER -> {
                // Eraser could be implemented as a path with the background color or by removing elements
                // For simplicity, let's treat it as a white pen for now
                currentPath = Path().apply { moveTo(offset.x, offset.y) }
                _elements.add(WhiteboardElement.Freehand(currentPath, Color.White, currentStyle.strokeWidth * 2))
            }
            WhiteboardTool.RECTANGLE -> {
                _elements.add(WhiteboardElement.Rectangle(offset, Size.Zero, currentStyle.color, currentStyle.strokeWidth, currentStyle.isFilled))
            }
            WhiteboardTool.CIRCLE -> {
                _elements.add(WhiteboardElement.Circle(offset, 0f, currentStyle.color, currentStyle.strokeWidth, currentStyle.isFilled))
            }
            WhiteboardTool.LINE -> {
                _elements.add(WhiteboardElement.Line(offset, offset, currentStyle.color, currentStyle.strokeWidth))
            }
            WhiteboardTool.ARROW -> {
                _elements.add(WhiteboardElement.Arrow(offset, offset, currentStyle.color, currentStyle.strokeWidth))
            }
        }
    }

    fun onTouchMove(offset: Offset) {
        if (_elements.isEmpty()) return
        val lastIndex = _elements.size - 1
        when (val last = _elements[lastIndex]) {
            is WhiteboardElement.Freehand -> {
                last.path.lineTo(offset.x, offset.y)
                // Force recomposition
                _elements[lastIndex] = last.copy()
            }
            is WhiteboardElement.Rectangle -> {
                val size = Size(offset.x - last.topLeft.x, offset.y - last.topLeft.y)
                _elements[lastIndex] = last.copy(size = size)
            }
            is WhiteboardElement.Circle -> {
                val dx = offset.x - last.center.x
                val dy = offset.y - last.center.y
                val radius = kotlin.math.sqrt(dx * dx + dy * dy)
                _elements[lastIndex] = last.copy(radius = radius)
            }
            is WhiteboardElement.Line -> {
                _elements[lastIndex] = last.copy(end = offset)
            }
            is WhiteboardElement.Arrow -> {
                _elements[lastIndex] = last.copy(end = offset)
            }
        }
    }

    fun clear() {
        _elements.clear()
    }

    fun undo() {
        if (_elements.isNotEmpty()) {
            _elements.removeAt(_elements.size - 1)
        }
    }

    fun draw(drawScope: DrawScope) {
        elements.forEach { element ->
            when (element) {
                is WhiteboardElement.Freehand -> {
                    drawScope.drawPath(
                        path = element.path,
                        color = element.color,
                        style = Stroke(width = element.strokeWidth)
                    )
                }
                is WhiteboardElement.Rectangle -> {
                    drawScope.drawRect(
                        color = element.color,
                        topLeft = element.topLeft,
                        size = element.size,
                        style = if (element.isFilled) Fill else Stroke(width = element.strokeWidth)
                    )
                }
                is WhiteboardElement.Circle -> {
                    drawScope.drawCircle(
                        color = element.color,
                        center = element.center,
                        radius = element.radius,
                        style = if (element.isFilled) Fill else Stroke(width = element.strokeWidth)
                    )
                }
                is WhiteboardElement.Line -> {
                    drawScope.drawLine(
                        color = element.color,
                        start = element.start,
                        end = element.end,
                        strokeWidth = element.strokeWidth
                    )
                }
                is WhiteboardElement.Arrow -> {
                    drawArrow(drawScope, element)
                }
            }
        }
    }

    private fun drawArrow(drawScope: DrawScope, arrow: WhiteboardElement.Arrow) {
        drawScope.drawLine(
            color = arrow.color,
            start = arrow.start,
            end = arrow.end,
            strokeWidth = arrow.strokeWidth
        )
        val angle = atan2(arrow.end.y - arrow.start.y, arrow.end.x - arrow.start.x)
        val headLength = 20f
        val headAngle = Math.PI / 6

        val x1 = arrow.end.x - headLength * cos(angle - headAngle).toFloat()
        val y1 = arrow.end.y - headLength * sin(angle - headAngle).toFloat()
        val x2 = arrow.end.x - headLength * cos(angle + headAngle).toFloat()
        val y2 = arrow.end.y - headLength * sin(angle + headAngle).toFloat()

        drawScope.drawLine(color = arrow.color, start = arrow.end, end = Offset(x1, y1), strokeWidth = arrow.strokeWidth)
        drawScope.drawLine(color = arrow.color, start = arrow.end, end = Offset(x2, y2), strokeWidth = arrow.strokeWidth)
    }

    /**
     * Draws to a NativeCanvas (used for WebRTC background rendering)
     */
    fun drawToNativeCanvas(canvas: android.graphics.Canvas) {
        // Clear background
        canvas.drawColor(android.graphics.Color.WHITE)
        
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }

        elements.forEach { element ->
            when (element) {
                is WhiteboardElement.Freehand -> {
                    paint.color = element.color.toArgb()
                    paint.strokeWidth = element.strokeWidth
                    paint.style = android.graphics.Paint.Style.STROKE
                    canvas.drawPath(element.path.asAndroidPath(), paint)
                }
                is WhiteboardElement.Rectangle -> {
                    paint.color = element.color.toArgb()
                    paint.strokeWidth = element.strokeWidth
                    paint.style = if (element.isFilled) android.graphics.Paint.Style.FILL else android.graphics.Paint.Style.STROKE
                    canvas.drawRect(
                        element.topLeft.x,
                        element.topLeft.y,
                        element.topLeft.x + element.size.width,
                        element.topLeft.y + element.size.height,
                        paint
                    )
                }
                is WhiteboardElement.Circle -> {
                    paint.color = element.color.toArgb()
                    paint.strokeWidth = element.strokeWidth
                    paint.style = if (element.isFilled) android.graphics.Paint.Style.FILL else android.graphics.Paint.Style.STROKE
                    canvas.drawCircle(element.center.x, element.center.y, element.radius, paint)
                }
                is WhiteboardElement.Line -> {
                    paint.color = element.color.toArgb()
                    paint.strokeWidth = element.strokeWidth
                    paint.style = android.graphics.Paint.Style.STROKE
                    canvas.drawLine(element.start.x, element.start.y, element.end.x, element.end.y, paint)
                }
                is WhiteboardElement.Arrow -> {
                    paint.color = element.color.toArgb()
                    paint.strokeWidth = element.strokeWidth
                    paint.style = android.graphics.Paint.Style.STROKE
                    canvas.drawLine(element.start.x, element.start.y, element.end.x, element.end.y, paint)
                    // Draw arrow head
                    val angle = atan2(element.end.y - element.start.y, element.end.x - element.start.x)
                    val headLength = 20f
                    val headAngle = Math.PI / 6
                    val x1 = element.end.x - headLength * cos(angle - headAngle).toFloat()
                    val y1 = element.end.y - headLength * sin(angle - headAngle).toFloat()
                    val x2 = element.end.x - headLength * cos(angle + headAngle).toFloat()
                    val y2 = element.end.y - headLength * sin(angle + headAngle).toFloat()
                    canvas.drawLine(element.end.x, element.end.y, x1, y1, paint)
                    canvas.drawLine(element.end.x, element.end.y, x2, y2, paint)
                }
            }
        }
    }
}

