package com.duofold.launcher.model

/** Immutable edit helpers — original DuoFold layout rules. */
object LayoutEditing {
    fun placeOnPage(layout: HomeLayout, page: Int, cell: Int, appKey: String): HomeLayout {
        if (page !in layout.pages.indices || cell !in 0 until HomeLayout.CELLS) return layout
        val pages = layout.pages.mapIndexed { i, cells ->
            if (i != page) cells else cells.toMutableList().also { it[cell] = appKey }
        }
        val dock = layout.dock.map { if (it == appKey) null else it }
        val leading = layout.leading.map { if (it == appKey) null else it }
        return layout.copy(pages = pages, dock = dock, leading = leading)
    }

    fun placeOnDock(layout: HomeLayout, slot: Int, appKey: String): HomeLayout {
        if (slot !in 0 until HomeLayout.DOCK) return layout
        val dock = layout.dock.toMutableList().also { it[slot] = appKey }
        val pages = layout.pages.map { page -> page.map { if (it == appKey) null else it } }
        val leading = layout.leading.map { if (it == appKey) null else it }
        return layout.copy(pages = pages, dock = dock, leading = leading)
    }

    fun placeOnLeading(layout: HomeLayout, cell: Int, appKey: String): HomeLayout {
        if (cell !in 0 until HomeLayout.CELLS) return layout
        val leading = layout.leading.toMutableList().also { it[cell] = appKey }
        val pages = layout.pages.map { page -> page.map { if (it == appKey) null else it } }
        val dock = layout.dock.map { if (it == appKey) null else it }
        return layout.copy(pages = pages, dock = dock, leading = leading)
    }

    fun clearKey(layout: HomeLayout, appKey: String): HomeLayout =
        layout.copy(
            pages = layout.pages.map { page -> page.map { if (it == appKey) null else it } },
            dock = layout.dock.map { if (it == appKey) null else it },
            leading = layout.leading.map { if (it == appKey) null else it },
        )

    fun firstEmptyCell(page: List<String?>): Int? =
        page.indexOfFirst { it == null }.takeIf { it >= 0 }
}
