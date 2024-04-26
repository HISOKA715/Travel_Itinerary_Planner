package com.example.travel_itinerary_planner.useractivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.FeedbackDetailsBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FeedbackDetail(
    val accuracy: Int,
    val category: String,
    val date: Date,
    val description: String,
    val location: String
)
class FeedDetailsActivity : LoggedInActivity() {
    private lateinit var binding: FeedbackDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FeedbackDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val feedbackId = intent.getStringExtra("FEEDBACK_ID")
        if (feedbackId != null) {
            fetchFeedbackDetails(feedbackId)
        }

        binding.imageButton13.setOnClickListener {
            val intent = Intent(this, UserListActivity::class.java).apply {
                intent.putExtra("SELECTED_TAB", "Feedback")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish() }
    }

    private fun fetchFeedbackDetails(feedbackId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val feedbackDocRef = firestore.collection("Feedback").document(feedbackId)
        firestore.collection("Feedback").document(feedbackId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val accuracy = documentSnapshot.getLong("FeedbackAccuracy")?.toInt() ?: 0
                    val category = documentSnapshot.getString("FeedbackCategory") ?: "-"
                    val date = documentSnapshot.getDate("FeedbackDate") ?: Date()
                    val description = documentSnapshot.getString("FeedbackDesc") ?: "-"
                    val location = documentSnapshot.getString("FeedbackLocation") ?: "-"

                    updateUI(FeedbackDetail(accuracy, category, date, description, location))
                    fetchReplies(feedbackId)

                    updateReadStatus(feedbackDocRef)
                } else {
                    Log.d("FeedDetailsActivity", "No feedback found with ID: $feedbackId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FeedDetailsActivity", "Error fetching feedback details", exception)
            }
    }
    private fun updateReadStatus(feedbackDocRef: DocumentReference) {
        feedbackDocRef
            .update("read", "true")
            .addOnSuccessListener {
                Log.d("FeedDetailsActivity", "Read status updated to true")
            }
            .addOnFailureListener { e ->
                Log.e("FeedDetailsActivity", "Error updating read status", e)
            }
    }
    private fun fetchReplies(feedbackId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Feedback").document(feedbackId)
            .collection("reply")
            .orderBy("replyDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (document in queryDocumentSnapshots) {
                    val replyDesc = document.getString("replyDesc") ?: "No reply description"
                    val replyDate = document.getDate("replyDate") ?: Date()
                    updateUIWithReply(replyDate, replyDesc)

                }
            }
            .addOnFailureListener { exception ->
                Log.e("FeedDetailsActivity", "Error fetching replies: ", exception)
            }
    }
    private fun updateUIWithReply(replyDate: Date, replyDesc: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.reply.text = replyDesc
        binding.replydate.text = dateFormat.format(replyDate)
    }
    private fun updateUI(feedbackDetail: FeedbackDetail) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        binding.category.text = feedbackDetail.category
        binding.date.text = dateFormat.format(feedbackDetail.date)
        binding.location.text = feedbackDetail.location
        if (feedbackDetail.category == "Recommendation"){
            binding.accurary.text = if (feedbackDetail.accuracy == 1) "Accurate" else "Not Accurate"
        }else{
            binding.accurary.text = "-"
        }

        binding.describe.text = feedbackDetail.description
    }
}