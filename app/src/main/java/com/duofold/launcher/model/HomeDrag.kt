package com.duofold.launcher.model

/** Where a dragged shortcut can land. */
sealed class DropZone {
    data class Page(val page: Int, val cell: Int) : DropZone()
    data class Leading(val cell: Int) : DropZone()
    data class Dock(val slot: Int) : DropZone()
}

data class DropRegion(
    val zone: DropZone,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    fun contains(x: Float, y: Float): Boolean =
        x in left..right && y in top..bottom
}

/** Pure hit-test — first matching region wins (caller should register fine targets first). */
fun resolveDrop(regions: List<DropRegion>, x: Float, y: Float): DropZone? =
    regions.firstOrNull { it.contains(x, y) }?.zone

/** Keep only drop zones that belong to the currently visible workspace. */
fun DropZone.isActive(dual: Boolean, currentPage: Int): Boolean =
    when (this) {
        is DropZone.Leading -> dual
        is DropZone.Page -> page == currentPage
        is DropZone.Dock -> true
    }

fun commitDrop(layout: HomeLayout, appKey: String, zone: DropZone): HomeLayout =
    when (zone) {
        is DropZone.Page -> LayoutEditing.placeOnPage(layout, zone.page, zone.cell, appKey)
        is DropZone.Leading -> LayoutEditing.placeOnLeading(layout, zone.cell, appKey)
        is DropZone.Dock -> LayoutEditing.placeOnDock(layout, zone.slot, appKey)
    }
