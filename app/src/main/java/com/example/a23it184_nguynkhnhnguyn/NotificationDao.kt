package com.example.a23it184_nguynkhnhnguyn

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NotificationDao {

    @Insert
    suspend fun insertNotification(notification: Notification)

    @Query("SELECT * FROM notification_table")
    suspend fun getAllNotifications(): List<Notification>

    @Query("DELETE FROM notification_table WHERE id = :id")
    suspend fun deleteNotification(id: Int)

    @Update
    suspend fun updateNotification(notification: Notification)  // Update function
}
