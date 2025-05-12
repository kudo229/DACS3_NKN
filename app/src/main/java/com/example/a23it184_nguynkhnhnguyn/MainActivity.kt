package com.example.a23it184_nguynkhnhnguyn

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a23it184_nguynkhnhnguyn.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.*
import android.app.TimePickerDialog
import android.provider.Settings
import android.content.pm.PackageManager
import android.Manifest
import android.app.AlertDialog
import android.util.Log
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationDao by lazy { NotificationDatabase.getDatabase(this).notificationDao() }
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Kiểm tra và yêu cầu quyền POST_NOTIFICATIONS và SCHEDULE_EXACT_ALARM
        checkPermissions()

        // Initialize RecyclerView Adapter
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(
            this,
            mutableListOf(),
            { notification -> deleteNotification(notification) },
            { notification -> editNotification(notification) }
        )
        binding.rvUsers.adapter = notificationAdapter

        // Handle selecting a time
        binding.btnSetTime.setOnClickListener {
            showDatePickerDialog()  // Show the date picker
        }

// Inside MainActivity when setting the notification
        binding.them.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val detail = binding.etDetail.text.toString().trim()

            if (name.isEmpty() || detail.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val notification = Notification(
                title = name,
                detail = detail,
                image = R.drawable.chuong,
                time = selectedTime
            )

            lifecycleScope.launch {
                notificationDao.insertNotification(notification)
                Toast.makeText(this@MainActivity, "Đã thêm thông báo", Toast.LENGTH_SHORT).show()
                loadNotifications()  // Refresh the notification list
            }

            // Set alarm if the time is selected
            if (selectedTime.isNotEmpty()) {
                setNotificationAlarm(notification)
            }
        }


        loadNotifications()  // Load notifications on start
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        selectedTime = "$year-${month + 1}-$dayOfMonth $hourOfDay:$minute:00"
                        Toast.makeText(this, " $selectedTime", Toast.LENGTH_SHORT).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun setNotificationAlarm(notification: Notification) {
        val calendar = Calendar.getInstance()

        // Split selectedTime into date and time
        val timeParts = selectedTime.split(" ")
        val dateParts = timeParts[0].split("-")  // Year-Month-Day
        val timePartsArray = timeParts[1].split(":")  // Hour:Minute

        // Set values to Calendar
        calendar.set(
            dateParts[0].toInt(),  // Year
            dateParts[1].toInt() - 1,  // Month (0-indexed, subtract 1)
            dateParts[2].toInt(),  // Day of the month
            timePartsArray[0].toInt(),  // Hour
            timePartsArray[1].toInt(),  // Minute
            0  // Second (set to 0)
        )

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("title", notification.title)  // Pass title
            putExtra("detail", notification.detail)  // Pass detail
        }

        // Log the title and detail before sending to NotificationReceiver
        Log.d("MainActivity", "Sending title: ${notification.title}, detail: ${notification.detail}")

        // Create a unique notification ID using the current time
        val notificationId = System.currentTimeMillis().toInt()

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            this, notificationId, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Check if the app can schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // If not granted, request user to enable exact alarm permission
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return  // Exit the method if permission isn't granted
            }
        }

        try {
            // Schedule the alarm if permission is granted
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
            Toast.makeText(this, "Giờ thông báo: $selectedTime", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            // Handle exception if permission was denied or if there's another issue
            Log.e("MainActivity", "SecurityException while setting alarm: ${e.message}")
            Toast.makeText(this, "Permission to set exact alarm denied.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            val notifications = notificationDao.getAllNotifications()
            notificationAdapter.updateNotifications(notifications)
        }
    }

    private fun deleteNotification(notification: Notification) {
        lifecycleScope.launch {
            notificationDao.deleteNotification(notification.id)
            loadNotifications()  // Refresh the list
            Toast.makeText(this@MainActivity, "Đã xóa thông báo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editNotification(notification: Notification) {
        // Create a dialog to edit the notification details
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_notification, null) // Inflate the custom layout

        // Find EditText views from the dialog layout
        val editTitle = dialogView.findViewById<EditText>(R.id.editTitle)
        val editDetail = dialogView.findViewById<EditText>(R.id.editDetail)

        // Set the current values of title and detail in the EditTexts
        editTitle.setText(notification.title)
        editDetail.setText(notification.detail)

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa Thông báo")
            .setView(dialogView)  // Set the custom view with the EditTexts
            .setPositiveButton("Lưu") { dialogInterface, _ ->
                val newTitle = editTitle.text.toString()  // Get the new title
                val newDetail = editDetail.text.toString()  // Get the new detail

                if (newTitle.isNotEmpty() && newDetail.isNotEmpty()) {
                    // Update the notification object
                    notification.title = newTitle
                    notification.detail = newDetail

                    // Update the notification in the database
                    lifecycleScope.launch {
                        notificationDao.updateNotification(notification)
                        loadNotifications()  // Refresh the list
                        Toast.makeText(this@MainActivity, "Đã cập nhập thông báo!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter both title and detail", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Thoát", null)
            .create()

        dialog.show()  // Show the dialog
    }



    // Kiểm tra quyền POST_NOTIFICATIONS và SCHEDULE_EXACT_ALARM cho Android 13+
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM), 102)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {  // Notification permission
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền thông báo đã được cấp", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Quyền thông báo bị từ chối", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == 102) {  // Alarm scheduling permission
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Zooo 01", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Zooo 00", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
