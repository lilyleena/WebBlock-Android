package com.kshrd.starter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.kshrd.coreui.theme.AppTheme
import com.kshrd.starter.ui.navigation.MainGraph
import dagger.hilt.android.AndroidEntryPoint

/** The single activity. It only applies the theme and hosts the navigation graph. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface {
                    MainGraph(navController = rememberNavController())
                }
            }
        }
    }
}
