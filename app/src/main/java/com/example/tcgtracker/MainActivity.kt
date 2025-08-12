package com.example.tcgtracker

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tcgtracker.ui.screens.Navigation
import com.example.tcgtracker.ui.theme.TCGTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: Make it Asynchronous
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TCGTrackerTheme {
                Navigation(applicationContext)
            }
        }
    }
}