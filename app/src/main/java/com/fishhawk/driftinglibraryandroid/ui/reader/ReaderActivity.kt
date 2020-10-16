package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.databinding.ActivityReaderBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.extension.setupFullScreen
import com.fishhawk.driftinglibraryandroid.ui.extension.setupThemeWithTranslucentStatus

class ReaderActivity : AppCompatActivity() {
    lateinit var binding: ActivityReaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupThemeWithTranslucentStatus()
        super.onCreate(savedInstanceState)
        setupFullScreen()

        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SettingsHelper.screenOrientation.observe(this) {
            val newOrientation = when (it) {
                SettingsHelper.ScreenOrientation.DEFAULT -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                SettingsHelper.ScreenOrientation.LOCK -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
                SettingsHelper.ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                SettingsHelper.ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            if (newOrientation != requestedOrientation) {
                requestedOrientation = newOrientation
            }
        }

        SettingsHelper.keepScreenOn.observe(this) {
            val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            if (it) window.addFlags(flag)
            else window.clearFlags(flag)
        }

        SettingsHelper.secureMode.observe(this) {
            val flag = WindowManager.LayoutParams.FLAG_SECURE
            if (it) window.addFlags(flag)
            else window.clearFlags(flag)
        }
    }
}
