package com.example.travel_itinerary_planner.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.travel_itinerary_planner.R

data class NotificationItem(
    val title: String,
    val description: String,
    val date: String
)
class NotiHistoryAdapter(context: Context, private val notifications: List<NotificationItem>) :
    ArrayAdapter<NotificationItem>(context, 0, notifications) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false)
        }

        val currentNotification = getItem(position)

        listItemView?.findViewById<TextView>(R.id.textViewTitle)?.text = currentNotification?.title
        listItemView?.findViewById<TextView>(R.id.textViewDescription)?.text = currentNotification?.description
        listItemView?.findViewById<TextView>(R.id.textViewDate)?.text = currentNotification?.date

        return listItemView!!
    }
}