package com.example.travel_itinerary_planner.useractivity


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel_itinerary_planner.databinding.LayoutFeedbackBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class FeedbackFragment: LoggedInFragment() {
    private var _binding: LayoutFeedbackBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FeedbackAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = LayoutFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchFeedbackData()
    }
    private fun fetchFeedbackData() {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val feedbackList = mutableListOf<FeedbackItem>()

        firestore.collection("Feedback")
            .orderBy("FeedbackDate", Query.Direction.DESCENDING)
            .whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener { documents ->
                val feedbackCount = documents.size()
                var processedCount = 0

                if (feedbackCount == 0) {

                    updateFeedbackList(feedbackList)
                }

                for (document in documents) {
                    val feedbackId = document.id
                    document.reference.collection("reply").orderBy("replyDate", Query.Direction.DESCENDING).limit(1).get()
                        .addOnSuccessListener { replies ->
                            processedCount++
                            val date: String
                            val readStatus: Boolean = document.getString("read")?.toBoolean() ?: false
                            if (!replies.isEmpty) {
                                val reply = replies.documents.first()
                                date = reply.getDate("replyDate")?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "No Date"
                            } else {
                                date = document.getDate("FeedbackDate")?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "No Date"
                            }
                            val title = document.getString("FeedbackCategory") ?: "No Title"
                            val content = document.getString("FeedbackDesc") ?: "No Content"

                            feedbackList.add(FeedbackItem(feedbackId, title, content, readStatus, date))

                            if (processedCount == feedbackCount) {
                                updateFeedbackList(feedbackList)
                            }
                        }
                        .addOnFailureListener { exception ->
                            processedCount++
                            Log.w("FeedbackFragment", "Error getting replies: ", exception)
                            if (processedCount == feedbackCount) {
                                updateFeedbackList(feedbackList)
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FeedbackFragment", "Error getting feedback documents: ", exception)
            }
    }

    private fun updateFeedbackList(feedbackItems: List<FeedbackItem>) {
        val sortedItems = feedbackItems.sortedByDescending {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.date)
        }

        adapter = FeedbackAdapter(requireContext(), sortedItems)
        binding.listView.adapter = adapter

        setupItemClickListener()
    }

    private fun setupItemClickListener() {
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val clickedFeedbackId = adapter.getItem(position)?.id
            val intent = Intent(requireContext(), FeedDetailsActivity::class.java).apply {
                putExtra("FEEDBACK_ID", clickedFeedbackId)
            }
            startActivity(intent)
        }
    }
}
