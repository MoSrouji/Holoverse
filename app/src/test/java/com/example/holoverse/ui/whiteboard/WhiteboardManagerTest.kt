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
        assertEquals(1, manager.elements.size)
        assertTrue(manager.elements[0] is WhiteboardElement.Freehand)
        
        manager.onTouchMove(Offset(20f, 20f))
        assertEquals(1, manager.elements.size)
    }

    @Test
    fun `test adding rectangle element`() {
        val manager = WhiteboardManager()
        manager.currentTool = WhiteboardTool.RECTANGLE
        manager.onTouchStart(Offset(0f, 0f))
        manager.onTouchMove(Offset(100f, 100f))
        
        val rect = manager.elements[0] as WhiteboardElement.Rectangle
        assertEquals(0f, rect.topLeft.x)
        assertEquals(100f, rect.size.width)
    }

    @Test
    fun `test undo and clear`() {
        val manager = WhiteboardManager()
        manager.onTouchStart(Offset(0f, 0f))
        manager.onTouchStart(Offset(10f, 10f))
        assertEquals(2, manager.elements.size)
        
        manager.undo()
        assertEquals(1, manager.elements.size)
        
        manager.clear()
        assertTrue(manager.elements.isEmpty())
    }
}
