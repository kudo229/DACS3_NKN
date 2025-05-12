package com.example.a23it184_nguynkhnhnguyn

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_table")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String,
    var detail: String,
    val image: Int,
    val time: String = "",  // New column: 'time'
    val years: String = ""  // Existing field
)
