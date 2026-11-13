package com.kshrd.starter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.kshrd.coreui.theme.AppTheme
import com.kshrd.starter.ui.navigation.MainGraph
import com.kshrd.starter.ui.splash.AppSplash
import dagger.hilt.android.AndroidEntryPoint

/** The single activity. It applies the theme, hosts the navigation graph and plays the launch splash. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only on a fresh launch; not after rotation or when the process is restored.
        val freshLaunch = savedInstanceState == null
        setContent {
            AppTheme {
                Surface {
                    var showSplash by rememberSaveable { mutableStateOf(freshLaunch) }
                    Box {
                        MainGraph(navController = rememberNavController())
                        if (showSplash) AppSplash(onFinished = { showSplash = false })
                    }
                }
            }
        }
    }
}
