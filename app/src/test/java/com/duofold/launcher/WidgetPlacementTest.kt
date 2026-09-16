package com.duofold.launcher

import com.duofold.launcher.model.DropZone
import com.duofold.launcher.model.HomeLayout
import com.duofold.launcher.model.LayoutEditing
import com.duofold.launcher.model.WidgetPlacement
import com.duofold.launcher.model.firstFreeWidgetCell
import com.duofold.launcher.model.isActive
import com.duofold.launcher.model.occupiedCells
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetPlacementTest {
    @Test fun findsFirstFreeRectangleAvoidingIconsAndWidgets() {
        var layout = LayoutEditing.placeOnPage(HomeLayout.empty(1), 0, 0, "a")
        layout = layout.copy(
            widgets = listOf(
                WidgetPlacement("w1", page = 0, cell = 1, spanW = 2, spanH = 1, provider = "builtin:clock"),
            ),
        )
        // cells 0,1,2 occupied → first free 2x2 should be cell 4 (row1 col0) if 1,2 block row0
        assertEquals(4, layout.firstFreeWidgetCell(0, 2, 2))
    }

    @Test fun returnsNullWhenFull() {
        val full = HomeLayout.empty(1).copy(
            pages = listOf(List(HomeLayout.CELLS) { "x" }),
        )
        assertNull(full.firstFreeWidgetCell(0, 1, 1))
        assertTrue(full.occupiedCells(0).size == HomeLayout.CELLS)
    }
}

class DropZoneActiveTest {
    @Test fun leadingOnlyWhenDual() {
        assertTrue(DropZone.Leading(0).isActive(dual = true, currentPage = 0))
        assertFalse(DropZone.Leading(0).isActive(dual = false, currentPage = 0))
    }

    @Test fun pageMustMatch() {
        assertTrue(DropZone.Page(1, 3).isActive(dual = false, currentPage = 1))
        assertFalse(DropZone.Page(1, 3).isActive(dual = false, currentPage = 0))
        assertTrue(DropZone.Dock(2).isActive(dual = false, currentPage = 9))
    }
}
