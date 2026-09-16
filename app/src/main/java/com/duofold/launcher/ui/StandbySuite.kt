package com.duofold.launcher.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duofold.launcher.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.timer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandbySuite(onExit: () -> Unit) {
    val pager = rememberPagerState(pageCount = { 3 })
    var now by remember { mutableStateOf(Date()) }
    DisposableEffect(Unit) {
        val t = timer(period = 15_000L) { now = Date() }
        onDispose { t.cancel() }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF070B10), Color(0xFF15202A), Color(0xFF243440)))
            )
            .clickable(onClick = onExit)
            .systemBarsPadding()
            .padding(28.dp),
    ) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> StandbyClockPage(now)
                1 -> StandbyPhotoPage()
                else -> StandbyGlancePage(now)
            }
        }
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .size(if (pager.currentPage == i) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (pager.currentPage == i) 0.95f else 0.35f))
                )
            }
        }
    }
}

@Composable
private fun StandbyClockPage(now: Date) {
    val time = remember(now) { DateFormat.getTimeInstance(DateFormat.SHORT).format(now) }
    val date = remember(now) { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(now) }
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(time, color = Color.White, fontSize = 84.sp, fontWeight = FontWeight.ExtraLight, letterSpacing = (-1.5).sp)
        Spacer(Modifier.height(10.dp))
        Text(date, color = Color.White.copy(0.72f), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(28.dp))
        BatteryChip()
    }
}

@Composable
private fun StandbyPhotoPage() {
    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF3A4A5C), Color(0xFF8FA0AE), Color(0xFFD7C4A8)))
            ),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            stringResource(R.string.standby_photo_hint),
            color = Color.White.copy(0.9f),
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun StandbyGlancePage(now: Date) {
    val hour = remember(now) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) }
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        GlanceCard(stringResource(R.string.glance_time), hour)
        GlanceCard(stringResource(R.string.glance_focus), stringResource(R.string.glance_focus_body))
        GlanceCard(stringResource(R.string.glance_weather), stringResource(R.string.weather_sample))
    }
}

@Composable
private fun GlanceCard(title: String, body: String) {
    GlassPanel(Modifier.fillMaxWidth(), radius = 22.dp) {
        Column(Modifier.padding(18.dp)) {
            Text(title, color = Color.White.copy(0.65f), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Text(body, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun BatteryChip() {
    val context = LocalContext.current
    val pct = rememberBatteryPercent(context)
    GlassPanel(radius = 40.dp) {
        Text(
            stringResource(R.string.battery_pct, pct),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun rememberBatteryPercent(context: Context): Int {
    var value by remember { mutableIntStateOf(100) }
    DisposableEffect(context) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = context.registerReceiver(null, filter)
        fun read(intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            if (level >= 0 && scale > 0) value = (level * 100) / scale
        }
        read(sticky)
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) = read(intent)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }
    return value
}
