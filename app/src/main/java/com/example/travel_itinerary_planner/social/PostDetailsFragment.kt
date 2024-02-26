package com.example.travel_itinerary_planner.social

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentPostDetailsBinding
import com.example.travel_itinerary_planner.databinding.ItemSocialMediaBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.profile.ProfileViewModel
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date

class PostDetailsFragment : LoggedInFragment(), SocialPostAdapter.OnMoreOptionsClickListener , SocialPostAdapter.OnBookmarkClickListener {
    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var socialPostAdapter: SocialPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
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
            if (menuItem.itemId == R.id.navigation_profile) {
                menuItem.isChecked = true
                break
            }
        }
        binding.toolbarPostDetails.setNavigationOnClickListener {
            val intent = Intent(requireContext(), BottomNavigationActivity::class.java)
            intent.putExtra("returnToProfileFragment", true)
            startActivity(intent)
            requireActivity().finish()
        }


        socialPostAdapter  = SocialPostAdapter(this, this)
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPosts.adapter = socialPostAdapter

        fetchSocialPosts()



    }
    override fun onBookmarkClick(socialMediaPost: SocialMediaPost, binding: ItemSocialMediaBinding) {
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
                                binding.bookmarkImageView.setColorFilter(
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
                            binding.bookmarkImageView.setColorFilter(
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
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(context, EditPostActivity::class.java).apply {
                            putExtra("postId", socialMediaPost.SocialID)
                        }
                        startActivity(intent)


                    }
                    1 -> {
                        showDeleteConfirmationDialog(socialMediaPost)
                    }
                }
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(socialMediaPost: SocialMediaPost) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                deletePost(socialMediaPost.SocialID)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(socialId:String) {
        val postsRef = firestore.collection("SocialMedia")
        postsRef.document(socialId)
            .delete()
            .addOnSuccessListener {
                fetchSocialPosts()
                Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchSocialPosts() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            val postsRef = firestore.collection("SocialMedia")
                .whereEqualTo("UserID", currentUserUid)

            postsRef.get()
                .addOnSuccessListener { documents ->
                    val socialPostsList = mutableListOf<SocialMediaPost>()
                    val userDataMap = mutableMapOf<String, UserData?>()

                    for (document in documents) {
                        val socialPost = document.toObject(SocialMediaPost::class.java)
                        socialPostsList.add(0,socialPost)

                        fetchUserData(socialPost.UserID) { userData ->
                            userDataMap[socialPost.UserID] = userData
                            socialPostAdapter.submitList(socialPostsList, userDataMap)
                            arguments?.let { bundle ->
                                val position = bundle.getInt("position", RecyclerView.NO_POSITION)
                                if (position != RecyclerView.NO_POSITION) {
                                    binding.recyclerViewPosts.post {
                                        (binding.recyclerViewPosts.layoutManager as LinearLayoutManager).scrollToPosition(position)
                                    }
                                }
                            }
                        }
                    }


                }
                .addOnFailureListener { exception ->
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




