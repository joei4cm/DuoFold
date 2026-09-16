package com.duofold.launcher.fold

/**
 * Cover vs inner classification from physical bounds.
 * HyperOS may reuse logical display ids; prefer area / smallest width.
 */
enum class PanelKind { Cover, Inner, Unknown }

fun classifyPanel(
    widthPx: Int,
    heightPx: Int,
    density: Float,
    knownAreas: List<Long> = emptyList(),
): PanelKind {
    if (widthPx <= 0 || heightPx <= 0 || density <= 0f) return PanelKind.Unknown
    val area = widthPx.toLong() * heightPx
    if (knownAreas.size >= 2) {
        val sorted = knownAreas.sorted()
        val coverArea = sorted.first()
        val innerArea = sorted.last()
        if (area <= (coverArea + innerArea) / 2) return PanelKind.Cover
        return PanelKind.Inner
    }
    val smallestDp = minOf(widthPx, heightPx) / density
    return if (smallestDp >= 600f) PanelKind.Inner else PanelKind.Cover
}

/** Whether the workspace should show the dual-pane (leading + home) composition. */
fun useExpandedWorkspace(panel: PanelKind, windowWidthDp: Float): Boolean =
    panel != PanelKind.Cover && windowWidthDp >= 650f
