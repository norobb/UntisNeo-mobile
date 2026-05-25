package com.example.utils

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationOptionBadge

actual class NotificationPlatformHelper actual constructor() {
    
    actual fun initChannels() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, error ->
            if (error != null) {
                println("NotificationAuthorizationError: \${error.localizedDescription}")
            }
        }
    }

    actual fun showNotification(title: String, content: String) {
        val notificationContent = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(content)
        }
        
        // Show immediately (1 second delay is the minimum for UNTimeIntervalNotificationTrigger)
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "untisneo_\${platform.Foundation.NSUUID().UUIDString()}",
            content = notificationContent,
            trigger = trigger
        )
        
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("NotificationAddError: \${error.localizedDescription}")
            }
        }
    }
}
