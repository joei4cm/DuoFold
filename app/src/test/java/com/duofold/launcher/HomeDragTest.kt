package com.duofold.launcher

import com.duofold.launcher.model.DropRegion
import com.duofold.launcher.model.DropZone
import com.duofold.launcher.model.HomeLayout
import com.duofold.launcher.model.LayoutEditing
import com.duofold.launcher.model.commitDrop
import com.duofold.launcher.model.resolveDrop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeDragTest {
    @Test fun hitsFirstMatchingRegion() {
        val regions = listOf(
            DropRegion(DropZone.Dock(0), 0f, 0f, 50f, 50f),
            DropRegion(DropZone.Page(0, 3), 40f, 40f, 100f, 100f),
        )
        assertEquals(DropZone.Dock(0), resolveDrop(regions, 45f, 45f))
        assertEquals(DropZone.Page(0, 3), resolveDrop(regions, 80f, 80f))
        assertNull(resolveDrop(regions, 200f, 200f))
    }

    @Test fun commitMovesOntoDock() {
        val base = LayoutEditing.placeOnPage(HomeLayout.empty(1), 0, 2, "app")
        val next = commitDrop(base, "app", DropZone.Dock(3))
        assertNull(next.pages[0][2])
        assertEquals("app", next.dock[3])
    }

    @Test fun commitMovesOntoLeading() {
        val base = LayoutEditing.placeOnDock(HomeLayout.empty(1), 0, "b")
        val next = commitDrop(base, "b", DropZone.Leading(5))
        assertNull(next.dock[0])
        assertEquals("b", next.leading[5])
    }
}
