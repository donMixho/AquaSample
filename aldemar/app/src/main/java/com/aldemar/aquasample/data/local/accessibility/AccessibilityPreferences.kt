package com.aldemar.aquasample.data.local.accessibility

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "aqua_accessibility_prefs"
private const val KEY_FONT_SIZE_SCALE = "key_font_size_scale"
private const val KEY_COLOR_BLINDNESS_MODE = "key_color_blindness_mode"

/**
 * Gestor de preferencias de accesibilidad con persistencia local mediante SharedPreferences.
 */
class AccessibilityPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<AccessibilityState> = _state.asStateFlow()

    private fun loadState(): AccessibilityState {
        val fontSizeName = prefs.getString(KEY_FONT_SIZE_SCALE, FontSizeScale.NORMAL.name)
        val colorBlindnessName = prefs.getString(KEY_COLOR_BLINDNESS_MODE, ColorBlindnessMode.NONE.name)

        return AccessibilityState(
            colorBlindnessMode = ColorBlindnessMode.fromName(colorBlindnessName),
            fontSizeScale = FontSizeScale.fromName(fontSizeName)
        )
    }

    fun setFontSizeScale(scale: FontSizeScale) {
        prefs.edit().putString(KEY_FONT_SIZE_SCALE, scale.name).apply()
        _state.value = _state.value.copy(fontSizeScale = scale)
    }

    fun setColorBlindnessMode(mode: ColorBlindnessMode) {
        prefs.edit().putString(KEY_COLOR_BLINDNESS_MODE, mode.name).apply()
        _state.value = _state.value.copy(colorBlindnessMode = mode)
    }
}
