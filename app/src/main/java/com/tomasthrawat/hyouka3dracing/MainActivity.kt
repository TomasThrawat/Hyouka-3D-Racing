package com.tomasthrawat.hyouka3dracing

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    private var screen by mutableStateOf(Screen.MENU)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RacingGame(screen = screen, onScreen = { screen = it })
        }
    }

    enum class Screen { MENU, MAPS, RACE }
}
