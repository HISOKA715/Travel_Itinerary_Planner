package com.example.travel_itinerary_planner.notification

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.travel_itinerary_planner.databinding.NotificationHistoryBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NotificationActivity : LoggedInActivity() {

    private lateinit var binding: NotificationHistoryBinding
    private var adapter: NotiHistoryAdapter? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NotificationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        binding.imageButton3.setOnClickListener {
            finish()
        }
        setupNotificationsListener()
    }
    private fun setupNotificationsListener() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .collection("notifications")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    val notificationItems = ArrayList<NotificationItem>()
                    for (document in snapshots!!) {
                        val id = document.id
                        val title = document.getString("title") ?: ""
                        val description = document.getString("body") ?: ""
                        val timestamp = document.getTimestamp("date")
                        val date: String = if (timestamp != null) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            sdf.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                            sdf.format(timestamp.toDate())
                        } else {
                            ""
                        }
                        notificationItems.add(NotificationItem(id, title, description, date))
                    }
                    if (adapter == null) {
                        adapter = NotiHistoryAdapter(this, notificationItems)
                        binding.notificationList.adapter = adapter
                        binding.notificationList.setOnItemClickListener { parent, view, position, id ->
                            val notificationItem = adapter!!.getItem(position)
                            val intent = Intent(this@NotificationActivity, Notification_View::class.java).apply {
                                putExtra("notificationId", notificationItem?.id)
                            }
                            startActivity(intent)
                        }
                        binding.notificationList.setOnItemLongClickListener { _, _, position, _ ->
                            val notificationItem = adapter?.getItem(position)
                            notificationItem?.let { item ->
                                AlertDialog.Builder(this@NotificationActivity)
                                    .setTitle("Delete Notification")
                                    .setMessage("Are you sure you want to delete the notification history?")
                                    .setPositiveButton("Yes") { _, _ ->

                                        deleteNotification(item.id)
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            }
                            true // Return true to indicate the long click was handled
                        }
                    } else {
                        adapter!!.clear()
                        adapter!!.addAll(notificationItems)
                        adapter!!.notifyDataSetChanged()
                    }
                }
        }
    }
    private fun deleteNotification(notificationId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .collection("notifications").document(notificationId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Notification deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error deleting notification", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
