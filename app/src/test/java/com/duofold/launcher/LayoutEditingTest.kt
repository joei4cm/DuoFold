package com.duofold.launcher

import com.duofold.launcher.model.HomeLayout
import com.duofold.launcher.model.LayoutEditing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LayoutEditingTest {
    @Test fun movesBetweenPageAndDock() {
        val base = HomeLayout.empty(1)
        val withApp = LayoutEditing.placeOnPage(base, 0, 0, "a")
        val onDock = LayoutEditing.placeOnDock(withApp, 1, "a")
        assertNull(onDock.pages[0][0])
        assertEquals("a", onDock.dock[1])
    }
}
