package com.duofold.launcher

import com.duofold.launcher.fold.PanelKind
import com.duofold.launcher.fold.classifyPanel
import com.duofold.launcher.fold.useExpandedWorkspace
import com.duofold.launcher.lock.shouldOfferLockSurface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PanelKindTest {
    @Test fun coverBySmallestWidth() {
        assertEquals(PanelKind.Cover, classifyPanel(1168, 1712, 2.75f))
    }

    @Test fun innerBySmallestWidth() {
        assertEquals(PanelKind.Inner, classifyPanel(1672, 2364, 2.5f))
    }

    @Test fun expandedNeedsInnerAndWidth() {
        assertTrue(useExpandedWorkspace(PanelKind.Inner, 700f))
        assertFalse(useExpandedWorkspace(PanelKind.Cover, 800f))
        assertFalse(useExpandedWorkspace(PanelKind.Inner, 600f))
    }
}

class LockGateTest {
    @Test fun respectsFlags() {
        assertFalse(shouldOfferLockSurface(false, true, true, false))
        assertFalse(shouldOfferLockSurface(true, true, false, false))
        assertFalse(shouldOfferLockSurface(true, false, true, true))
        assertTrue(shouldOfferLockSurface(true, true, true, false))
    }
}
