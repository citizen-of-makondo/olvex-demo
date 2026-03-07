package dev.olvex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.olvex.core.AndroidOlvex
import dev.olvex.demo.App
import dev.olvex.demo.DemoConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidOlvex.init(
            context = this,
            apiKey = DemoConfig.apiKey // Get your key at olvex.dev
        )

        setContent {
            App()
        }
    }
}
