package com.myapplication

import MainView
import android.os.Bundle
import androidx.activity.compose.setContent
import android.content.Context

object AppGlobals {
    var appContext: Context? = null
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGlobals.appContext = this.applicationContext

        setContent {
            MainView()
        }
    }
}