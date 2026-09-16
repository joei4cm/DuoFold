package com.duofold.launcher.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.duofold.launcher.R
import com.duofold.launcher.assist.AssistAction
import com.duofold.launcher.assist.AssistResult
import com.duofold.launcher.assist.AssistService

private val mainHandler = Handler(Looper.getMainLooper())

/** Top-edge swipe: left ~70% notifications, right ~30% quick settings. */
@Composable
fun ShadePullHost(
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!enabled) return
    val context = LocalContext.current
    var accum by remember { mutableFloatStateOf(0f) }
    var startX by remember { mutableFloatStateOf(0f) }
    Box(
        modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        accum = 0f
                        startX = offset.x
                    },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        accum += amount
                    },
                    onDragEnd = {
                        if (accum <= 56f) return@detectVerticalDragGestures
                        val qs = startX > size.width * 0.7f
                        requestAssist(
                            context,
                            if (qs) AssistAction.QUICK_SETTINGS else AssistAction.NOTIFICATIONS,
                        )
                    },
                )
            },
    )
}

@Composable
fun SoftNavBar(
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!enabled) return
    val context = LocalContext.current
    Row(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 8.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.28f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { requestAssist(context, AssistAction.BACK) }) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.nav_back), tint = Color.White, modifier = Modifier.size(22.dp))
        }
        IconButton(onClick = { requestAssist(context, AssistAction.HOME) }) {
            Icon(Icons.Rounded.Circle, stringResource(R.string.nav_home), tint = Color.White, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = { requestAssist(context, AssistAction.RECENTS) }) {
            Icon(Icons.Rounded.CropSquare, stringResource(R.string.nav_recents), tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

fun requestAssist(context: Context, action: AssistAction, retry: Boolean = true) {
    when (AssistService.perform(context, action)) {
        AssistResult.DONE -> Unit
        AssistResult.STARTING -> {
            Toast.makeText(context, R.string.assist_starting, Toast.LENGTH_SHORT).show()
            if (retry) {
                mainHandler.postDelayed({ requestAssist(context, action, retry = false) }, 450L)
            } else if (!AssistService.enabled(context)) {
                Toast.makeText(context, R.string.assist_enable_toast, Toast.LENGTH_LONG).show()
                AssistService.openSettings(context)
            } else {
                Toast.makeText(context, R.string.assist_rejected, Toast.LENGTH_SHORT).show()
            }
        }
        AssistResult.NEED_ENABLE -> {
            Toast.makeText(context, R.string.assist_enable_toast, Toast.LENGTH_LONG).show()
            AssistService.openSettings(context)
        }
        AssistResult.REJECTED ->
            Toast.makeText(context, R.string.assist_rejected, Toast.LENGTH_SHORT).show()
    }
}
