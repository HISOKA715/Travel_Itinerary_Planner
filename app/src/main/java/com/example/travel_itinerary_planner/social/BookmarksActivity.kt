package com.example.travel_itinerary_planner.social

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.databinding.ActivityBookmarksBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookmarksActivity : LoggedInActivity(), BookmarksAdapter.OnItemClickListener {
    private lateinit var binding: ActivityBookmarksBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var bookmarksAdapter: BookmarksAdapter
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.toolbarBookmarks.setNavigationOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToDrawerFragment", true)
            startActivity(intent)
        }

        firestore = FirebaseFirestore.getInstance()
        bookmarksAdapter = BookmarksAdapter(this)
        binding.recyclerViewBookmarks.apply {
            val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            this.layoutManager = layoutManager
            adapter = bookmarksAdapter
        }

        fetchBookmarks()
    }

    private fun fetchBookmarks() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        userId?.let { uid ->
            firestore.collection("Bookmarks")
                .whereEqualTo("UserID", uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val bookmarkList = mutableListOf<SocialMediaPost>()
                    for (document in querySnapshot.documents) {
                        val post = document.toObject(SocialMediaPost::class.java)
                        post?.let { bookmarkList.add(it) }
                    }
                    bookmarksAdapter.submitList(bookmarkList)

                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch bookmarks: ${exception.message}", Toast.LENGTH_SHORT).show()

                }
        } ?: run {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClick(socialMediaPost: SocialMediaPost) {

            val intent = Intent(this, BookmarkDetailsActivity::class.java)
            intent.putExtra("postId", socialMediaPost.SocialID)
            startActivity(intent)

    }

}

