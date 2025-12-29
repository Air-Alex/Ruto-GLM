package com.rosan.ruto.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rosan.ruto.ui.NavGraph
import com.rosan.ruto.ui.compose.theme.RutoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RutoTheme {
                NavGraph()
            }
        }
    }
}