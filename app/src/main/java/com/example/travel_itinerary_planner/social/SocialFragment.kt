package com.example.travel_itinerary_planner.social

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SocialFragment : Fragment(), AllSocialPostAdapter.OnMoreOptionsClick, AllSocialPostAdapter.OnBookmarkClickListener {

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

        allSocialPostAdapter = AllSocialPostAdapter(this,this, childFragmentManager)
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

                        val bookmarkData = hashMapOf(
                            "UserID" to currentUserUid,
                            "SocialID" to postId,
                            "BookmarkTime" to dateFormat.format(Date())
                        )

                        bookmarksRef
                            .add(bookmarkData)
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
    override fun onMoreOptionsClick(socialMediaPost: SocialMediaPost) {
        fetchPostDetailsAndShowOptions(socialMediaPost.SocialID)
    }


    private fun fetchPostDetailsAndShowOptions(postId: String) {
        val currentUser = auth.currentUser?.uid
        firestore.collection("SocialMedia").document(postId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val socialMediaPost = documentSnapshot.toObject(SocialMediaPost::class.java)
                socialMediaPost?.let {
                    if (it.UserID == currentUser) {

                        showMoreOptionsDialog(it)
                    } else {
                        showMoreReportOptionsDialog(it)
                    }
                }

            }
            .addOnFailureListener { exception ->
            }
    }
    private fun showMoreReportOptionsDialog(socialMediaPost: SocialMediaPost) {
        AlertDialog.Builder(requireContext())
            .setTitle("More Options")
            .setItems(arrayOf("Report")) { _, which ->
                when (which) {
                    0 -> {
                        showReportOptionsDialog(socialMediaPost)

                    }

                }
            }
            .show()
    }
    private fun showReportOptionsDialog(socialMediaPost: SocialMediaPost) {
        val reasons = arrayOf("Inappropriate content", "Spam", "Offensive language", "Other")
        AlertDialog.Builder(requireContext())
            .setTitle("Report Post")
            .setSingleChoiceItems(reasons, -1) { dialog, which ->
                val selectedReason = reasons[which]
                showReportConfirmationDialog(selectedReason, socialMediaPost)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showReportConfirmationDialog(reason: String, socialMediaPost: SocialMediaPost) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Report")
            .setMessage("Are you sure you want to report this post for '$reason'?")
            .setPositiveButton("Report") { _, _ ->
                reportPost(socialMediaPost, reason)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reportPost(socialMediaPost: SocialMediaPost, reason: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateAndTime = Calendar.getInstance().time
        val formattedDate = dateFormat.format(currentDateAndTime).toString()


            val reportData = hashMapOf(
                "SocialID" to socialMediaPost.SocialID,
                "UserID" to currentUserID,
                "ReportTime" to formattedDate,
                "Reason" to reason
            )

            val reportsRef = firestore.collection("Reports")
            reportsRef.add(reportData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Post reported for '$reason'", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Failed to report post", Toast.LENGTH_SHORT).show()
                }

    }




    private fun showMoreOptionsDialog(socialMediaPost: SocialMediaPost) {
        AlertDialog.Builder(requireContext())
            .setTitle("More Options")
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(requireContext(), EditPostActivity::class.java).apply {
                            putExtra("postId", socialMediaPost.SocialID)
                            putExtra("cameFromSocialFragment", true)
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

    private fun deletePost(socialId: String) {
        val postsRef = firestore.collection("SocialMedia")
        postsRef.document(socialId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
                fetchSocialPosts()

            }
            .addOnFailureListener { exception ->
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
    override fun onResume() {
        super.onResume()
        fetchSocialPosts()
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
