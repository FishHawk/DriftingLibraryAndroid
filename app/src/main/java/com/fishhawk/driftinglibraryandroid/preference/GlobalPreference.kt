package com.fishhawk.driftinglibraryandroid.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object GlobalPreference {
    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private inline fun <reified T : Enum<T>> preferenceEnumLiveData(key: String, defaultValue: T) =
        PreferenceEnumLiveData(sharedPreferences, key, defaultValue, T::class.java)

    private fun preferenceIntLiveData(key: String, defaultValue: Int) =
        PreferenceIntLiveData(sharedPreferences, key, defaultValue)

    private fun preferenceStringLiveData(key: String, defaultValue: String) =
        PreferenceStringLiveData(sharedPreferences, key, defaultValue)

    private fun preferenceBooleanLiveData(key: String, defaultValue: Boolean) =
        PreferenceBooleanLiveData(sharedPreferences, key, defaultValue)


    // Settings internal
    val selectedServer by lazy {
        preferenceIntLiveData("selected_library", 1)
    }

    val lastUsedProvider by lazy {
        preferenceStringLiveData("last_used_provider", "")
    }


    // Settings general
    enum class StartScreen { LIBRARY, HISTORY, EXPLORE }

    val startScreen by lazy {
        preferenceEnumLiveData("start_screen", StartScreen.LIBRARY)
    }

    enum class Theme { LIGHT, DARK }

    val theme by lazy {
        preferenceEnumLiveData("theme", Theme.LIGHT)
    }

    enum class DisplayMode { GRID, LINEAR }

    val displayMode by lazy {
        preferenceEnumLiveData("display_mode", DisplayMode.GRID)
    }

    enum class HistoryFilter { ALL, FROM_LIBRARY, FROM_SOURCES }

    val historyFilter by lazy {
        preferenceEnumLiveData("history_filter", HistoryFilter.FROM_LIBRARY)
    }

    enum class ChapterDisplayMode { GRID, LINEAR }

    val chapterDisplayMode by lazy {
        preferenceEnumLiveData("chapter_display_mode", ChapterDisplayMode.GRID)
    }

    enum class ChapterDisplayOrder { ASCEND, DESCEND }

    val chapterDisplayOrder by lazy {
        preferenceEnumLiveData("chapter_display_order", ChapterDisplayOrder.ASCEND)
    }


    // Settings reading
    enum class ReadingDirection { LTR, RTL, VERTICAL }

    val readingDirection by lazy {
        preferenceEnumLiveData("reading_direction", ReadingDirection.LTR)
    }

    enum class ScreenOrientation { DEFAULT, LOCK, PORTRAIT, LANDSCAPE }

    val screenOrientation by lazy {
        preferenceEnumLiveData("screen_orientation", ScreenOrientation.DEFAULT)
    }

    val keepScreenOn by lazy {
        preferenceBooleanLiveData("keep_screen_on", false)
    }

    val useVolumeKey by lazy {
        preferenceBooleanLiveData("use_volume_key", false)
    }

    val longTapDialog by lazy {
        preferenceBooleanLiveData("long_tap_dialog", true)
    }

    // Settings advanced
    val secureMode by lazy {
        preferenceBooleanLiveData("secure_mode", false)
    }
}