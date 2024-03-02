package com.example.travel_itinerary_planner.social

import SocialCommentBottomSheetFragment
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.travel_itinerary_planner.R
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.databinding.ActivityBookmarkDetailsBinding
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookmarkDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookmarkDetailsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val postId = intent.getStringExtra("postId")
        if (postId != null) {
            fetchPostDetails(postId)
        }
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val bookmarksRef = firestore.collection("Bookmarks")
            val bookmarkQuery = bookmarksRef
                .whereEqualTo("UserID", currentUserUid)
                .whereEqualTo("SocialID", postId)

            bookmarkQuery.get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    binding.bookmarkBookmarks.setColorFilter(
                        ContextCompat.getColor(applicationContext, R.color.blue)
                    )
                } else {
                    binding.bookmarkBookmarks.setColorFilter(
                        ContextCompat.getColor(applicationContext, R.color.dark_blue)
                    )
                }
            }
        }


        binding.toolbarBookmarkDetails.setNavigationOnClickListener {
            val intent = Intent(this, BookmarksActivity::class.java)
            startActivity(intent)
        }

        binding.moreBookmarks.setOnClickListener {
            val postId = intent.getStringExtra("postId")
            postId?.let { postId ->
                fetchPostDetailsAndShowOptions(postId)
            }
        }
        binding.userNameViewBookmarks.setOnClickListener{
            val postId = intent.getStringExtra("postId")
            postId?.let { postId ->
                val currentUser = auth.currentUser?.uid
                firestore.collection("SocialMedia").document(postId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        val socialMediaPost = documentSnapshot.toObject(SocialMediaPost::class.java)
                        socialMediaPost?.let {
                            if (it.UserID == currentUser) {
                                val intent = Intent(this, BottomNavigationActivity::class.java)
                                intent.putExtra("navigateToProfileFragment", true)
                                startActivity(intent)
                                finish()
                            } else {
                                socialMediaPost?.let {
                                    val postUserId = it.UserID
                                    val intent = Intent(this, BottomNavigationActivity::class.java).apply {
                                        putExtra("userId", postUserId )
                                        putExtra("navigateToOthersProfileFragment", true)
                                    }

                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }

                    }
                    .addOnFailureListener { exception ->
                    }
            }
        }
        binding.profileImageBookmarks.setOnClickListener {
            val postId = intent.getStringExtra("postId")
            postId?.let { postId ->
                val currentUser = auth.currentUser?.uid
                firestore.collection("SocialMedia").document(postId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        val socialMediaPost = documentSnapshot.toObject(SocialMediaPost::class.java)
                        socialMediaPost?.let {
                            if (it.UserID == currentUser) {
                                val intent = Intent(this, BottomNavigationActivity::class.java)
                                intent.putExtra("navigateToProfileFragment", true)
                                startActivity(intent)
                                finish()
                            } else {
                                socialMediaPost?.let {
                                    val postUserId = it.UserID
                                    val intent = Intent(this, BottomNavigationActivity::class.java).apply {
                                        putExtra("userId", postUserId )
                                        putExtra("navigateToOthersProfileFragment", true)
                                    }

                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }

                    }
                    .addOnFailureListener { exception ->
                    }
            }
        }
        binding.bookmarkBookmarks.setOnClickListener {
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
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

                                findViewById<ImageView>(R.id.bookmark).setColorFilter(
                                    ContextCompat.getColor(
                                        applicationContext,
                                        R.color.blue
                                    )
                                )
                                Toast.makeText(applicationContext, "Bookmark added", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { exception ->
                                Toast.makeText(applicationContext, "Failed to add bookmark", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        val bookmarkDocRef = querySnapshot.documents[0].reference
                        bookmarkDocRef.delete()
                            .addOnSuccessListener {
                                findViewById<ImageView>(R.id.bookmark).setColorFilter(
                                    ContextCompat.getColor(
                                        applicationContext,
                                        R.color.dark_blue
                                    )
                                )
                                Toast.makeText(applicationContext, "Bookmark removed", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { exception ->
                                Toast.makeText(applicationContext, "Failed to remove bookmark", Toast.LENGTH_SHORT).show()
                            }
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(applicationContext, "Error checking bookmark", Toast.LENGTH_SHORT).show()
                }
            }

        }
        binding.commentsBookmarks.setOnClickListener {
            val postId = intent.getStringExtra("postId")
            val bottomSheetFragment = SocialCommentBottomSheetFragment()
            val bundle = Bundle().apply {
                putString("postId", postId)
            }
            bottomSheetFragment.arguments = bundle
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }
        val userSocialRef = firestore.collection("UserSocial")
        val query = userSocialRef.whereEqualTo("SocialID", postId )

        query.get().addOnSuccessListener { querySnapshot ->
            postId?.let { postId ->
                val commentCount = querySnapshot.size().toString()
                val socialMediaRef = firestore.collection("SocialMedia").document(postId)

                socialMediaRef.update("SocialCommentCounts", commentCount)
                    .addOnSuccessListener {
                        binding.commentsCountBookmarks.text = "$commentCount Comments"
                    }
                    .addOnFailureListener { e ->

                    }
            }
        }.addOnFailureListener { e ->

        }



    }

    private fun fetchPostDetails(postId: String) {
        firestore.collection("SocialMedia").document(postId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val socialMediaPost = documentSnapshot.toObject(SocialMediaPost::class.java)
                socialMediaPost?.let { bindDataToViews(it) }
            }
            .addOnFailureListener { exception ->
            }
        firestore.collection("Bookmarks")
            .whereEqualTo("SocialID", postId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val bookmarkDocument = querySnapshot.documents[0]
                    val timestamp = bookmarkDocument.getString("BookmarkTime")

                    binding.timeBookmarks.text = timestamp
                }

            }
            .addOnFailureListener { exception ->
            }
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

    private fun bindDataToViews(socialMediaPost: SocialMediaPost) {

        firestore.collection("users").document(socialMediaPost.UserID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val username = documentSnapshot.getString("Username")
                binding.userNameViewBookmarks.text = username

                val profileImage = documentSnapshot.getString("ProfileImage")
                Glide.with(this)
                    .load(profileImage)
                    .override(300, 300)
                    .error(R.drawable.travel_main)
                    .centerCrop()
                    .into(binding.profileImageBookmarks)

            }
            .addOnFailureListener { exception ->
            }
        binding.locationBookmarks.text = socialMediaPost.SocialLocation
        binding.postContentBookmarks.text = socialMediaPost.SocialContent
        binding.commentsCountBookmarks.text = socialMediaPost.SocialCommentCounts
        binding.postDateBookmarks.text = socialMediaPost.SocialDate




        Glide.with(this)
            .load(socialMediaPost.SocialImage)
            .override(300, 300)
            .error(R.drawable.travel_main)
            .centerCrop()
            .into(binding.postImageBookmarks)
    }
    private fun showMoreReportOptionsDialog(socialMediaPost: SocialMediaPost) {
        AlertDialog.Builder(this)
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
        AlertDialog.Builder(this)
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
        AlertDialog.Builder(this)
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
                    Toast.makeText(this , "Post reported for '$reason'", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to report post", Toast.LENGTH_SHORT).show()
                }

    }



    private fun showMoreOptionsDialog(socialMediaPost: SocialMediaPost) {
        AlertDialog.Builder(this)
            .setTitle("More Options")
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, EditPostActivity::class.java).apply {
                            putExtra("postId", socialMediaPost.SocialID)
                            putExtra("cameFromBookmarkDetails", true)
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
        AlertDialog.Builder(this)
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
                Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show()

                val postId = intent.getStringExtra("postId")
                postId?.let { postId ->
                    fetchPostDetails(postId)
                }
            }
            .addOnFailureListener { exception ->
            }
    }



}
