package com.myapplication

import MainView
import android.os.Bundle
import androidx.activity.compose.setContent
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

import com.example.utils.AppGlobals

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGlobals.appContext = this.applicationContext

        setContent {
            MainView()
        }
    }
}