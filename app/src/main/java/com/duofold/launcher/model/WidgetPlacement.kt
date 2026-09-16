package com.duofold.launcher.model

data class WidgetPlacement(
    val id: String,
    /** -1 = leading canvas, 0+ = home page index. */
    val page: Int,
    val cell: Int,
    val spanW: Int,
    val spanH: Int,
    /** Platform AppWidget id; 0 means first-party glance card. */
    val appWidgetId: Int = 0,
    /** `package/class` for native widgets, or `builtin:clock|date|battery`. */
    val provider: String,
) {
    val isBuiltin: Boolean get() = provider.startsWith("builtin:")
}

fun HomeLayout.withWidgets(widgets: List<WidgetPlacement>) = copy(widgets = widgets)

fun HomeLayout.addWidget(placement: WidgetPlacement): HomeLayout =
    copy(widgets = widgets + placement)

fun HomeLayout.removeWidget(id: String): HomeLayout =
    copy(widgets = widgets.filterNot { it.id == id })

/** Cells covered by a span starting at [cell], or empty if out of bounds. */
fun spanCells(cell: Int, spanW: Int, spanH: Int): List<Int> {
    val col = cell % HomeLayout.COLS
    val row = cell / HomeLayout.COLS
    if (col + spanW > HomeLayout.COLS || row + spanH > HomeLayout.ROWS) return emptyList()
    val out = ArrayList<Int>(spanW * spanH)
    for (r in row until row + spanH) for (c in col until col + spanW) {
        out += r * HomeLayout.COLS + c
    }
    return out
}

fun HomeLayout.pageCells(page: Int): List<String?> =
    if (page < 0) leading else pages.getOrNull(page) ?: emptyList()

/** Icon + widget cells already taken on a page (-1 = leading). */
fun HomeLayout.occupiedCells(page: Int): Set<Int> {
    val taken = mutableSetOf<Int>()
    pageCells(page).forEachIndexed { index, key -> if (key != null) taken += index }
    widgets.filter { it.page == page }.forEach { placement ->
        taken += spanCells(placement.cell, placement.spanW, placement.spanH)
    }
    return taken
}

/** First top-left cell that fits [spanW]×[spanH] without overlapping icons/widgets. */
fun HomeLayout.firstFreeWidgetCell(page: Int, spanW: Int, spanH: Int): Int? {
    val taken = occupiedCells(page)
    for (cell in 0 until HomeLayout.CELLS) {
        val span = spanCells(cell, spanW, spanH)
        if (span.isNotEmpty() && span.none { it in taken }) return cell
    }
    return null
}
