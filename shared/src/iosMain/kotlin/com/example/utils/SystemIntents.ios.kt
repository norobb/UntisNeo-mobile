package com.example.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object SystemIntents {
    actual fun openUrl(url: String) {
        val nsUrl = NSURL(string = url)
        if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
        } else {
            println("Cannot open URL: $url")
        }
    }

    actual fun downloadAndInstallApk(url: String) {
        // On iOS, we just open the release URL in Safari and let the user handle it
        openUrl(url)
    }
}
