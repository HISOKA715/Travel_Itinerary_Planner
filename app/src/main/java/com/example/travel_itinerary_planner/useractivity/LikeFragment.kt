package com.example.travel_itinerary_planner.useractivity


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel_itinerary_planner.databinding.LayoutLikeBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.tourism_attraction.TourismActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LikeFragment : LoggedInFragment() {
    private var _binding: LayoutLikeBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchLikedAttractions()
    }

    private fun setupItemClickListener() {
        binding.likeGrid.setOnItemClickListener { adapterView, view, position, id ->

            val item = adapterView.getItemAtPosition(position) as ImageItem
            val intent = Intent(context, TourismActivity::class.java).apply {
                putExtra("documentId", item.attractionId)
            }
            startActivity(intent)
        }}

    private fun fetchLikedAttractions() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("Tourism Attractions").get().addOnSuccessListener { documents ->
            val likedAttractions = mutableListOf<ImageItem>()

            documents.forEach { document ->
                document.reference.collection("like").document(userId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("LikeFragment", "Listen failed.", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val imageUrl = document.getString("TourismImage") ?: ""
                            val imageItem = ImageItem(document.id, imageUrl)
                            if (!likedAttractions.contains(imageItem)) {
                                likedAttractions.add(imageItem)
                            }
                        } else {
                            likedAttractions.removeAll { it.attractionId == document.id }
                        }
                        updateUI(likedAttractions)
                    }
            }
        }
    }

    private fun updateUI(likedAttractions: List<ImageItem>) {
        val adapter = LikeAdapter(requireContext(), likedAttractions)
        binding.likeGrid.adapter = adapter
        setupItemClickListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}