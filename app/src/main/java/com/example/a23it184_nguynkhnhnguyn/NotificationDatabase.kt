package com.example.a23it184_nguynkhnhnguyn

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Define the NotificationDatabase class which extends RoomDatabase
@Database(entities = [Notification::class], version = 3, exportSchema = true)
abstract class NotificationDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: NotificationDatabase? = null

        fun getDatabase(context: Context): NotificationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotificationDatabase::class.java,
                    "notification_database"
                )
                    .addMigrations(MIGRATION_2_3)  // Add migration
                    .build()

                INSTANCE = instance
                instance
            }
        }
        // Migration from version 2 to 3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the 'time' column
                database.execSQL("ALTER TABLE notification_table ADD COLUMN time TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
