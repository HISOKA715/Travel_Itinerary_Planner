package com.example.travel_itinerary_planner.notification
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.notification.NotiHistoryAdapter
import com.example.travel_itinerary_planner.notification.NotificationItem


class NotificationActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: NotiHistoryAdapter
    private lateinit var notifications: List<NotificationItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_history)

        listView = findViewById(R.id.notification_list)

        // Sample data for notifications
        notifications = listOf(
            NotificationItem("1 Utama Shopping Centre", "1 Utama is a shopping mall in Bandar Utama, Selangor...", "15/03/2024"),
        )
        adapter = NotiHistoryAdapter(this, notifications)
        listView.adapter = adapter
    }
}
