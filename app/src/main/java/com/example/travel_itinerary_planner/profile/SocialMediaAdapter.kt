package com.example.travel_itinerary_planner.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ItemSocialMediaPostBinding

class SocialMediaAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<SocialMediaAdapter.SocialMediaViewHolder>() {

    private var socialMediaPosts: List<SocialMediaPost> = ArrayList()

    inner class SocialMediaViewHolder(private val binding: ItemSocialMediaPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageViewPost.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(socialMediaPosts[position])
                }
            }
        }

        fun bind(socialMediaPost: SocialMediaPost) {
            Glide.with(binding.root)
                .load(socialMediaPost.SocialImage)
                .override(300, 300)
                .error(R.drawable.travel_main)
                .centerCrop()
                .into(binding.imageViewPost)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(socialMediaPost: SocialMediaPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialMediaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSocialMediaPostBinding.inflate(inflater, parent, false)
        return SocialMediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialMediaViewHolder, position: Int) {
        holder.bind(socialMediaPosts[position])

    }

    override fun getItemCount(): Int {
        return socialMediaPosts.size
    }

    fun submitList(posts: List<SocialMediaPost>) {
        socialMediaPosts = posts
        notifyDataSetChanged()
    }
}
