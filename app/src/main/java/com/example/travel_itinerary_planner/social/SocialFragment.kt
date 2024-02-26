package com.example.travel_itinerary_planner.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentSocialBinding
import com.example.travel_itinerary_planner.databinding.ItemAllPostBinding
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.example.travel_itinerary_planner.social.AllSocialPostAdapter
import com.example.travel_itinerary_planner.social.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date

class SocialFragment : Fragment(), AllSocialPostAdapter.OnBookmarkClickListener {

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var allSocialPostAdapter: AllSocialPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        allSocialPostAdapter = AllSocialPostAdapter(this)
        binding.recyclerViewAllPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAllPosts.adapter = allSocialPostAdapter

        fetchSocialPosts()
    }

    override fun onBookmarkClick(socialMediaPost: SocialMediaPost, binding: ItemAllPostBinding) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val postId = socialMediaPost.SocialID
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val bookmarksRef = firestore.collection("Bookmarks")

        if (currentUserUid != null && postId != null) {
            val bookmarkQuery = bookmarksRef
                .whereEqualTo("UserID", currentUserUid)
                .whereEqualTo("SocialID", postId)

            bookmarkQuery.get().addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    generateBookmarkId(bookmarksRef) { bookmarkId ->
                        val bookmarkData = hashMapOf(
                            "BookmarkID" to bookmarkId,
                            "UserID" to currentUserUid,
                            "SocialID" to postId,
                            "BookmarkTime" to dateFormat.format(Date())
                        )

                        bookmarksRef.document(bookmarkId)
                            .set(bookmarkData)
                            .addOnSuccessListener {
                                binding.bookmarkIcon.setColorFilter(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.blue
                                    )
                                )
                                Toast.makeText(context, "Bookmark added", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { exception ->
                                Toast.makeText(context, "Failed to add bookmark", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val bookmarkDocRef = querySnapshot.documents[0].reference
                    bookmarkDocRef.delete()
                        .addOnSuccessListener {
                            binding.bookmarkIcon.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.dark_blue
                                )
                            )
                            Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { exception ->
                            Toast.makeText(context, "Failed to remove bookmark", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Error checking bookmark", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateBookmarkId(bookmarksRef: CollectionReference, callback: (String) -> Unit) {
        bookmarksRef
            .orderBy("BookmarkID", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val lastBookmarkId = if (!querySnapshot.isEmpty) {
                    querySnapshot.documents[0].getString("BookmarkID")
                } else {
                    null
                }

                val newBookmarkId = if (lastBookmarkId != null) {
                    val lastId = lastBookmarkId.substring(1).toInt()
                    "B${String.format("%09d", lastId + 1)}"
                } else {
                    "B000000001"
                }
                callback(newBookmarkId)
            }
    }

    private fun fetchSocialPosts() {
        val postsRef = firestore.collection("SocialMedia")

        postsRef.whereEqualTo("SocialSharingOptions", "Public")
            .get()
            .addOnSuccessListener { documents ->
                val socialPostsList = mutableListOf<SocialMediaPost>()
                val userDataMap = mutableMapOf<String, UserData?>()

                for (document in documents) {
                    val socialPost = document.toObject(SocialMediaPost::class.java)
                    socialPostsList.add(0, socialPost)

                    fetchUserData(socialPost.UserID) { userData ->
                        userDataMap[socialPost.UserID] = userData
                        allSocialPostAdapter.submitList(socialPostsList, userDataMap)
                    }
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun fetchUserData(userId: String, callback: (UserData?) -> Unit) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(UserData::class.java)
                    callback(userData)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
