package com.duofold.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.duofold.launcher.model.HomeLayout
import com.duofold.launcher.model.LaunchApp
import org.json.JSONArray
import org.json.JSONObject

class AppCatalog(private val context: Context) {
    fun loadLaunchableApps(): List<LaunchApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { info ->
                val cn = ComponentName(info.activityInfo.packageName, info.activityInfo.name)
                if (cn.packageName == context.packageName) return@mapNotNull null
                val label = info.loadLabel(pm)?.toString().orEmpty().ifBlank { cn.packageName }
                val icon = info.loadIcon(pm) ?: return@mapNotNull null
                LaunchApp(key = cn.flattenToString(), label = label, component = cn, icon = icon)
            }
            .sortedBy { it.label.lowercase() }
    }
}

class LayoutStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("duofold_layout_v1", Context.MODE_PRIVATE)

    fun loadOrDefault(apps: List<LaunchApp>): HomeLayout {
        val raw = prefs.getString(KEY, null) ?: return seed(apps)
        return runCatching { decode(raw) }.getOrElse { seed(apps) }
    }

    fun save(layout: HomeLayout) {
        prefs.edit().putString(KEY, encode(layout)).apply()
    }

    private fun seed(apps: List<LaunchApp>): HomeLayout {
        val keys = apps.map { it.key }
        val page = MutableList<String?>(HomeLayout.CELLS) { null }
        keys.take(HomeLayout.CELLS).forEachIndexed { i, key -> page[i] = key }
        val dock = MutableList<String?>(HomeLayout.DOCK) { null }
        // Prefer common system tools for dock when present.
        val prefer = listOf("camera", "dialer", "messaging", "browser", "chrome", "mms", "contacts")
        val dockKeys = keys.filter { key ->
            prefer.any { token -> key.lowercase().contains(token) }
        }.distinct().take(HomeLayout.DOCK)
        dockKeys.forEachIndexed { i, key -> dock[i] = key }
        // Avoid duplicating dock icons on page 0.
        dock.filterNotNull().forEach { key ->
            val idx = page.indexOf(key)
            if (idx >= 0) page[idx] = null
        }
        return HomeLayout(
            pages = listOf(page),
            dock = dock,
            leading = List(HomeLayout.CELLS) { null },
            widgets = listOf(
                com.duofold.launcher.model.WidgetPlacement(
                    id = "seed-clock",
                    page = -1,
                    cell = 0,
                    spanW = 2,
                    spanH = 2,
                    provider = "builtin:clock",
                ),
                com.duofold.launcher.model.WidgetPlacement(
                    id = "seed-date",
                    page = -1,
                    cell = 2,
                    spanW = 2,
                    spanH = 1,
                    provider = "builtin:date",
                ),
            ),
        )
    }

    private fun encode(layout: HomeLayout): String {
        val root = JSONObject()
        root.put("pages", JSONArray(layout.pages.map { page -> JSONArray(page.map { it ?: JSONObject.NULL }) }))
        root.put("dock", JSONArray(layout.dock.map { it ?: JSONObject.NULL }))
        root.put("leading", JSONArray(layout.leading.map { it ?: JSONObject.NULL }))
        root.put("widgets", JSONArray(layout.widgets.map { w ->
            JSONObject()
                .put("id", w.id)
                .put("page", w.page)
                .put("cell", w.cell)
                .put("spanW", w.spanW)
                .put("spanH", w.spanH)
                .put("appWidgetId", w.appWidgetId)
                .put("provider", w.provider)
        }))
        return root.toString()
    }

    private fun decode(raw: String): HomeLayout {
        val root = JSONObject(raw)
        fun arr(name: String): List<String?> {
            val a = root.getJSONArray(name)
            return List(a.length()) { i ->
                if (a.isNull(i)) null else a.getString(i)
            }
        }
        val pagesJson = root.getJSONArray("pages")
        val pages = List(pagesJson.length()) { p ->
            val a = pagesJson.getJSONArray(p)
            List(a.length()) { i -> if (a.isNull(i)) null else a.getString(i) }
                .let { cells ->
                    if (cells.size >= HomeLayout.CELLS) cells.take(HomeLayout.CELLS)
                    else cells + List(HomeLayout.CELLS - cells.size) { null }
                }
        }.ifEmpty { listOf(List(HomeLayout.CELLS) { null }) }
        val dock = arr("dock").let {
            if (it.size >= HomeLayout.DOCK) it.take(HomeLayout.DOCK)
            else it + List(HomeLayout.DOCK - it.size) { null }
        }
        val leading = arr("leading").let {
            if (it.size >= HomeLayout.CELLS) it.take(HomeLayout.CELLS)
            else it + List(HomeLayout.CELLS - it.size) { null }
        }
        val widgets = if (root.has("widgets")) {
            val a = root.getJSONArray("widgets")
            List(a.length()) { i ->
                val o = a.getJSONObject(i)
                com.duofold.launcher.model.WidgetPlacement(
                    id = o.getString("id"),
                    page = o.getInt("page"),
                    cell = o.getInt("cell"),
                    spanW = o.getInt("spanW"),
                    spanH = o.getInt("spanH"),
                    appWidgetId = o.optInt("appWidgetId", 0),
                    provider = o.getString("provider"),
                )
            }
        } else emptyList()
        return HomeLayout(pages, dock, leading, widgets)
    }

    companion object {
        private const val KEY = "layout_json"
    }
}
