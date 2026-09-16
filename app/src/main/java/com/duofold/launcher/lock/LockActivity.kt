package com.duofold.launcher.lock

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.app.WallpaperManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.duofold.launcher.MainActivity
import com.duofold.launcher.R
import com.duofold.launcher.data.FeaturePrefs
import com.duofold.launcher.fold.PanelKind
import com.duofold.launcher.fold.classifyPanel
import com.duofold.launcher.ui.DuoFoldTheme
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.util.Date

class LockActivity : FragmentActivity() {
    private val prefs by lazy { FeaturePrefs(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!prefs.lockSurface) {
            finish(); return
        }
        if (prefs.lockCoverOnly && currentPanel() != PanelKind.Cover) {
            finish(); return
        }
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        enableEdgeToEdge()
        setContent {
            DuoFoldTheme {
                val time = remember { DateFormat.getTimeInstance(DateFormat.SHORT).format(Date()) }
                val date = remember { DateFormat.getDateInstance(DateFormat.FULL).format(Date()) }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B2430), Color(0xFF3A4A5C), Color(0xFF6B7C8F))
                            )
                        )
                        .systemBarsPadding()
                        .padding(28.dp),
                ) {
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(time, color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Light)
                        Text(date, color = Color.White.copy(0.85f), style = MaterialTheme.typography.titleMedium)
                        Button(
                            onClick = { authenticate() },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.padding(top = 20.dp),
                        ) { Text(stringResource(R.string.unlock)) }
                        Text(
                            stringResource(R.string.lock_surface_detail),
                            color = Color.White.copy(0.55f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                }
            }
        }
    }

    private fun currentPanel(): PanelKind {
        val bounds = getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
        return classifyPanel(bounds.width(), bounds.height(), resources.displayMetrics.density)
    }

    private fun authenticate() {
        val can = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        if (can != BiometricManager.BIOMETRIC_SUCCESS &&
            getSystemService(KeyguardManager::class.java)?.isDeviceSecure != true
        ) {
            Toast.makeText(this, R.string.lock_auth_unavailable, Toast.LENGTH_LONG).show()
            return
        }
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    startActivity(
                        Intent(this@LockActivity, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        Toast.makeText(this@LockActivity, errString, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    Toast.makeText(this@LockActivity, R.string.lock_auth_failed, Toast.LENGTH_SHORT).show()
                }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.lock_auth_title))
                .setSubtitle(getString(R.string.lock_auth_subtitle))
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        )
    }

    companion object {
        fun maybeLaunch(context: Context) {
            val prefs = FeaturePrefs(context)
            if (!prefs.lockSurface) return
            val keyguard = context.getSystemService(KeyguardManager::class.java)
            val locked = keyguard?.isKeyguardLocked == true && keyguard.isDeviceSecure
            if (locked) return
            val bounds = context.getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
            val panel = classifyPanel(bounds.width(), bounds.height(), context.resources.displayMetrics.density)
            if (prefs.lockCoverOnly && panel != PanelKind.Cover) return
            context.startActivity(
                Intent(context, LockActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }
}

class UnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_USER_PRESENT) return
        LockActivity.maybeLaunch(context.applicationContext)
    }
}

class LockWallpaper(private val context: Context) {
    fun apply(): Boolean = runCatching {
        val bitmap = render(1080, 1920)
        val manager = WallpaperManager.getInstance(context)
        val bytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }
        val stream = ByteArrayInputStream(bytes)
        if (Build.VERSION.SDK_INT >= 24) {
            manager.setStream(stream, null, true, WallpaperManager.FLAG_LOCK)
        } else {
            manager.setStream(stream)
        }
        true
    }.getOrDefault(false)

    fun clear(): Boolean = runCatching {
        if (Build.VERSION.SDK_INT >= 28) {
            WallpaperManager.getInstance(context).clear(WallpaperManager.FLAG_LOCK)
            true
        } else false
    }.getOrDefault(false)

    companion object {
        fun render(width: Int, height: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF1B2430.toInt(), 0xFF3A4A5C.toInt(), 0xFF6B7C8F.toInt()),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            return bitmap
        }
    }
}

/** When the optional lock surface should present after system unlock. */
fun shouldOfferLockSurface(
    enabled: Boolean,
    coverOnly: Boolean,
    isCover: Boolean,
    keyguardStillLocked: Boolean,
): Boolean {
    if (!enabled || keyguardStillLocked) return false
    if (coverOnly && !isCover) return false
    return true
}

fun estimateCover(widthPx: Int, heightPx: Int, density: Float): Boolean =
    classifyPanel(widthPx, heightPx, density) == PanelKind.Cover
