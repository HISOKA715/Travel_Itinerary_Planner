package com.example.travel_itinerary_planner.social

import SocialCommentBottomSheetFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ItemSocialMediaBinding
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.example.travel_itinerary_planner.social.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SocialPostAdapter(private val listener: OnMoreOptionsClickListener, private val bookmarkListener: OnBookmarkClickListener, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<SocialPostAdapter.SocialPostViewHolder>() {

    private var socialPosts: List<SocialMediaPost> = ArrayList()
    private var userDataMap: Map<String, UserData?> = HashMap()
    private var isExpandedMap: MutableMap<Int, Boolean> = HashMap()
    private lateinit var firestore: FirebaseFirestore
    inner class SocialPostViewHolder(private val binding: ItemSocialMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(socialPost: SocialMediaPost, userData: UserData?) {
            firestore = FirebaseFirestore.getInstance()
            binding.postContentTextView.text = socialPost.SocialContent
            binding.commentsIconImageView.setOnClickListener {
                val postId = socialPost.SocialID
                val bottomSheetFragment = SocialCommentBottomSheetFragment()
                val bundle = Bundle().apply {
                    putString("postId", postId)
                }
                bottomSheetFragment.arguments = bundle
                bottomSheetFragment.show(fragmentManager, "SocialCommentBottomSheetFragment")
            }
            val userSocialRef = firestore.collection("UserSocial")
            val query = userSocialRef.whereEqualTo("SocialID", socialPost.SocialID)

            query.get().addOnSuccessListener { querySnapshot ->
                val commentCount = querySnapshot.size().toString()
                val socialMediaRef = firestore.collection("SocialMedia").document( socialPost.SocialID)

                socialMediaRef.update("SocialCommentCounts", commentCount)
                    .addOnSuccessListener {
                        binding.commentsCountTextView.text=  "$commentCount Comments"
                    }
                    .addOnFailureListener { e ->

                    }
            }.addOnFailureListener { e ->

            }

            binding.postDateTextView.text = socialPost.SocialDate
            binding.textLocation.text = socialPost.SocialLocation
            binding.userNameTextView.setOnClickListener { view ->
                val position = adapterPosition
                val socialPost = socialPosts[position]
                val userId = socialPost.UserID

                if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    view.findNavController().navigate(R.id.navigation_profile)
                }
            }
            binding.userProfileImageView.setOnClickListener { view ->
                val position = adapterPosition
                val socialPost = socialPosts[position]
                val userId = socialPost.UserID

                if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    view.findNavController().navigate(R.id.navigation_profile)
                }
            }

            binding.postContentTextView.setOnClickListener {
                toggleExpandCollapse(binding.postContentTextView)
            }

            if (isExpandedMap[adapterPosition] == true) {
                expandTextView(binding.postContentTextView)
            } else {
                collapseTextView(binding.postContentTextView)
            }
            Glide.with(binding.root)
                .load(socialPost.SocialImage)
                .error(R.drawable.travel_main)
                .override(300, 300)
                .centerCrop()
                .into(binding.postImageView)

            userData?.let {
                Glide.with(binding.root)
                    .load(it.ProfileImage)
                    .error(R.drawable.travel_main)
                    .override(300, 300)
                    .circleCrop()
                    .into(binding.userProfileImageView)

                binding.userNameTextView.text = it.Username
            }
            binding.imageViewMore.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val post = socialPosts[position]
                    listener.onMoreOptionsClick(post)
                }
            }
            binding.bookmarkImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val post = socialPosts[position]
                    bookmarkListener.onBookmarkClick(post, binding)
                }
            }
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserUid != null) {
                val bookmarksRef = firestore.collection("Bookmarks")
                val bookmarkQuery = bookmarksRef
                    .whereEqualTo("UserID", currentUserUid)
                    .whereEqualTo("SocialID", socialPost.SocialID)

                bookmarkQuery.get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        binding.bookmarkImageView.setColorFilter(
                            ContextCompat.getColor(itemView.context, R.color.blue
                            )
                        )
                    } else {
                        binding.bookmarkImageView.setColorFilter(
                            ContextCompat.getColor(itemView.context, R.color.dark_blue
                            )
                        )
                    }
                }
            }

        }
    }

    interface OnBookmarkClickListener {
        fun onBookmarkClick(socialMediaPost: SocialMediaPost, binding: ItemSocialMediaBinding)
    }

    interface OnMoreOptionsClickListener {
        fun onMoreOptionsClick(socialMediaPost: SocialMediaPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialPostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSocialMediaBinding.inflate(inflater, parent, false)
        return SocialPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialPostViewHolder, position: Int) {
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
