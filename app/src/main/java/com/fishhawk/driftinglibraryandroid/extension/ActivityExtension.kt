package com.fishhawk.driftinglibraryandroid.extension;

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainActivity
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.base.ListEmptyNotification
import com.fishhawk.driftinglibraryandroid.ui.base.ListReachEndNotification
import com.fishhawk.driftinglibraryandroid.ui.base.NetworkErrorNotification
import com.fishhawk.driftinglibraryandroid.ui.base.Notification
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryActivity
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderActivity
import java.lang.Error
import java.lang.reflect.Method


fun Activity.navToGalleryActivity(
    id: String,
    title: String,
    thumb: String,
    source: String?
) {
    val bundle = bundleOf(
        "id" to id,
        "title" to title,
        "thumb" to thumb,
        "source" to source
    )

    val intent = Intent(this, GalleryActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun Activity.navToReaderActivity(
    id: String,
    source: String?,
    collectionIndex: Int = 0,
    chapterIndex: Int = 0,
    pageIndex: Int = 0
) {
    val bundle = bundleOf(
        "id" to id,
        "source" to source,
        "collectionIndex" to collectionIndex,
        "chapterIndex" to chapterIndex,
        "pageIndex" to pageIndex
    )

    val intent = Intent(this, ReaderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun Activity.navToMainActivity(
    filter: String
) {
    val bundle = bundleOf("filter" to filter)
    val intent = Intent(this, MainActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

private fun AppCompatActivity.getThemeResId(): Int {
    val wrapper: Class<*> = Context::class.java
    val method: Method = wrapper.getMethod("getThemeResId")
    method.isAccessible = true
    return method.invoke(this) as Int
}

fun AppCompatActivity.setupFullScreen() {
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (getThemeResId() == R.style.Theme_App_Light_TranslucentStatus)
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    }
}

fun AppCompatActivity.setupThemeWithTranslucentStatus() {
    val lightThemeId = R.style.Theme_App_Light_TranslucentStatus
    val darkThemeId = R.style.Theme_App_Dark_TranslucentStatus

    when (SettingsHelper.theme.getValueDirectly()) {
        SettingsHelper.THEME_LIGHT -> setTheme(lightThemeId)
        SettingsHelper.THEME_DARK -> setTheme(darkThemeId)
    }

    SettingsHelper.theme.observe(this, Observer {
        val themeId = when (it) {
            SettingsHelper.THEME_LIGHT -> lightThemeId
            SettingsHelper.THEME_DARK -> darkThemeId
            else -> lightThemeId
        }
        if (getThemeResId() != themeId) {
            recreate()
        }
    })
}

fun AppCompatActivity.setupTheme() {
    val lightThemeId = R.style.Theme_App_Light
    val darkThemeId = R.style.Theme_App_Dark

    when (SettingsHelper.theme.getValueDirectly()) {
        SettingsHelper.THEME_LIGHT -> setTheme(lightThemeId)
        SettingsHelper.THEME_DARK -> setTheme(darkThemeId)
    }

    SettingsHelper.theme.observe(this, Observer {
        val themeId = when (it) {
            SettingsHelper.THEME_LIGHT -> lightThemeId
            SettingsHelper.THEME_DARK -> darkThemeId
            else -> throw InternalError()
        }
        if (getThemeResId() != themeId) {
            recreate()
        }
    })
}

fun Activity.getNotificationMessage(notification: Notification): String {
    return when (notification) {
        is ListEmptyNotification -> getString(R.string.error_hint_empty_refresh_result)
        is ListReachEndNotification -> getString(R.string.error_hint_empty_fetch_more_result)
        is NetworkErrorNotification ->
            notification.throwable.message ?: getString(R.string.library_unknown_error_hint)
        else -> getString(R.string.library_unknown_error_hint)
    }
}


