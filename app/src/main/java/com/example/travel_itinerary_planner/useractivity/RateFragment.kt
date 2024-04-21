package com.example.travel_itinerary_planner.useractivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.travel_itinerary_planner.databinding.LayoutRateBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.tourism_attraction.ReviewDetailAdapter
import com.example.travel_itinerary_planner.tourism_attraction.ReviewItem1
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Locale

class RateFragment : LoggedInFragment() {
    private var _binding: LayoutRateBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UserRateAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = LayoutRateBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        adapter = UserRateAdapter(requireContext(), mutableListOf())
        binding.listrate.adapter = adapter

        if (auth.currentUser != null) {
            fetchAttractionsAndReviews(auth.currentUser!!.uid)
        }
    }
    private fun setupListViewClickListener() {
        binding.listrate.setOnItemClickListener { _, _, position, _ ->
            adapter.getItem(position)?.let { clickedReview ->
                val intent = Intent(context, EditReviewActivity::class.java).apply {
                    putExtra("REVIEW_ID", clickedReview.id)
                    putExtra("documentId", clickedReview.attractionId)
                }
                startActivity(intent)
            }
        }
    }
    private fun fetchAttractionsAndReviews(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").get().addOnSuccessListener { attractionsSnapshot ->
            val attractionsWithReviewsTasks = attractionsSnapshot.documents.map { attractionDoc ->
                val attractionId = attractionDoc.id
                val tourismImage = attractionDoc.getString("TourismImage") ?: ""
                db.collection("Tourism Attractions").document(attractionId).collection("Review")
                    .whereEqualTo("UserID", userId)
                    .orderBy("RateDate", Query.Direction.DESCENDING)
                    .get().continueWithTask { reviewTask ->
                        val reviewsQuerySnapshot = reviewTask.result
                        val reviews = reviewsQuerySnapshot?.documents?.map { reviewDoc ->
                            val rateId = reviewDoc.id
                            val rateDesc = reviewDoc.getString("RateDesc") ?: ""
                            val rateNo = reviewDoc.getString("RateNo") ?: "0"
                            val rateDate = reviewDoc.getDate("RateDate")?.let { date ->
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                            } ?: ""
                            UserReview(rateId, attractionId, "Anonymous", "", rateNo, rateDate, rateDesc, tourismImage)
                        } ?: listOf()
                        Tasks.forResult(reviews)
                    }
            }
            Tasks.whenAllSuccess<List<UserReview>>(attractionsWithReviewsTasks).addOnSuccessListener { listOfReviewsLists ->
                val allReviews = listOfReviewsLists.flatten()
                fetchUserDetailsAndUpdateUI(allReviews)
            }.addOnFailureListener { e ->
                Log.e("RateFragment", "Error fetching reviews: ", e)
            }
        }
    }


    private fun fetchUserDetailsAndUpdateUI(reviews: List<UserReview>) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener { userDoc ->
            val username = userDoc.getString("Name") ?: "Anonymous"
            val profileImage = userDoc.getString("ProfileImage") ?: ""
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val updatedReviews = reviews.onEach { review ->
                review.username = username
                review.profileImage = profileImage
            }.sortedByDescending { review ->
                dateFormat.parse(review.rateDate)
            }

            updateReviewsUI(updatedReviews)
        }.addOnFailureListener { e ->
            Log.e("RateFragment", "Error fetching user details: ", e)
        }
    }
    private fun updateReviewsUI(reviews: List<UserReview>) {
        adapter.clear()
        adapter.addAll(reviews)
        adapter.notifyDataSetChanged()
        setupListViewClickListener()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}