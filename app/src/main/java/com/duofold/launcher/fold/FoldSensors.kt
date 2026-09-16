package com.duofold.launcher.fold

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FoldSensors(context: Context) : SensorEventListener {
    private val app = context.applicationContext
    private val sensors = app.getSystemService(SensorManager::class.java)
    private val hinge = sensors.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)

    var hingeDegrees by mutableFloatStateOf(180f)
        private set
    var panel by mutableStateOf(PanelKind.Unknown)
        private set

    /** 0 = fully open (flat), 1 = closed-looking for our wipe effect. */
    val foldAmount: Float
        get() {
            val open = hingeDegrees.coerceIn(0f, 180f)
            // Map 180° open → 0 effect, ~0–30° closed → strong effect.
            return ((180f - open) / 150f).coerceIn(0f, 1f)
        }

    fun start() {
        refreshPanel()
        hinge?.let { sensors.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stop() {
        sensors.unregisterListener(this)
    }

    fun refreshPanel() {
        val bounds = app.getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
        val density = app.resources.displayMetrics.density
        panel = classifyPanel(bounds.width(), bounds.height(), density)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return
        if (values.isNotEmpty()) hingeDegrees = values[0]
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
