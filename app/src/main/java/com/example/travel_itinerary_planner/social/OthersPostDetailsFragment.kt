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
import java.util.Calendar
import java.util.Date
import java.util.Locale

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




        othersPostAdapter = OthersPostAdapter(this,this,childFragmentManager)
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

                        val bookmarkData = hashMapOf(
                            "UserID" to currentUserUid,
                            "SocialID" to postId,
                            "BookmarkTime" to dateFormat.format(Date())
                        )

                        bookmarksRef
                            .add(bookmarkData)
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


    override fun onMoreOptionsClick(socialMediaPost: SocialMediaPost) {

        showMoreReportOptionsDialog(socialMediaPost)
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
