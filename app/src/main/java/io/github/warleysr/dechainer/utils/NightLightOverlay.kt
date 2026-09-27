package io.github.warleysr.dechainer.utils

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import kotlin.math.ln
import kotlin.math.roundToInt

class NightLightOverlay(private val service: AccessibilityService) {
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var view: View? = null

    fun show(temperature: Int, intensity: Int) {
        val color = tint(temperature, intensity)
        view?.let {
            it.setBackgroundColor(color)
            return
        }

        val overlay = View(service).apply { setBackgroundColor(color) }
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            LayoutParams.FLAG_NOT_TOUCHABLE or LayoutParams.FLAG_NOT_FOCUSABLE or
                LayoutParams.FLAG_LAYOUT_IN_SCREEN or LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            fitInsetsTypes = 0
        }
        windowManager.addView(overlay, params)
        view = overlay
    }

    fun hide() {
        view?.let { windowManager.removeView(it) }
        view = null
    }

    // Modelled on an overlay app whose 3200K / 5% filter came closest to Samsung's eye comfort
    // shield: it turned white into (252, 249, 244) and black into (8, 5, 0), which is (185, 116, 0)
    // at alpha 11. Other temperatures keep that colour's blue-free, darkened take on the blackbody
    // hue, and the intensity is its opacity on the same scale.
    private fun tint(temperature: Int, intensity: Int): Int {
        val alpha = (intensity.coerceIn(0, 100) * MAX_ALPHA / 100f).roundToInt()
        val green = blackbodyGreen(temperature) * GREEN_SCALE
        return Color.argb(alpha, RED, green.roundToInt().coerceIn(0, 255), 0)
    }

    // Tanner Helland's blackbody approximation; red is saturated below 6600K.
    private fun blackbodyGreen(kelvin: Int): Float {
        val t = kelvin.coerceIn(1000, 6600) / 100f
        return (99.4708025861f * ln(t) - 161.1195681661f).coerceIn(0f, 255f)
    }

    private companion object {
        const val MAX_ALPHA = 220
        const val RED = 185
        const val GREEN_SCALE = 116f / 184f
    }
}
