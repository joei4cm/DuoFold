package com.duofold.launcher.ui

import android.appwidget.AppWidgetHostView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.duofold.launcher.R
import com.duofold.launcher.model.WidgetPlacement
import com.duofold.launcher.widgets.WidgetController
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WidgetSurface(
    placement: WidgetPlacement,
    controller: WidgetController,
    modifier: Modifier = Modifier,
) {
    if (placement.isBuiltin) {
        BuiltinGlance(placement.provider.removePrefix("builtin:"), modifier)
    } else {
        NativeWidgetView(placement.appWidgetId, controller, modifier)
    }
}

@Composable
private fun NativeWidgetView(appWidgetId: Int, controller: WidgetController, modifier: Modifier) {
    val hostView = remember(appWidgetId) { controller.createHostView(appWidgetId) }
    if (hostView == null) {
        GlassPanel(modifier) {
            Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.widget_missing), color = Color.White.copy(0.7f))
            }
        }
        return
    }
    AndroidView(
        factory = { context ->
            FrameLayout(context).also { frame ->
                (hostView.parent as? ViewGroup)?.removeView(hostView)
                frame.addView(
                    hostView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
            }
        },
        update = { frame ->
            val child = frame.getChildAt(0) as? AppWidgetHostView
            child?.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        },
        modifier = modifier,
    )
    DisposableEffect(appWidgetId) { onDispose { } }
}

@Composable
fun BuiltinGlance(kind: String, modifier: Modifier = Modifier) {
    val now = remember { Date() }
    GlassPanel(modifier) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            when (kind) {
                "clock" -> {
                    Text(
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(now),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Light,
                    )
                    Text(
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(now),
                        color = Color.White.copy(0.7f),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                "date" -> {
                    Text(stringResource(R.string.glance_date), color = Color.White.copy(0.65f), style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        SimpleDateFormat("MMM d", Locale.getDefault()).format(now),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                "battery" -> {
                    val pct = rememberBatteryLevel()
                    Text(stringResource(R.string.glance_battery), color = Color.White.copy(0.65f), style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("$pct%", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }
                else -> Text(kind, color = Color.White)
            }
        }
    }
}

@Composable
private fun rememberBatteryLevel(): Int {
    val context = LocalContext.current
    return remember {
        val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, 100) ?: 100
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100) ?: 100
        if (scale > 0) (level * 100) / scale else 100
    }
}
