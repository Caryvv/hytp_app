package com.example.hytp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hytp.navigation.HytpNavHost
import com.example.hytp.ui.theme.HytpTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 单 Activity，承载全局 NavHost（对齐 docs/dev/04 §1）。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HytpTheme {
                HytpNavHost()
            }
        }
    }
}
