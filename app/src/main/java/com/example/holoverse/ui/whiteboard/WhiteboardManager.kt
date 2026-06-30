package com.example.holoverse.ui.whiteboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
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
    
    // The size of the screen where drawing happens
    private var sourceSize by mutableStateOf(Size.Zero)

    fun updateSourceSize(size: Size) {
        sourceSize = size
    }

    fun onTouchStart(offset: Offset) {
        startOffset = offset
        synchronized(_elements) {
            when (currentTool) {
                WhiteboardTool.PEN -> {
                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                    _elements.add(
                        WhiteboardElement.Freehand(
                            currentPath,
                            currentStyle.color,
                            currentStyle.strokeWidth
                        )
                    )
                }

                WhiteboardTool.ERASER -> {
                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                    _elements.add(
                        WhiteboardElement.Freehand(
                            currentPath,
                            Color.White,
                            currentStyle.strokeWidth * 2
                        )
                    )
                }

                WhiteboardTool.RECTANGLE -> {
                    _elements.add(
                        WhiteboardElement.Rectangle(
                            offset,
                            Size.Zero,
                            currentStyle.color,
                            currentStyle.strokeWidth,
                            currentStyle.isFilled
                        )
                    )
                }

                WhiteboardTool.CIRCLE -> {
                    _elements.add(
                        WhiteboardElement.Circle(
                            offset,
                            0f,
                            currentStyle.color,
                            currentStyle.strokeWidth,
                            currentStyle.isFilled
                        )
                    )
                }

                WhiteboardTool.LINE -> {
                    _elements.add(
                        WhiteboardElement.Line(
                            offset,
                            offset,
                            currentStyle.color,
                            currentStyle.strokeWidth
                        )
                    )
                }

                WhiteboardTool.ARROW -> {
                    _elements.add(
                        WhiteboardElement.Arrow(
                            offset,
                            offset,
                            currentStyle.color,
                            currentStyle.strokeWidth
                        )
                    )
                }
            }
        }
    }

    fun onTouchMove(offset: Offset) {
        synchronized(_elements) {
            if (_elements.isEmpty()) return
            val lastIndex = _elements.size - 1
            when (val last = _elements[lastIndex]) {
                is WhiteboardElement.Freehand -> {
                    last.path.lineTo(offset.x, offset.y)
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
    }

    fun clear() {
        synchronized(_elements) {
            _elements.clear()
        }
    }

    fun undo() {
        synchronized(_elements) {
            if (_elements.isNotEmpty()) {
                _elements.removeAt(_elements.size - 1)
            }
        }
    }

    fun draw(drawScope: DrawScope) {
        val elementsCopy = synchronized(_elements) { _elements.toList() }
        elementsCopy.forEach { element ->
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

        drawScope.drawLine(
            color = arrow.color,
            start = arrow.end,
            end = Offset(x1, y1),
            strokeWidth = arrow.strokeWidth
        )
        drawScope.drawLine(
            color = arrow.color,
            start = arrow.end,
            end = Offset(x2, y2),
            strokeWidth = arrow.strokeWidth
        )
    }

    fun drawToNativeCanvas(canvas: android.graphics.Canvas, clearBackground: Boolean = true) {
        if (clearBackground) {
            canvas.drawColor(android.graphics.Color.WHITE)
        }

        if (sourceSize.width <= 0f || sourceSize.height <= 0f) return

        val targetWidth = canvas.width.toFloat()
        val targetHeight = canvas.height.toFloat()

        val scaleX = targetWidth / sourceSize.width
        val scaleY = targetHeight / sourceSize.height
        val scale = kotlin.math.min(scaleX, scaleY)

        val dx = (targetWidth - sourceSize.width * scale) / 2f
        val dy = (targetHeight - sourceSize.height * scale) / 2f

        canvas.save()
        canvas.translate(dx, dy)
        canvas.scale(scale, scale)

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }

        val elementsCopy = synchronized(_elements) { _elements.toList() }
        elementsCopy.forEach { element ->
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
                    paint.style =
                        if (element.isFilled) android.graphics.Paint.Style.FILL else android.graphics.Paint.Style.STROKE
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
                    paint.style =
                        if (element.isFilled) android.graphics.Paint.Style.FILL else android.graphics.Paint.Style.STROKE
                    canvas.drawCircle(element.center.x, element.center.y, element.radius, paint)
                }

                is WhiteboardElement.Line -> {
                    paint.color = element.color.toArgb()
                    paint.strokeWidth = element.strokeWidth
                    paint.style = android.graphics.Paint.Style.STROKE
                    canvas.drawLine(
                        element.start.x,
                        element.start.y,
                        element.end.x,
                        element.end.y,
                        paint
                    )
                }

                is WhiteboardElement.Arrow -> {
                    paint.color = element.color.toArgb()
                    paint.strokeWidth = element.strokeWidth
                    paint.style = android.graphics.Paint.Style.STROKE
                    canvas.drawLine(
                        element.start.x,
                        element.start.y,
                        element.end.x,
                        element.end.y,
                        paint
                    )
                    val angle =
                        atan2(element.end.y - element.start.y, element.end.x - element.start.x)
                    val headLength = 20f
                    val headAngle = Math.PI / 6
                    val x1 = element.end.x - headLength * kotlin.math.cos(angle - headAngle).toFloat()
                    val y1 = element.end.y - headLength * kotlin.math.sin(angle - headAngle).toFloat()
                    val x2 = element.end.x - headLength * kotlin.math.cos(angle + headAngle).toFloat()
                    val y2 = element.end.y - headLength * kotlin.math.sin(angle + headAngle).toFloat()
                    canvas.drawLine(element.end.x, element.end.y, x1, y1, paint)
                    canvas.drawLine(element.end.x, element.end.y, x2, y2, paint)
                }
            }
        }
        canvas.restore()
    }
}
