package com.example.a23it184_nguynkhnhnguyn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import android.content.pm.PackageManager
import android.Manifest
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.*


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")  // Lấy tiêu đề thông báo
        val detail = intent.getStringExtra("detail")  // Lấy chi tiết thông báo

        // Log the data being received
        Log.d("NotificationReceiver", "Title: $title, Detail: $detail")

        if (title.isNullOrEmpty() || detail.isNullOrEmpty()) {
            Log.e("NotificationReceiver", "Title or Detail is empty or null!")
            return
        }

        // Check if the app has permission to post notifications
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {

            Log.e("NotificationReceiver", "Permission to post notifications not granted!")
            // Optionally, you can handle this case by requesting permission or showing a Toast.
            return
        }

        val notificationId = System.currentTimeMillis().toInt()

        // Create notification
        val notification = NotificationCompat.Builder(context, "default_channel")
            .setContentTitle(title)  // Set title for notification
            .setContentText(detail)  // Set detail for notification
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Post notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notification)

        // Show Toast message
        Toast.makeText(context, "Thông báo: $title", Toast.LENGTH_SHORT).show()
    }
}


