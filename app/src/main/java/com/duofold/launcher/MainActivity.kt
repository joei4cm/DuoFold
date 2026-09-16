package com.duofold.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.res.Configuration
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duofold.launcher.data.AppCatalog
import com.duofold.launcher.data.FeaturePrefs
import com.duofold.launcher.data.LayoutStore
import com.duofold.launcher.fold.FoldSensors
import com.duofold.launcher.model.HomeLayout
import com.duofold.launcher.model.LaunchApp
import com.duofold.launcher.ui.DuoFoldTheme
import com.duofold.launcher.ui.HomeRoot
import com.duofold.launcher.widgets.WidgetController
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    internal val prefs by lazy { FeaturePrefs(this) }
    private val catalog by lazy { AppCatalog(this) }
    private val layoutStore by lazy { LayoutStore(this) }
    private val fold by lazy { FoldSensors(this) }
    private val appsFlow = MutableStateFlow<Map<String, LaunchApp>>(emptyMap())
    private val layoutFlow = MutableStateFlow(HomeLayout.empty())
    private var homeRole by mutableStateOf(false)
    private var landscape by mutableStateOf(false)

    private lateinit var widgets: WidgetController

    private val bindLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        widgets.onBindResult(result.resultCode == RESULT_OK)
    }
    private val configLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        widgets.onConfigResult(result.resultCode == RESULT_OK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgets = WidgetController(
            activity = this,
            onLayout = { next ->
                layoutFlow.value = next
                layoutStore.save(next)
            },
            layout = { layoutFlow.value },
        )
        enableEdgeToEdge()
        landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        refreshCatalog(loadLayout = true)
        setContent {
            val apps by appsFlow.collectAsStateWithLifecycle()
            val layout by layoutFlow.collectAsStateWithLifecycle()
            LaunchedEffect(widgets.bindRequest) {
                widgets.consumeBindRequest()?.let(bindLauncher::launch)
            }
            LaunchedEffect(widgets.configRequest) {
                widgets.consumeConfigRequest()?.let(configLauncher::launch)
            }
            DuoFoldTheme {
                HomeRoot(
                    apps = apps,
                    layout = layout,
                    panel = fold.panel,
                    foldAmount = fold.foldAmount,
                    foldAnimationEnabled = prefs.foldAnimation,
                    standbyEnabled = prefs.standby,
                    landscape = landscape,
                    isDefaultHome = homeRole,
                    prefs = prefs,
                    widgets = widgets,
                    onLaunch = ::launch,
                    onMakeDefault = ::openHomeSettings,
                    onLayoutChange = { next ->
                        layoutFlow.value = next
                        layoutStore.save(next)
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        fold.start()
        widgets.start()
        homeRole = isHomeRole()
    }

    override fun onStop() {
        widgets.stop()
        fold.stop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        fold.refreshPanel()
        homeRole = isHomeRole()
        refreshCatalog(loadLayout = false)
        widgets.refreshCatalog()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        fold.refreshPanel()
        landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private fun refreshCatalog(loadLayout: Boolean) {
        val list = catalog.loadLaunchableApps()
        appsFlow.value = list.associateBy { it.key }
        if (loadLayout) layoutFlow.value = layoutStore.loadOrDefault(list)
    }

    private fun launch(app: LaunchApp) {
        runCatching {
            getSystemService(LauncherApps::class.java)
                .startMainActivity(app.component, Process.myUserHandle(), null, null)
        }.onFailure {
            Toast.makeText(this, app.label, Toast.LENGTH_SHORT).show()
            refreshCatalog(loadLayout = false)
        }
    }

    private fun isHomeRole(): Boolean =
        getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_HOME)

    private fun openHomeSettings() {
        runCatching { startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
            .onFailure {
                runCatching { startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)) }
            }
    }
}
