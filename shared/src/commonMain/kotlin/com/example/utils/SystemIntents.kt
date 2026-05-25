package com.example.utils

expect object SystemIntents {
    fun openUrl(url: String)
    fun downloadAndInstallApk(url: String)
}
