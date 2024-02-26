package com.example.travel_itinerary_planner.social

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ItemAllPostBinding
import com.example.travel_itinerary_planner.databinding.ItemSocialMediaBinding
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.example.travel_itinerary_planner.social.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AllSocialPostAdapter(private val bookmarkListener: OnBookmarkClickListener) : RecyclerView.Adapter<AllSocialPostAdapter.AllSocialPostViewHolder>() {

    private var socialPosts: List<SocialMediaPost> = ArrayList()
    private var userDataMap: Map<String, UserData?> = HashMap()
    private var isExpandedMap: MutableMap<Int, Boolean> = HashMap()
    private lateinit var firestore: FirebaseFirestore
    inner class AllSocialPostViewHolder(private val binding: ItemAllPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(socialPost: SocialMediaPost, userData: UserData?) {
            firestore = FirebaseFirestore.getInstance()
            binding.postContent.text = socialPost.SocialContent
            binding.commentsCount.text = socialPost.SocialCommentCounts
            binding.postDate.text = socialPost.SocialDate
            binding.textViewLocation.text = socialPost.SocialLocation

            binding.userName.setOnClickListener { view ->
                val position = adapterPosition
                val socialPost = socialPosts[position]
                val userId = socialPost.UserID

                if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    val intent = Intent(view.context, BottomNavigationActivity::class.java)
                    intent.putExtra("navigateToProfileFragment", true)
                    view.context.startActivity(intent)
                } else {
                    val bundle = Bundle().apply {
                        putString("userId", userId)
                    }
                    view.findNavController().navigate(R.id.navigation_others_profile, bundle)
                }

            }





            binding.userProfile.setOnClickListener { view ->
                val position = adapterPosition
                val socialPost = socialPosts[position]
                val userId = socialPost.UserID

                if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    val intent = Intent(view.context, BottomNavigationActivity::class.java)
                    intent.putExtra("navigateToProfileFragment", true)
                    view.context.startActivity(intent)
                } else {
                    val bundle = Bundle().apply {
                        putString("userId", userId)
                    }
                    view.findNavController().navigate(R.id.navigation_others_profile, bundle)
                }
            }



            binding.postContent.setOnClickListener {
                toggleExpandCollapse(binding.postContent)
            }

            if (isExpandedMap[adapterPosition] == true) {
                expandTextView(binding.postContent)
            } else {
                collapseTextView(binding.postContent)
            }
            Glide.with(binding.root)
                .load(socialPost.SocialImage)
                .error(R.drawable.travel_main)
                .override(300, 300)
                .centerCrop()
                .into(binding.postImage)

            userData?.let {
                Glide.with(binding.root)
                    .load(it.ProfileImage)
                    .error(R.drawable.travel_main)
                    .override(300, 300)
                    .circleCrop()
                    .into(binding.userProfile)

                binding.userName.text = it.Username
            }
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserUid != null) {
                val bookmarksRef = firestore.collection("Bookmarks")
                val bookmarkQuery = bookmarksRef
                    .whereEqualTo("UserID", currentUserUid)
                    .whereEqualTo("SocialID", socialPost.SocialID)

                bookmarkQuery.get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        binding.bookmarkIcon.setColorFilter(
                            ContextCompat.getColor(itemView.context, R.color.blue
                            )
                        )
                    } else {
                        binding.bookmarkIcon.setColorFilter(
                            ContextCompat.getColor(itemView.context, R.color.dark_blue
                            )
                        )
                    }
                }
            }
            binding.bookmarkIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    bookmarkListener.onBookmarkClick(socialPosts[position], binding)
                }
            }

        }
    }

    interface OnBookmarkClickListener {
        fun onBookmarkClick(socialMediaPost: SocialMediaPost, binding: ItemAllPostBinding)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllSocialPostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAllPostBinding.inflate(inflater, parent, false)
        return AllSocialPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllSocialPostViewHolder, position: Int) {
        val socialPost = socialPosts[position]
        val userData = userDataMap[socialPost.UserID]
        holder.bind(socialPost, userData)
    }
    override fun getItemCount(): Int {
        return socialPosts.size
    }

    fun submitList(posts: List<SocialMediaPost>, userDataMap: Map<String, UserData?>) {
        socialPosts = posts
        this.userDataMap = userDataMap
        notifyDataSetChanged()
    }
    private fun expandTextView(textView: TextView) {
        textView.maxLines = Int.MAX_VALUE
        textView.ellipsize = null
        textView.isClickable = true
        textView.isFocusable = true
    }

    private fun collapseTextView(textView: TextView) {
        textView.maxLines = 3
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.isClickable = true
        textView.isFocusable = true
    }

    private fun toggleExpandCollapse(textView: TextView) {
        val isExpanded = textView.maxLines == Int.MAX_VALUE
        if (isExpanded) {
            collapseTextView(textView)
        } else {
            expandTextView(textView)
        }
    }
}


