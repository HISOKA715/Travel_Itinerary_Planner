package com.example.travel_itinerary_planner.social

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ItemSocialMediaBinding
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.example.travel_itinerary_planner.social.UserData
class SocialPostAdapter : RecyclerView.Adapter<SocialPostAdapter.SocialPostViewHolder>() {

    private var socialPosts: List<SocialMediaPost> = ArrayList()
    private var userDataMap: Map<String, UserData?> = HashMap()
    private var isExpandedMap: MutableMap<Int, Boolean> = HashMap()
    inner class SocialPostViewHolder(private val binding: ItemSocialMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(socialPost: SocialMediaPost, userData: UserData?) {
            binding.postContentTextView.text = socialPost.SocialContent
            binding.commentsCountTextView.text = socialPost.SocialCommentCounts
            binding.postDateTextView.text = socialPost.SocialDate


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
        }
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


