package com.example.holoverse.ui.whiteboard

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhiteboardManagerTest {

    @Test
    fun `test initial state is empty`() {
        val manager = WhiteboardManager()
        assertTrue(manager.elements.isEmpty())
        assertEquals(WhiteboardTool.PEN, manager.currentTool)
    }

    @Test
    fun `test adding freehand element`() {
        val manager = WhiteboardManager()
        manager.onTouchStart(Offset(10f, 10f))
        // Element is in currentDrawingElement, not yet in elements list
        assertTrue(manager.currentDrawingElement is WhiteboardElement.Freehand)
        assertEquals(0, manager.elements.size)
        
        manager.onTouchMove(Offset(20f, 20f))
        manager.onTouchEnd()
        
        assertEquals(1, manager.elements.size)
        assertTrue(manager.elements[0] is WhiteboardElement.Freehand)
    }

    @Test
    fun `test adding rectangle element`() {
        val manager = WhiteboardManager()
        manager.currentTool = WhiteboardTool.RECTANGLE
        manager.onTouchStart(Offset(0f, 0f))
        manager.onTouchMove(Offset(100f, 100f))
        manager.onTouchEnd()
        
        val rect = manager.elements[0] as WhiteboardElement.Rectangle
        assertEquals(0f, rect.topLeft.x)
        assertEquals(100f, rect.size.width)
    }

    @Test
    fun `test undo and clear`() {
        val manager = WhiteboardManager()
        manager.onTouchStart(Offset(0f, 0f))
        manager.onTouchEnd()
        manager.onTouchStart(Offset(10f, 10f))
        manager.onTouchEnd()
        assertEquals(2, manager.elements.size)
        
        manager.undo()
        assertEquals(1, manager.elements.size)
        
        manager.clear()
        assertTrue(manager.elements.isEmpty())
    }

    @Test
    fun `test eraser removes elements`() {
        val manager = WhiteboardManager()
        // Add a rectangle
        manager.currentTool = WhiteboardTool.RECTANGLE
        manager.onTouchStart(Offset(0f, 0f))
        manager.onTouchMove(Offset(100f, 100f))
        manager.onTouchEnd()
        assertEquals(1, manager.elements.size)

        // Erase it
        manager.currentTool = WhiteboardTool.ERASER
        manager.onTouchStart(Offset(50f, 50f))
        assertEquals(0, manager.elements.size)
    }

    @Test
    fun `test adding text element`() {
        val manager = WhiteboardManager()
        manager.currentTool = WhiteboardTool.TEXT
        val pos = Offset(100f, 100f)
        manager.onTouchStart(pos)
        
        assertEquals(pos, manager.pendingTextPosition)
        
        manager.addText("Hello")
        
        assertEquals(1, manager.elements.size)
        val textElement = manager.elements[0] as WhiteboardElement.Text
        assertEquals("Hello", textElement.text)
        assertEquals(pos, textElement.position)
        assertEquals(null, manager.pendingTextPosition)
    }

    @Test
    fun `test erasing text element`() {
        val manager = WhiteboardManager()
        manager.currentTool = WhiteboardTool.TEXT
        manager.onTouchStart(Offset(100f, 100f))
        manager.addText("Hello")
        assertEquals(1, manager.elements.size)

        // Erase it
        manager.currentTool = WhiteboardTool.ERASER
        manager.onTouchStart(Offset(110f, 95f)) // Within text bounding box
        assertEquals(0, manager.elements.size)
    }
}
