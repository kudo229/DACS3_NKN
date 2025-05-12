package com.example.a23it184_nguynkhnhnguyn

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import android.app.PendingIntent
import android.content.Intent
import android.app.NotificationManager
import androidx.core.app.NotificationCompat


class NotificationAdapter(
    private val context: Context,
    private var notifications: List<Notification>,
    private val onDelete: (Notification) -> Unit,
    private val onEdit: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val titleTextView: TextView = view.findViewById(R.id.tvTen)  // Notification Title
        val idTextView: TextView = view.findViewById(R.id.tvMa)      // Notification ID
        val detailTextView: TextView = view.findViewById(R.id.tvChiTiet) // Notification Details
        val yearTextView: TextView = view.findViewById(R.id.tvTime)  // Notification Time
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        // Bind notification data to the views
        holder.imageView.setImageResource(notification.image)  // Set image dynamically (use resource ID)
        holder.titleTextView.text = "${notification.title}"  // Display notification title
        holder.idTextView.text = "ID: ${notification.id}"  // Display notification ID
        holder.detailTextView.text = "Chi tiết: ${notification.detail}"  // Display notification details
        holder.yearTextView.text = "${notification.time}"  // Display the time field



        // Set click listeners for edit and delete actions
        holder.btnEdit.setOnClickListener { onEdit(notification) }
        holder.btnDelete.setOnClickListener { onDelete(notification) }
    }

    override fun getItemCount(): Int = notifications.size
}

