package com.duofold.launcher.assist

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import java.lang.ref.WeakReference

/** Shade / soft-nav actions DuoFold may request. */
enum class AssistAction {
    NOTIFICATIONS,
    QUICK_SETTINGS,
    HOME,
    BACK,
    RECENTS,
}

enum class AssistResult {
    DONE,
    NEED_ENABLE,
    STARTING,
    REJECTED,
}

/**
 * Minimal AccessibilityService: no event observation, only global actions the user opts into.
 * Original DuoFold assist surface for HyperOS shade + optional soft nav.
 */
class AssistService : AccessibilityService() {
    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply { eventTypes = 0 }
        live = WeakReference(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (live.get() === this) live.clear()
        super.onDestroy()
    }

    companion object {
        private var live = WeakReference<AssistService>(null)

        fun connected(): Boolean = live.get() != null

        fun enabled(context: Context): Boolean {
            val want = ComponentName(context, AssistService::class.java)
            val am = context.getSystemService(AccessibilityManager::class.java) ?: return false
            return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any {
                    val info = it.resolveInfo?.serviceInfo ?: return@any false
                    ComponentName(info.packageName, info.name) == want
                }
        }

        fun perform(context: Context, action: AssistAction): AssistResult {
            val service = live.get()
            if (service != null) {
                val code = when (action) {
                    AssistAction.NOTIFICATIONS -> GLOBAL_ACTION_NOTIFICATIONS
                    AssistAction.QUICK_SETTINGS -> GLOBAL_ACTION_QUICK_SETTINGS
                    AssistAction.HOME -> GLOBAL_ACTION_HOME
                    AssistAction.BACK -> GLOBAL_ACTION_BACK
                    AssistAction.RECENTS -> GLOBAL_ACTION_RECENTS
                }
                return if (service.performGlobalAction(code)) AssistResult.DONE else AssistResult.REJECTED
            }
            return if (enabled(context)) AssistResult.STARTING else AssistResult.NEED_ENABLE
        }

        fun openSettings(context: Context) {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
