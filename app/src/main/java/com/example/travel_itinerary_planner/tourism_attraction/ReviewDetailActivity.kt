package com.example.travel_itinerary_planner.tourism_attraction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.DetailReviewBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewDetailActivity : LoggedInActivity() {
    private lateinit var binding: DetailReviewBinding
    private lateinit var adapter: ReviewDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.detail_review)
        binding.button.background = ContextCompat.getDrawable(this, R.drawable.rounded_button)
        val documentId = intent.getStringExtra("documentId")

        binding.button.setOnClickListener {
            val intent = Intent(this, AddReviewActivity::class.java).apply {
                putExtra("documentId", documentId)
            }
            startActivity(intent)
        }
        binding.imageButton8.setOnClickListener {
            finish()
        }

        fetchReviewsWithUserDetails(documentId!!)
        fetchAndCalculateReviews(documentId)
        fetchTourismAttraction(documentId)
    }

    private fun fetchTourismAttraction(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").document(documentId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("TourismName")
                    val imageUrl = document.getString("TourismImage")
                    binding.textView24.text = name
                    binding.textView25.text = document.getString("TourismState")
                    Glide.with(this).load(imageUrl).into(binding.imageButton11)
                } else {
                }
            }
            .addOnFailureListener { e -> }
    }

    private fun fetchAndCalculateReviews(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").document(documentId).collection("Review")
            .get()
            .addOnSuccessListener { result ->
                val starCounts = IntArray(5)
                var reviewCount = 0

                for (document in result) {
                    val ratingString = document.getString("RateNo")
                    val rating = ratingString?.toDoubleOrNull()?.let { Math.round(it).toInt() }
                    if (rating != null && rating in 1..5) {
                        starCounts[rating - 1]++
                        reviewCount++
                    }
                }

                if (reviewCount > 0) {
                    val percentages = starCounts.map { it * 100 / reviewCount.toDouble() }
                    updateUIWithRatingAndPercentages(percentages, reviewCount)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TourismActivity", "Error fetching reviews", e)
            }
    }

    private fun updateUIWithRatingAndPercentages(percentages: List<Double>, reviewCount: Int) {
        runOnUiThread {
            val totalRating = percentages.indices.sumOf { (it + 1) * percentages[it] }
            val averageRating = totalRating / 100.0
            val formattedAverageRating = String.format("%.1f", averageRating)
            binding.textView26.text = formattedAverageRating
        }
    }



    private fun fetchReviewsWithUserDetails(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").document(documentId).collection("Review")
            .orderBy("RateDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { reviewsSnapshot ->
                val reviews = mutableListOf<ReviewItem1>()
                val userDetailsFetches = mutableListOf<Task<DocumentSnapshot>>()

                for (reviewDoc in reviewsSnapshot) {
                    val userId = reviewDoc.getString("UserID") ?: continue
                    val rateDesc = reviewDoc.getString("RateDesc") ?: ""
                    val rateNo = reviewDoc.getString("RateNo")?: ""
                    val rateImg =  reviewDoc.getString("RateUrl")?: ""
                    val rateDate = reviewDoc.getTimestamp("RateDate")?.toDate()?.let { date ->
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                    } ?: ""
                    val userDetailFetch = db.collection("users").document(userId).get()
                    userDetailsFetches.add(userDetailFetch)

                    userDetailFetch.addOnSuccessListener { userDoc ->
                        val username = userDoc.getString("Name") ?: "Anonymous"
                        val profileImage = userDoc.getString("ProfileImage") ?: ""
                        synchronized(reviews) {
                            reviews.add(ReviewItem1(username, profileImage, rateDate,rateDesc,rateNo,rateImg ))
                        }
                    }
                }
                Tasks.whenAllComplete(userDetailsFetches).addOnCompleteListener {
                    Log.d("TourismActivity", "Total reviews fetched: ${reviews.size}")
                    updateReviewsUI(reviews)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TourismActivity", "Error fetching reviews with user details", e)
            }
    }
    private fun updateReviewsUI(reviews: List<ReviewItem1>) {
        adapter = ReviewDetailAdapter(this, reviews)
        binding.itemReviewDetail.adapter = adapter
    }
}