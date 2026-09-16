package com.duofold.launcher.widgets

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.duofold.launcher.R
import com.duofold.launcher.model.HomeLayout
import com.duofold.launcher.model.WidgetPlacement
import com.duofold.launcher.model.addWidget
import com.duofold.launcher.model.firstFreeWidgetCell
import com.duofold.launcher.model.spanCells
import java.util.UUID

/** Original AppWidget host for DuoFold. */
class WidgetController(
    private val activity: Activity,
    private val onLayout: (HomeLayout) -> Unit,
    private val layout: () -> HomeLayout,
) {
    private val manager = AppWidgetManager.getInstance(activity)
    private val host = AppWidgetHost(activity, HOST_ID)
    var catalog by mutableStateOf<List<AppWidgetProviderInfo>>(emptyList())
        private set

    private var pendingPage = 0
    private var pendingCell = 0
    private var pendingId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var pendingProvider: ComponentName? = null

    var bindRequest: Intent? by mutableStateOf(null)
        private set
    var configRequest: Intent? by mutableStateOf(null)
        private set

    fun start() {
        host.startListening()
        refreshCatalog()
    }

    fun stop() = host.stopListening()

    fun refreshCatalog() {
        catalog = manager.installedProviders
            .sortedBy { it.loadLabel(activity.packageManager)?.toString().orEmpty() }
    }

    fun createHostView(appWidgetId: Int): AppWidgetHostView? {
        if (appWidgetId <= 0) return null
        val info = manager.getAppWidgetInfo(appWidgetId) ?: return null
        return host.createView(activity, appWidgetId, info).also {
            it.setAppWidget(appWidgetId, info)
        }
    }

    fun requestNative(provider: AppWidgetProviderInfo, page: Int, cell: Int) {
        val spanW = ((provider.minWidth) / 70).coerceIn(1, HomeLayout.COLS)
        val spanH = ((provider.minHeight) / 70).coerceIn(1, HomeLayout.ROWS)
        val free = layout().firstFreeWidgetCell(page, spanW, spanH) ?: run {
            Toast.makeText(activity, R.string.widget_no_space, Toast.LENGTH_SHORT).show()
            return
        }
        pendingPage = page
        pendingCell = free
        pendingProvider = provider.provider
        pendingId = host.allocateAppWidgetId()
        val bound = manager.bindAppWidgetIdIfAllowed(pendingId, provider.provider)
        if (bound) onBound() else {
            bindRequest = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
            }
        }
    }

    fun consumeBindRequest(): Intent? {
        val intent = bindRequest
        bindRequest = null
        return intent
    }

    fun consumeConfigRequest(): Intent? {
        val intent = configRequest
        configRequest = null
        return intent
    }

    fun onBindResult(ok: Boolean) {
        if (ok) onBound() else abort(clearId = true)
    }

    fun onConfigResult(ok: Boolean) {
        if (ok) commit() else abort(clearId = true)
    }

    /** @return false when there is no free rectangle for the glance. */
    fun addBuiltin(kind: String, page: Int, preferredCell: Int = 0, spanW: Int = 2, spanH: Int = 2): Boolean {
        val w = spanW.coerceIn(1, HomeLayout.COLS)
        val h = spanH.coerceIn(1, HomeLayout.ROWS)
        val cell = layout().firstFreeWidgetCell(page, w, h) ?: return false
        onLayout(
            layout().addWidget(
                WidgetPlacement(
                    id = UUID.randomUUID().toString(),
                    page = page,
                    cell = cell,
                    spanW = w,
                    spanH = h,
                    provider = "builtin:$kind",
                )
            )
        )
        return true
    }

    fun delete(placement: WidgetPlacement) {
        if (placement.appWidgetId > 0) host.deleteAppWidgetId(placement.appWidgetId)
        onLayout(layout().copy(widgets = layout().widgets.filterNot { it.id == placement.id }))
    }

    private fun onBound() {
        val info = manager.getAppWidgetInfo(pendingId)
        if (info?.configure != null) {
            configRequest = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingId)
            }
        } else commit()
    }

    private fun commit() {
        val provider = pendingProvider ?: return abort(clearId = true)
        val info = manager.getAppWidgetInfo(pendingId)
        val spanW = ((info?.minWidth ?: 110) / 70).coerceIn(1, HomeLayout.COLS)
        val spanH = ((info?.minHeight ?: 70) / 70).coerceIn(1, HomeLayout.ROWS)
        val cell = layout().firstFreeWidgetCell(pendingPage, spanW, spanH)
            ?: pendingCell.takeIf { spanCells(it, spanW, spanH).isNotEmpty() }
        if (cell == null) return abort(clearId = true)
        onLayout(
            layout().addWidget(
                WidgetPlacement(
                    id = UUID.randomUUID().toString(),
                    page = pendingPage,
                    cell = cell,
                    spanW = spanW,
                    spanH = spanH,
                    appWidgetId = pendingId,
                    provider = provider.flattenToString(),
                )
            )
        )
        abort(clearId = false)
    }

    private fun abort(clearId: Boolean) {
        if (clearId && pendingId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            runCatching { host.deleteAppWidgetId(pendingId) }
        }
        pendingId = AppWidgetManager.INVALID_APPWIDGET_ID
        pendingProvider = null
        bindRequest = null
        configRequest = null
    }

    companion object {
        private const val HOST_ID = 0xD10F01D
    }
}
