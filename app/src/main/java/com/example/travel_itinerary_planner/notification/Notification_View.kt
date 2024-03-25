package com.example.travel_itinerary_planner.notification

import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.NotificationHistoryViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
class Notification_View : LoggedInActivity() {

    private lateinit var binding: NotificationHistoryViewBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NotificationHistoryViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val notificationId = intent.getStringExtra("notificationId")

        if (notificationId != null) {
            displayNotificationDetails(notificationId)
        } else {
            Toast.makeText(this, "No Notification History Record", Toast.LENGTH_SHORT).show()
        }
        binding.imageButton3.setOnClickListener {
            finish()
        }
    }

    private fun displayNotificationDetails(notificationId: String) {
        val userId = auth.currentUser?.uid
        firestore = FirebaseFirestore.getInstance()
        if (userId != null) {
            firestore.collection("users").document(userId).collection("notifications").document(notificationId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Extract the notification details
                        val title = document.getString("title") ?: "No Title"
                        val body = document.getString("body") ?: "No Body"
                        val timestamp = document.getTimestamp("date")
                        val image = document.getString("imageUrl") ?: ""
                        val date = timestamp?.toDate()?.let {
                            val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                            formatter.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                            formatter.format(it)
                        } ?: "No Date"
                        binding.title.text = title
                        binding.body.text = body
                        binding.date.text = date
                        binding.imageButton3.setOnClickListener {
                            finish()
                        }
                        if (image != null && image.isNotEmpty()) {
                            Glide.with(this)
                                .load(image)
                                .into(binding.imageView2)
                        } else {
                            Glide.with(this)
                                .load(R.drawable.email_support)
                                .into(binding.imageView2)
                        }
                    } else {
                        Toast.makeText(baseContext, "No detail found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(baseContext, "Error getting details: $exception", Toast.LENGTH_SHORT).show()
                }
        }else{
            Toast.makeText(baseContext, "Cannot find the user", Toast.LENGTH_SHORT).show()
        }
    }
}