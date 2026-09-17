package com.duofold.launcher.ui

import android.appwidget.AppWidgetProviderInfo
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import android.widget.Toast
import com.duofold.launcher.R
import com.duofold.launcher.assist.AssistService
import com.duofold.launcher.data.FeaturePrefs
import com.duofold.launcher.fold.PanelKind
import com.duofold.launcher.fold.useExpandedWorkspace
import com.duofold.launcher.model.DropRegion
import com.duofold.launcher.model.DropZone
import com.duofold.launcher.model.HomeLayout
import com.duofold.launcher.model.LaunchApp
import com.duofold.launcher.model.LayoutEditing
import com.duofold.launcher.model.WidgetPlacement
import com.duofold.launcher.model.commitDrop
import com.duofold.launcher.model.isActive
import com.duofold.launcher.model.resolveDrop
import com.duofold.launcher.widgets.WidgetController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

private data class DragSession(
    val appKey: String,
    val pointer: Offset,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeRoot(
    apps: Map<String, LaunchApp>,
    layout: HomeLayout,
    panel: PanelKind,
    foldAmount: Float,
    foldAnimationEnabled: Boolean,
    standbyEnabled: Boolean,
    landscape: Boolean,
    isDefaultHome: Boolean,
    prefs: FeaturePrefs,
    widgets: WidgetController,
    onLaunch: (LaunchApp) -> Unit,
    onMakeDefault: () -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
) {
    var showSettings by remember { mutableStateOf(false) }
    var showAllApps by remember { mutableStateOf(false) }
    var showWidgets by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    var drag by remember { mutableStateOf<DragSession?>(null) }
    val dropRegions = remember { mutableMapOf<DropZone, DropRegion>() }
    val pager = rememberPagerState(pageCount = { layout.pages.size.coerceAtLeast(1) })
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val localDensity = LocalDensity.current
    val context = LocalContext.current
    var clock by remember {
        mutableStateOf(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date()))
    }

    fun registerDrop(zone: DropZone, coords: LayoutCoordinates) {
        val pos = coords.positionInRoot()
        dropRegions[zone] = DropRegion(
            zone = zone,
            left = pos.x,
            top = pos.y,
            right = pos.x + coords.size.width,
            bottom = pos.y + coords.size.height,
        )
    }

    fun beginDrag(key: String, pointer: Offset) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        editing = true
        selectedKey = key
        drag = DragSession(key, pointer)
    }

    fun moveDrag(pointer: Offset) {
        drag = drag?.copy(pointer = pointer)
    }

    fun endDrag(dualPane: Boolean, page: Int) {
        val session = drag ?: return
        val active = dropRegions.values.filter { it.zone.isActive(dualPane, page) }
        val zone = resolveDrop(active, session.pointer.x, session.pointer.y)
        if (zone != null) {
            onLayoutChange(commitDrop(layout, session.appKey, zone))
            selectedKey = null
        }
        drag = null
    }

    if (standbyEnabled && panel == PanelKind.Cover && landscape) {
        StandbySuite(onExit = { /* rotate back exits */ })
        return
    }

    val effectTarget = if (foldAnimationEnabled) foldAmount else 0f
    val effect by animateFloatAsState(effectTarget, animationSpec = DuoMotion.fold, label = "fold")
    val draggingKey = drag?.appKey
    val cover = panel == PanelKind.Cover
    val sidePad = if (cover) 10.dp else 16.dp
    val topPad = if (cover) 4.dp else 8.dp
    val clockSize = if (cover) 34.sp else 30.sp

    FoldAtmosphere(
        foldAmount = effect,
        panel = panel,
        enabled = foldAnimationEnabled,
    ) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val dual = useExpandedWorkspace(panel, maxWidth.value)
        LaunchedEffect(dual, pager.currentPage) {
            dropRegions.clear()
        }
        LaunchedEffect(Unit) {
            while (true) {
                clock = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
                delay(15_000)
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = sidePad, end = if (cover) 8.dp else 12.dp, top = topPad, bottom = 10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = if (cover) 2.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    clock,
                    color = Color.White.copy(alpha = 0.96f),
                    fontSize = clockSize,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.weight(1f),
                )
                if (editing) {
                    TextButton(onClick = {
                        editing = false
                        selectedKey = null
                        drag = null
                    }) {
                        Text(stringResource(R.string.done), color = Color.White)
                    }
                } else {
                    IconButton(onClick = { showWidgets = true }) {
                        Icon(Icons.Rounded.Widgets, stringResource(R.string.widgets), tint = Color.White)
                    }
                    IconButton(onClick = { showAllApps = true }) {
                        Icon(Icons.Rounded.Apps, stringResource(R.string.all_apps), tint = Color.White)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Rounded.Settings, stringResource(R.string.settings), tint = Color.White)
                    }
                }
            }

            if (editing) {
                Text(
                    stringResource(R.string.edit_mode),
                    color = Color.White.copy(0.75f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            Row(Modifier.weight(1f).fillMaxWidth()) {
                if (dual) {
                    GlassPanel(
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 4.dp),
                        radius = 24.dp,
                        tone = GlassTone.Leading,
                    ) {
                        WorkspaceCanvas(
                            cells = layout.leading,
                            pageWidgets = layout.widgets.filter { it.page == -1 },
                            apps = apps,
                            editing = editing,
                            selectedKey = selectedKey,
                            draggingKey = draggingKey,
                            widgets = widgets,
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            onLaunch = onLaunch,
                            onLongPressEmpty = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                editing = true
                            },
                            onDragStart = ::beginDrag,
                            onDrag = ::moveDrag,
                            onDragEnd = { endDrag(dual, pager.currentPage) },
                            onRegisterDrop = { cell, coords -> registerDrop(DropZone.Leading(cell), coords) },
                            onTapCell = { cell ->
                                val key = selectedKey
                                if (editing && drag == null && key != null) {
                                    onLayoutChange(LayoutEditing.placeOnLeading(layout, cell, key))
                                    selectedKey = null
                                }
                            },
                            onRemoveWidget = widgets::delete,
                        )
                    }
                    HingeGroove(Modifier.padding(horizontal = 4.dp))
                }

                Column(Modifier.weight(if (dual) 1.18f else 1f).fillMaxHeight()) {
                    HorizontalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                        val cells = layout.pages.getOrNull(page) ?: List(HomeLayout.CELLS) { null }
                        WorkspaceCanvas(
                            cells = cells,
                            pageWidgets = layout.widgets.filter { it.page == page },
                            apps = apps,
                            editing = editing,
                            selectedKey = selectedKey,
                            draggingKey = draggingKey,
                            widgets = widgets,
                            modifier = Modifier.fillMaxSize(),
                            onLaunch = onLaunch,
                            onLongPressEmpty = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                editing = true
                            },
                            onDragStart = ::beginDrag,
                            onDrag = ::moveDrag,
                            onDragEnd = { endDrag(dual, pager.currentPage) },
                            onRegisterDrop = { cell, coords -> registerDrop(DropZone.Page(page, cell), coords) },
                            onTapCell = { cell ->
                                val key = selectedKey
                                if (editing && drag == null && key != null) {
                                    onLayoutChange(LayoutEditing.placeOnPage(layout, page, cell, key))
                                    selectedKey = null
                                }
                            },
                            onRemoveWidget = widgets::delete,
                        )
                    }
                    PageDots(count = layout.pages.size, selected = pager.currentPage)
                }

                DockColumn(
                    dock = layout.dock,
                    apps = apps,
                    editing = editing,
                    selectedKey = selectedKey,
                    draggingKey = draggingKey,
                    modifier = Modifier
                        .width(if (cover) 72.dp else 84.dp)
                        .fillMaxHeight()
                        .padding(start = if (dual) 8.dp else 10.dp),
                    onLaunch = onLaunch,
                    onDragStart = ::beginDrag,
                    onDrag = ::moveDrag,
                    onDragEnd = { endDrag(dual, pager.currentPage) },
                    onRegisterDrop = { slot, coords -> registerDrop(DropZone.Dock(slot), coords) },
                    onTapSlot = { slot ->
                        val key = selectedKey
                        if (editing && drag == null && key != null) {
                            onLayoutChange(LayoutEditing.placeOnDock(layout, slot, key))
                            selectedKey = null
                        }
                    },
                )
            }
        }

        ShadePullHost(
            enabled = prefs.shadeGestures && !editing && drag == null,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding(),
        )

        SoftNavBar(
            enabled = prefs.softNav,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        drag?.let { session ->
            val app = apps[session.appKey]
            if (app != null) {
                val previewPx = with(localDensity) { 56.dp.toPx() }
                Box(
                    Modifier
                        .zIndex(8f)
                        .offset {
                            IntOffset(
                                (session.pointer.x - previewPx / 2f).roundToInt(),
                                (session.pointer.y - previewPx / 2f).roundToInt(),
                            )
                        }
                        .size(56.dp)
                        .graphicsLayer { alpha = 0.92f; scaleX = 1.12f; scaleY = 1.12f },
                ) {
                    AppIcon(app, showLabel = false, jiggle = false)
                }
            }
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF1A242C),
            ) {
                SettingsSheet(
                    prefs = prefs,
                    isDefaultHome = isDefaultHome,
                    onMakeDefault = onMakeDefault,
                    onClose = { showSettings = false },
                )
            }
        }
        if (showWidgets) {
            ModalBottomSheet(
                onDismissRequest = { showWidgets = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF1A242C),
            ) {
                WidgetPickerSheet(
                    catalog = widgets.catalog,
                    dual = dual,
                    currentPage = pager.currentPage,
                    onBuiltin = { kind, page, _ ->
                        if (!widgets.addBuiltin(kind, page)) {
                            Toast.makeText(context, R.string.widget_no_space, Toast.LENGTH_SHORT).show()
                        } else {
                            showWidgets = false
                        }
                    },
                    onNative = { info, page, cell ->
                        widgets.requestNative(info, page, cell)
                        showWidgets = false
                    },
                    onClose = { showWidgets = false },
                )
            }
        }
        if (showAllApps) {
            ModalBottomSheet(
                onDismissRequest = { showAllApps = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF1A242C),
            ) {
                Text(
                    stringResource(R.string.all_apps),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
                AllAppsList(
                    apps = apps.values.toList(),
                    editing = editing,
                    onLaunch = {
                        showAllApps = false
                        onLaunch(it)
                    },
                    onPickForHome = { app ->
                        val page = pager.currentPage.coerceIn(0, layout.pages.lastIndex)
                        val cell = LayoutEditing.firstEmptyCell(layout.pages[page])
                        if (cell != null) {
                            onLayoutChange(LayoutEditing.placeOnPage(layout, page, cell, app.key))
                            showAllApps = false
                            editing = false
                        }
                    },
                )
                Spacer(Modifier.height(28.dp))
            }
        }
        LaunchedEffect(showAllApps) {
            if (showAllApps) scope.launch { /* keep current page */ }
        }
    }
    } // FoldAtmosphere
}

@Composable
private fun PageDots(count: Int, selected: Int) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count.coerceAtLeast(1)) { i ->
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (i == selected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (i == selected) 0.95f else 0.32f))
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkspaceCanvas(
    cells: List<String?>,
    pageWidgets: List<WidgetPlacement>,
    apps: Map<String, LaunchApp>,
    editing: Boolean,
    selectedKey: String?,
    draggingKey: String?,
    widgets: WidgetController,
    modifier: Modifier = Modifier,
    onLaunch: (LaunchApp) -> Unit,
    onLongPressEmpty: () -> Unit,
    onDragStart: (String, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onRegisterDrop: (Int, LayoutCoordinates) -> Unit,
    onTapCell: (Int) -> Unit,
    onRemoveWidget: (WidgetPlacement) -> Unit,
) {
    BoxWithConstraints(modifier) {
        val cellW = maxWidth / HomeLayout.COLS
        val cellH = maxHeight / HomeLayout.ROWS
        WorkspaceGrid(
            cells = cells,
            apps = apps,
            editing = editing,
            selectedKey = selectedKey,
            draggingKey = draggingKey,
            modifier = Modifier.fillMaxSize(),
            onLaunch = onLaunch,
            onLongPressEmpty = onLongPressEmpty,
            onDragStart = onDragStart,
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            onRegisterDrop = onRegisterDrop,
            onTapCell = onTapCell,
        )
        pageWidgets.forEach { placement ->
            val col = placement.cell % HomeLayout.COLS
            val row = placement.cell / HomeLayout.COLS
            Box(
                Modifier
                    .offset { IntOffset((cellW * col).roundToPx(), (cellH * row).roundToPx()) }
                    .width(cellW * placement.spanW)
                    .height(cellH * placement.spanH)
                    .padding(4.dp)
                    .then(
                        if (editing) {
                            Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { onRemoveWidget(placement) },
                            )
                        } else {
                            Modifier
                        },
                    )
            ) {
                WidgetSurface(placement, widgets, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun WidgetPickerSheet(
    catalog: List<AppWidgetProviderInfo>,
    dual: Boolean,
    currentPage: Int,
    onBuiltin: (String, Int, Int) -> Unit,
    onNative: (AppWidgetProviderInfo, Int, Int) -> Unit,
    onClose: () -> Unit,
) {
    val targetPage = if (dual) -1 else currentPage
    val pm = LocalContext.current.packageManager
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(560.dp)
    ) {
        Text(stringResource(R.string.widgets), color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.first_party_widgets), color = Color.White.copy(0.7f), style = MaterialTheme.typography.titleSmall)
        listOf("clock" to R.string.builtin_clock, "date" to R.string.builtin_date, "battery" to R.string.builtin_battery).forEach { (kind, label) ->
            TextButton(onClick = { onBuiltin(kind, targetPage, 0) }) {
                Text(stringResource(label), color = Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.native_widgets), color = Color.White.copy(0.7f), style = MaterialTheme.typography.titleSmall)
        LazyColumn(Modifier.weight(1f)) {
            lazyItems(catalog.take(40), key = { it.provider.flattenToString() }) { info ->
                val label = info.loadLabel(pm)?.toString() ?: info.provider.className
                TextButton(onClick = { onNative(info, targetPage, 0) }) {
                    Text(label, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        TextButton(onClick = onClose) { Text(stringResource(R.string.close), color = Color.White) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkspaceGrid(
    cells: List<String?>,
    apps: Map<String, LaunchApp>,
    editing: Boolean,
    selectedKey: String?,
    draggingKey: String?,
    modifier: Modifier = Modifier,
    onLaunch: (LaunchApp) -> Unit,
    onLongPressEmpty: () -> Unit,
    onDragStart: (String, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onRegisterDrop: (Int, LayoutCoordinates) -> Unit,
    onTapCell: (Int) -> Unit,
) {
    Column(modifier.padding(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(HomeLayout.ROWS) { row ->
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(HomeLayout.COLS) { col ->
                    val index = row * HomeLayout.COLS + col
                    val key = cells.getOrNull(index)
                    val app = key?.let { apps[it] }
                    val selected = key != null && key == selectedKey
                    val hidden = key != null && key == draggingKey
                    val scale by animateFloatAsState(
                        if (selected && !hidden) 1.08f else 1f,
                        animationSpec = DuoMotion.snappy,
                        label = "icon-scale",
                    )
                    var cellOrigin by remember { mutableStateOf(Offset.Zero) }
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .onGloballyPositioned {
                                cellOrigin = it.positionInRoot()
                                onRegisterDrop(index, it)
                            }
                            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (hidden) 0.2f else 1f }
                            .pointerInput(key, editing) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { local ->
                                        val appKey = key
                                        if (appKey != null) {
                                            onDragStart(appKey, cellOrigin + local)
                                        } else {
                                            onLongPressEmpty()
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        onDrag(cellOrigin + change.position)
                                    },
                                    onDragEnd = { onDragEnd() },
                                    onDragCancel = { onDragEnd() },
                                )
                            }
                            .combinedClickable(
                                onClick = {
                                    if (editing) onTapCell(index) else app?.let(onLaunch)
                                },
                                onLongClick = {
                                    if (app == null) onLongPressEmpty()
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (app != null) AppIcon(app, jiggle = editing && !hidden)
                    }
                }
            }
        }
    }
}

@Composable
private fun DockColumn(
    dock: List<String?>,
    apps: Map<String, LaunchApp>,
    editing: Boolean,
    selectedKey: String?,
    draggingKey: String?,
    modifier: Modifier = Modifier,
    onLaunch: (LaunchApp) -> Unit,
    onDragStart: (String, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onRegisterDrop: (Int, LayoutCoordinates) -> Unit,
    onTapSlot: (Int) -> Unit,
) {
    GlassPanel(modifier = modifier, radius = 28.dp, tone = GlassTone.Dock) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            dock.forEachIndexed { slot, key ->
                val app = key?.let { apps[it] }
                val hidden = key != null && key == draggingKey
                var slotOrigin by remember { mutableStateOf(Offset.Zero) }
                Box(
                    Modifier
                        .size(56.dp)
                        .onGloballyPositioned {
                            slotOrigin = it.positionInRoot()
                            onRegisterDrop(slot, it)
                        }
                        .graphicsLayer { alpha = if (hidden) 0.2f else 1f }
                        .pointerInput(key, editing) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { local ->
                                    val appKey = key ?: return@detectDragGesturesAfterLongPress
                                    onDragStart(appKey, slotOrigin + local)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    onDrag(slotOrigin + change.position)
                                },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                            )
                        }
                        .combinedClickable(
                            onClick = {
                                if (editing) onTapSlot(slot) else app?.let(onLaunch)
                            },
                            onLongClick = {},
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (app != null) AppIcon(app, showLabel = false, jiggle = editing && !hidden)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AllAppsList(
    apps: List<LaunchApp>,
    editing: Boolean,
    onLaunch: (LaunchApp) -> Unit,
    onPickForHome: (LaunchApp) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(88.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().height(520.dp),
    ) {
        items(apps, key = { it.key }) { app ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { if (editing) onPickForHome(app) else onLaunch(app) },
                        onLongClick = { onPickForHome(app) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppIcon(app)
                if (editing) {
                    Text(
                        stringResource(R.string.add_to_home),
                        color = Color.White.copy(0.55f),
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun AppIcon(app: LaunchApp, showLabel: Boolean = true, jiggle: Boolean = false) {
    val jiggleRot by animateFloatAsState(
        if (jiggle) (-2f..2f).random() else 0f,
        animationSpec = DuoMotion.soft,
        label = "jiggle",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer { rotationZ = jiggleRot },
    ) {
        DrawableIcon(app.icon, Modifier.size(54.dp))
        if (showLabel) {
            Text(
                app.label,
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 5.dp).fillMaxWidth(),
            )
        }
    }
}

@Composable
fun DrawableIcon(drawable: Drawable, modifier: Modifier = Modifier) {
    val bitmap = remember(drawable) { drawable.toBitmap(width = 144, height = 144) }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = modifier.clip(RoundedCornerShape(DuoMotion.iconRadius)),
    )
}

@Composable
fun SettingsSheet(
    prefs: FeaturePrefs,
    isDefaultHome: Boolean,
    onMakeDefault: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.settings), color = Color.White, style = MaterialTheme.typography.headlineSmall)
        TextButton(onClick = onMakeDefault) {
            Icon(Icons.Rounded.Home, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(if (isDefaultHome) R.string.home_enabled else R.string.set_as_home),
                color = Color.White,
            )
        }
        PrefSwitch(
            title = stringResource(R.string.fold_animation),
            detail = stringResource(R.string.fold_animation_detail),
            checked = prefs.foldAnimation,
            onChecked = prefs::enableFoldAnimation,
        )
        PrefSwitch(
            title = stringResource(R.string.standby),
            detail = stringResource(R.string.standby_detail),
            checked = prefs.standby,
            onChecked = prefs::enableStandby,
        )
        PrefSwitch(
            title = stringResource(R.string.lock_surface),
            detail = stringResource(R.string.lock_surface_detail),
            checked = prefs.lockSurface,
            onChecked = prefs::enableLockSurface,
        )
        if (prefs.lockSurface) {
            PrefSwitch(
                title = stringResource(R.string.lock_cover_only),
                detail = null,
                checked = prefs.lockCoverOnly,
                onChecked = prefs::enableLockCoverOnly,
            )
        }
        PrefSwitch(
            title = stringResource(R.string.shade_gestures),
            detail = stringResource(R.string.shade_gestures_detail),
            checked = prefs.shadeGestures,
            onChecked = prefs::enableShadeGestures,
        )
        PrefSwitch(
            title = stringResource(R.string.soft_nav),
            detail = stringResource(R.string.soft_nav_detail),
            checked = prefs.softNav,
            onChecked = prefs::enableSoftNav,
        )
        if (prefs.shadeGestures || prefs.softNav) {
            TextButton(onClick = { AssistService.openSettings(context) }) {
                Text(stringResource(R.string.open_assist_settings), color = Color.White)
            }
        }
        Text(stringResource(R.string.about_title), color = Color.White, style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.about_body), color = Color.White.copy(0.75f), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onClose) { Text(stringResource(R.string.close), color = Color.White) }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PrefSwitch(
    title: String,
    detail: String?,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall)
            detail?.let { Text(it, color = Color.White.copy(0.65f), style = MaterialTheme.typography.bodySmall) }
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float =
    start + (endInclusive - start) * kotlin.random.Random.nextFloat()
