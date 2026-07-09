package com.example.holoverse.ui.whiteboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

sealed class WhiteboardElement {
    data class Freehand(
        val path: Path,
        val points: List<Offset>,
        val color: Color,
        val strokeWidth: Float
    ) : WhiteboardElement()

    data class Rectangle(
        val topLeft: Offset,
        val size: Size,
        val color: Color,
        val strokeWidth: Float,
        val isFilled: Boolean = false
    ) : WhiteboardElement()

    data class Circle(
        val center: Offset,
        val radius: Float,
        val color: Color,
        val strokeWidth: Float,
        val isFilled: Boolean = false
    ) : WhiteboardElement()

    data class Line(
        val start: Offset,
        val end: Offset,
        val color: Color,
        val strokeWidth: Float
    ) : WhiteboardElement()

    data class Arrow(
        val start: Offset,
        val end: Offset,
        val color: Color,
        val strokeWidth: Float
    ) : WhiteboardElement()

    data class Text(
        val text: String,
        val position: Offset,
        val color: Color,
        val fontSize: Float
    ) : WhiteboardElement()
}

enum class WhiteboardTool {
    PEN, ERASER, RECTANGLE, CIRCLE, LINE, ARROW, TEXT
}

data class WhiteboardStyle(
    val color: Color = Color.Black,
    val strokeWidth: Float = 5f,
    val isFilled: Boolean = false
)
