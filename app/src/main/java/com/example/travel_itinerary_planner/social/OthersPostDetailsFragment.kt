package com.example.travel_itinerary_planner.social

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentOthersPostDetailsBinding
import com.example.travel_itinerary_planner.databinding.ItemAllPostBinding
import com.example.travel_itinerary_planner.databinding.ItemOthersPostDetailsBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date

class OthersPostDetailsFragment : LoggedInFragment(), OthersPostAdapter.OnMoreOptionsClickListener, OthersPostAdapter.OnBookmarkClickListener  {
    private var _binding: FragmentOthersPostDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var othersPostAdapter: OthersPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOthersPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)


        val menu = bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            if (menuItem.itemId == R.id.navigation_social) {
                menuItem.isChecked = true
                break
            }
        }

        binding.toolbarOthersPostDetails.setNavigationOnClickListener {
            val userId = requireActivity().intent.getStringExtra("userId")

            val intent = Intent(requireContext(), BottomNavigationActivity::class.java).apply {
                putExtra("returnToOthersProfileFragment", true)
                putExtra("userId", userId)
            }
            startActivity(intent)
        }




        othersPostAdapter = OthersPostAdapter(this,this)
        binding.recyclerViewOthersPostDetails.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewOthersPostDetails.adapter = othersPostAdapter
        fetchSocialPosts()



    }
    override fun onBookmarkClick(socialMediaPost: SocialMediaPost, binding: ItemOthersPostDetailsBinding) {
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
                                binding.bookmark.setColorFilter(
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
                            binding.bookmark.setColorFilter(
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
    override fun onMoreOptionsClick(socialMediaPost: SocialMediaPost) {

        showMoreOptionsDialog(socialMediaPost)
    }

    private fun showMoreOptionsDialog(socialMediaPost: SocialMediaPost) {

        AlertDialog.Builder(requireContext())
            .setTitle("More Options")
            .setItems(arrayOf("Report")) { _, which ->
                when (which) {
                    0 -> {

                    }

                }
            }
            .show()
    }



    override fun onResume() {
        super.onResume()
        fetchSocialPosts()
    }

    private fun fetchSocialPosts() {
        val userId = requireActivity().intent.getStringExtra("userId")
        val position = requireActivity().intent.getIntExtra("position", RecyclerView.NO_POSITION)


        if (userId != null) {

            val postsRef = firestore.collection("SocialMedia")
                .whereEqualTo("UserID", userId)

            postsRef.get()
                .addOnSuccessListener { documents ->
                    val socialPostsList = mutableListOf<SocialMediaPost>()
                    val userDataMap = mutableMapOf<String, UserData?>()

                    for (document in documents) {
                        val socialPost = document.toObject(SocialMediaPost::class.java)
                        socialPostsList.add(0, socialPost)

                        fetchUserData(socialPost.UserID) { userData ->
                            userDataMap[socialPost.UserID] = userData
                            othersPostAdapter.submitList(socialPostsList, userDataMap)


                                if (position != RecyclerView.NO_POSITION) {

                                    binding.recyclerViewOthersPostDetails.post {
                                        (binding.recyclerViewOthersPostDetails.layoutManager as LinearLayoutManager).scrollToPosition(position)
                                    }
                                }

                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to fetch social posts: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
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
}
