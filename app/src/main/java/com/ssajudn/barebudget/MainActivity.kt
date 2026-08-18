package com.ssajudn.barebudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ssajudn.barebudget.ui.navigation.AppNavigation
import com.ssajudn.barebudget.ui.theme.BareBudgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BareBudgetTheme {
                AppNavigation()
            }
        }
    }
}