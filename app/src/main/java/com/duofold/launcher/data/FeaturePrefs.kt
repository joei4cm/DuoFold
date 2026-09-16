package com.duofold.launcher.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Local toggles. Original DuoFold preferences — no remote activation. */
class FeaturePrefs(context: Context) {
    private val app = context.applicationContext
    private val store = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var foldAnimation by mutableStateOf(store.getBoolean(KEY_FOLD, true))
        private set
    var standby by mutableStateOf(store.getBoolean(KEY_STANDBY, false))
        private set
    var lockSurface by mutableStateOf(store.getBoolean(KEY_LOCK, false))
        private set
    var lockCoverOnly by mutableStateOf(store.getBoolean(KEY_LOCK_COVER, true))
        private set
    var shadeGestures by mutableStateOf(store.getBoolean(KEY_SHADE, false))
        private set
    var softNav by mutableStateOf(store.getBoolean(KEY_NAV, false))
        private set

    fun enableFoldAnimation(value: Boolean) {
        if (store.edit().putBoolean(KEY_FOLD, value).commit()) foldAnimation = value
    }

    fun enableStandby(value: Boolean) {
        if (store.edit().putBoolean(KEY_STANDBY, value).commit()) standby = value
    }

    fun enableLockSurface(value: Boolean) {
        if (store.edit().putBoolean(KEY_LOCK, value).commit()) {
            lockSurface = value
            val wallpaper = com.duofold.launcher.lock.LockWallpaper(app)
            if (value) wallpaper.apply() else wallpaper.clear()
        }
    }

    fun enableLockCoverOnly(value: Boolean) {
        if (store.edit().putBoolean(KEY_LOCK_COVER, value).commit()) lockCoverOnly = value
    }

    fun enableShadeGestures(value: Boolean) {
        if (store.edit().putBoolean(KEY_SHADE, value).commit()) shadeGestures = value
    }

    fun enableSoftNav(value: Boolean) {
        if (store.edit().putBoolean(KEY_NAV, value).commit()) softNav = value
    }

    companion object {
        const val PREFS = "duofold_prefs_v1"
        private const val KEY_FOLD = "fold_anim"
        private const val KEY_STANDBY = "standby"
        private const val KEY_LOCK = "lock_surface"
        private const val KEY_LOCK_COVER = "lock_cover_only"
        private const val KEY_SHADE = "shade_gestures"
        private const val KEY_NAV = "soft_nav"
    }
}
