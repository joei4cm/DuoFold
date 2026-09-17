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
    var foldAmount by mutableFloatStateOf(0f)
        private set

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
        if (values.isEmpty()) return
        hingeDegrees = values[0]
        val open = hingeDegrees.coerceIn(0f, 180f)
        val raw = ((175f - open) / 145f).coerceIn(0f, 1f)
        foldAmount += (raw - foldAmount) * 0.22f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
