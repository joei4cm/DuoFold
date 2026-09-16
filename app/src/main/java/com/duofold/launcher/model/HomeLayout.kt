package com.duofold.launcher.model

import android.content.ComponentName
import android.graphics.drawable.Drawable

data class LaunchApp(
    val key: String,
    val label: String,
    val component: ComponentName,
    val icon: Drawable,
)

data class HomeLayout(
    /** Pages of cell keys; each page is COLS*ROWS, null = empty. */
    val pages: List<List<String?>>,
    /** Right dock, fixed length DOCK. */
    val dock: List<String?>,
    /** Extra left pane when unfolded; same cell count as one page. */
    val leading: List<String?>,
    /** Widget / glance placements occupying grid rectangles. */
    val widgets: List<WidgetPlacement> = emptyList(),
) {
    companion object {
        const val COLS = 4
        const val ROWS = 6
        const val CELLS = COLS * ROWS
        const val DOCK = 4

        fun empty(pageCount: Int = 1) = HomeLayout(
            pages = List(pageCount.coerceAtLeast(1)) { List(CELLS) { null } },
            dock = List(DOCK) { null },
            leading = List(CELLS) { null },
            widgets = emptyList(),
        )
    }
}
