package com.example.travel_itinerary_planner.social

import com.example.travel_itinerary_planner.profile.SocialMediaPost
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ItemOthersProfileBinding

class OthersProfileAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<OthersProfileAdapter.OthersProfileViewHolder>() {

    private var socialMediaPosts: List<SocialMediaPost> = ArrayList()

    inner class OthersProfileViewHolder(private val binding: ItemOthersProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imagePostPost.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(socialMediaPosts[position], socialMediaPosts[position].UserID)
                }
            }
        }

        fun bind(socialMediaPost: SocialMediaPost) {
            Glide.with(binding.root)
                .load(socialMediaPost.SocialImage)
                .override(300, 300)
                .error(R.drawable.travel_main)
                .centerCrop()
                .into(binding.imagePostPost)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OthersProfileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOthersProfileBinding.inflate(inflater, parent, false)
        return OthersProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OthersProfileViewHolder, position: Int) {
        val currentItem = socialMediaPosts[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return socialMediaPosts.size
    }

    fun submitList(posts: List<SocialMediaPost>) {
        socialMediaPosts = posts
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(socialMediaPost: SocialMediaPost, userId: String)
    }
    fun getSocialMediaPosts(): List<SocialMediaPost> {
        return socialMediaPosts
    }


}

