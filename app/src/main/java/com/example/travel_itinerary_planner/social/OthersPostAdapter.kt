package com.example.travel_itinerary_planner.social

import SocialCommentBottomSheetFragment
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
import com.example.travel_itinerary_planner.databinding.ItemAllPostBinding
import com.example.travel_itinerary_planner.databinding.ItemOthersPostDetailsBinding
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OthersPostAdapter(private val listener: OnMoreOptionsClickListener,private val bookmarkListener: OnBookmarkClickListener, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<OthersPostAdapter.OthersPostViewHolder>() {

    private var socialPosts: List<SocialMediaPost> = ArrayList()
    private var userDataMap: Map<String, UserData?> = HashMap()
    private var isExpandedMap: MutableMap<Int, Boolean> = HashMap()
    private lateinit var firestore: FirebaseFirestore
    inner class OthersPostViewHolder(private val binding: ItemOthersPostDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(socialPost: SocialMediaPost, userData: UserData?) {
            firestore = FirebaseFirestore.getInstance()
            binding.postContent.text = socialPost.SocialContent
            binding.comments.setOnClickListener {
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
                        binding.commentsCount.text=  "$commentCount Comments"
                    }
                    .addOnFailureListener { e ->

                    }
            }.addOnFailureListener { e ->

            }

            binding.postDate.text = socialPost.SocialDate
            binding.location.text=socialPost.SocialLocation
            binding.userNameView.setOnClickListener { view ->
                val position = adapterPosition
                val socialPost = socialPosts[position]
                val userId = socialPost.UserID

                val bundle = Bundle().apply {
                    putString("userId", userId)
                }
                view.findNavController().navigate(R.id.navigation_others_profile, bundle)
            }
            binding.profileImageView.setOnClickListener { view ->
                val position = adapterPosition
                val socialPost = socialPosts[position]
                val userId = socialPost.UserID

                val bundle = Bundle().apply {
                    putString("userId", userId)
                }
                view.findNavController().navigate(R.id.navigation_others_profile, bundle)
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
                    .into(binding.profileImageView)

                binding.userNameView.text = it.Username
            }
            binding.more.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val post = socialPosts[position]
                        listener.onMoreOptionsClick(post)
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
                        binding.bookmark.setColorFilter(
                            ContextCompat.getColor(itemView.context, R.color.blue
                            )
                        )
                    } else {
                        binding.bookmark.setColorFilter(
                            ContextCompat.getColor(itemView.context, R.color.dark_blue
                            )
                        )
                    }
                }
            }
            binding.bookmark.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    bookmarkListener.onBookmarkClick(socialPosts[position], binding)
                }
            }

        }
    }

    interface OnMoreOptionsClickListener {
        fun onMoreOptionsClick(socialMediaPost: SocialMediaPost)
    }
    interface OnBookmarkClickListener {
        fun onBookmarkClick(socialMediaPost: SocialMediaPost, binding: ItemOthersPostDetailsBinding)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OthersPostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOthersPostDetailsBinding.inflate(inflater, parent, false)
        return OthersPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OthersPostViewHolder, position: Int) {
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
    fun getSocialMediaPosts(): List<SocialMediaPost> {
        return socialPosts
    }
}


