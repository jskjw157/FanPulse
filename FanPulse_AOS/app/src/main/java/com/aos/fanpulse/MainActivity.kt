package com.aos.fanpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aos.fanpulse.presentation.MainScreen
import com.aos.fanpulse.presentation.ui.theme.FanPulseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  //  edge to edge 설정
        setContent {
            FanPulseTheme {
                MainScreen()
            }
        }
    }
}