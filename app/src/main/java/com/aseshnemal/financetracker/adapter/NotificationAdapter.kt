package com.aseshnemal.financetracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aseshnemal.financetracker.R
import com.aseshnemal.financetracker.model.Notification

class NotificationAdapter(private val notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivNotificationIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvNotificationTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotificationMessage)
        val tvDate: TextView = view.findViewById(R.id.tvNotificationDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message
        holder.tvDate.text = notification.date

        // Set icon based on notification type
        holder.ivIcon.setImageResource(
            when {
                notification.title.contains("Budget", ignoreCase = true) -> 
                    android.R.drawable.ic_dialog_alert
                notification.title.contains("Spending", ignoreCase = true) -> 
                    android.R.drawable.ic_menu_report_image
                else -> android.R.drawable.ic_dialog_info
            }
        )
    }

    override fun getItemCount() = notifications.size
} 