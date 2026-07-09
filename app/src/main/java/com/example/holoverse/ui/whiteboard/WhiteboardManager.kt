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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class WhiteboardManager {
    private val _elements = mutableStateListOf<WhiteboardElement>()
    val elements: List<WhiteboardElement> = _elements

    var currentTool by mutableStateOf(WhiteboardTool.PEN)
    var currentStyle by mutableStateOf(WhiteboardStyle())

    // Tracks the element currently being drawn
    var currentDrawingElement by mutableStateOf<WhiteboardElement?>(null)
    
    // Tracks where text should be added
    var pendingTextPosition by mutableStateOf<Offset?>(null)
    
    private var startOffset = Offset.Zero
    
    // The size of the screen where drawing happens
    private var sourceSize by mutableStateOf(Size.Zero)

    fun updateSourceSize(size: Size) {
        sourceSize = size
    }

    fun onTouchStart(offset: Offset) {
        startOffset = offset
        if (currentTool == WhiteboardTool.ERASER) {
            eraseAt(offset)
            return
        }

        if (currentTool == WhiteboardTool.TEXT) {
            pendingTextPosition = offset
            return
        }

        currentDrawingElement = when (currentTool) {
            WhiteboardTool.PEN -> {
                val path = Path().apply { moveTo(offset.x, offset.y) }
                WhiteboardElement.Freehand(
                    path,
                    listOf(offset),
                    currentStyle.color,
                    currentStyle.strokeWidth
                )
            }

            WhiteboardTool.ERASER -> null // Handled above

            WhiteboardTool.RECTANGLE -> {
                WhiteboardElement.Rectangle(
                    offset,
                    Size.Zero,
                    currentStyle.color,
                    currentStyle.strokeWidth,
                    currentStyle.isFilled
                )
            }

            WhiteboardTool.CIRCLE -> {
                WhiteboardElement.Circle(
                    offset,
                    0f,
                    currentStyle.color,
                    currentStyle.strokeWidth,
                    currentStyle.isFilled
                )
            }

            WhiteboardTool.LINE -> {
                WhiteboardElement.Line(
                    offset,
                    offset,
                    currentStyle.color,
                    currentStyle.strokeWidth
                )
            }

            WhiteboardTool.ARROW -> {
                WhiteboardElement.Arrow(
                    offset,
                    offset,
                    currentStyle.color,
                    currentStyle.strokeWidth
                )
            }

            WhiteboardTool.TEXT -> null
        }
    }

    fun onTouchMove(offset: Offset) {
        if (currentTool == WhiteboardTool.ERASER) {
            eraseAt(offset)
            return
        }

        val current = currentDrawingElement ?: return
        currentDrawingElement = when (current) {
            is WhiteboardElement.Freehand -> {
                current.path.lineTo(offset.x, offset.y)
                current.copy(points = current.points + offset)
            }

            is WhiteboardElement.Rectangle -> {
                val size = Size(offset.x - current.topLeft.x, offset.y - current.topLeft.y)
                current.copy(size = size)
            }

            is WhiteboardElement.Circle -> {
                val dx = offset.x - current.center.x
                val dy = offset.y - current.center.y
                val radius = kotlin.math.sqrt(dx * dx + dy * dy)
                current.copy(radius = radius)
            }

            is WhiteboardElement.Line -> {
                current.copy(end = offset)
            }

            is WhiteboardElement.Arrow -> {
                current.copy(end = offset)
            }

            is WhiteboardElement.Text -> {
                current // Text doesn't update on move currently
            }
        }
    }

    fun onTouchEnd() {
        currentDrawingElement?.let {
            synchronized(_elements) {
                _elements.add(it)
            }
        }
        currentDrawingElement = null
    }

    fun addText(text: String) {
        val position = pendingTextPosition ?: return
        synchronized(_elements) {
            _elements.add(
                WhiteboardElement.Text(
                    text,
                    position,
                    currentStyle.color,
                    currentStyle.strokeWidth * 10f // Scale stroke width to font size
                )
            )
        }
        pendingTextPosition = null
    }

    private fun eraseAt(offset: Offset) {
        val eraserRadius = currentStyle.strokeWidth * 4 // Eraser area
        synchronized(_elements) {
            val toRemove = _elements.filter { element ->
                intersects(element, offset, eraserRadius)
            }
            _elements.removeAll(toRemove)
        }
    }

    private fun intersects(element: WhiteboardElement, point: Offset, radius: Float): Boolean {
        return when (element) {
            is WhiteboardElement.Freehand -> {
                element.points.any { p ->
                    val dx = p.x - point.x
                    val dy = p.y - point.y
                    kotlin.math.sqrt(dx * dx + dy * dy) <= radius + element.strokeWidth / 2
                }
            }

            is WhiteboardElement.Rectangle -> {
                val left = kotlin.math.min(element.topLeft.x, element.topLeft.x + element.size.width)
                val right = kotlin.math.max(element.topLeft.x, element.topLeft.x + element.size.width)
                val top = kotlin.math.min(element.topLeft.y, element.topLeft.y + element.size.height)
                val bottom = kotlin.math.max(element.topLeft.y, element.topLeft.y + element.size.height)
                
                val rect = android.graphics.RectF(left, top, right, bottom)
                // Inset by negative radius to check proximity
                rect.inset(-radius, -radius)
                rect.contains(point.x, point.y)
            }

            is WhiteboardElement.Circle -> {
                val dx = point.x - element.center.x
                val dy = point.y - element.center.y
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                distance <= element.radius + radius
            }

            is WhiteboardElement.Line -> {
                distancePointToLine(point, element.start, element.end) <= radius + element.strokeWidth / 2
            }

            is WhiteboardElement.Arrow -> {
                distancePointToLine(point, element.start, element.end) <= radius + element.strokeWidth / 2
            }

            is WhiteboardElement.Text -> {
                // Approximate bounding box for text
                val textLength = element.text.length * element.fontSize * 0.6f
                val rect = android.graphics.RectF(
                    element.position.x,
                    element.position.y - element.fontSize,
                    element.position.x + textLength,
                    element.position.y
                )
                rect.inset(-radius, -radius)
                rect.contains(point.x, point.y)
            }
        }
    }

    private fun distancePointToLine(p: Offset, a: Offset, b: Offset): Float {
        val l2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
        if (l2 == 0f) return kotlin.math.sqrt((p.x - a.x) * (p.x - a.x) + (p.y - a.y) * (p.y - a.y))
        var t = ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2
        t = kotlin.math.max(0f, kotlin.math.min(1f, t))
        val projectionX = a.x + t * (b.x - a.x)
        val projectionY = a.y + t * (b.y - a.y)
        return kotlin.math.sqrt((p.x - projectionX) * (p.x - projectionX) + (p.y - projectionY) * (p.y - projectionY))
    }

    fun clear() {
        synchronized(_elements) {
            _elements.clear()
        }
        currentDrawingElement = null
    }

    fun undo() {
        synchronized(_elements) {
            if (_elements.isNotEmpty()) {
                _elements.removeAt(_elements.size - 1)
            }
        }
    }

    fun draw(drawScope: DrawScope) {
        val allElements = synchronized(_elements) { 
            _elements.toList() + listOfNotNull(currentDrawingElement)
        }
        allElements.forEach { element ->
            drawElement(drawScope, element)
        }
    }

    private fun drawElement(drawScope: DrawScope, element: WhiteboardElement) {
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

            is WhiteboardElement.Text -> {
                drawScope.drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = element.color.toArgb()
                        textSize = element.fontSize
                        isAntiAlias = true
                    }
                    drawText(element.text, element.position.x, element.position.y, paint)
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

        val allElements = synchronized(_elements) { 
            _elements.toList() + listOfNotNull(currentDrawingElement)
        }
        allElements.forEach { element ->
            drawElementToNative(canvas, element, paint)
        }
        canvas.restore()
    }

    private fun drawElementToNative(canvas: android.graphics.Canvas, element: WhiteboardElement, paint: android.graphics.Paint) {
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

            is WhiteboardElement.Text -> {
                paint.color = element.color.toArgb()
                paint.textSize = element.fontSize
                paint.style = android.graphics.Paint.Style.FILL
                canvas.drawText(element.text, element.position.x, element.position.y, paint)
            }
        }
    }
}
