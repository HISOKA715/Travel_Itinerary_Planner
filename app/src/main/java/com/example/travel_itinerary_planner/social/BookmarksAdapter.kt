package com.example.travel_itinerary_planner.social

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ItemBookmarksBinding
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.google.firebase.firestore.FirebaseFirestore

class BookmarksAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<BookmarksAdapter.BookmarksViewHolder>() {

    private var bookmarksList: List<SocialMediaPost> = ArrayList()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    inner class BookmarksViewHolder(private val binding: ItemBookmarksBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(bookmarksList[position])
                }
            }

        }

        fun bind(bookmark: SocialMediaPost) {
            val socialId = bookmark.SocialID
            getSocialImage(socialId) { socialImage ->
                Glide.with(binding.root)
                    .load(socialImage)
                    .override(300, 300)
                    .error(R.drawable.travel_main)
                    .centerCrop()
                    .into(binding.post)
            }
        }

        private fun getSocialImage(socialId: String, callback: (String) -> Unit) {
            firestore.collection("SocialMedia").document(socialId)
                .get()
                .addOnSuccessListener { document ->
                    val socialImage = document.getString("SocialImage")
                    socialImage?.let { callback(it) }
                }
                .addOnFailureListener { exception ->
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarksViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBookmarksBinding.inflate(inflater, parent, false)
        return BookmarksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarksViewHolder, position: Int) {
        holder.bind(bookmarksList[position])
    }

    override fun getItemCount(): Int {
        return bookmarksList.size
    }

    fun submitList(bookmarks: List<SocialMediaPost>) {
        bookmarksList = bookmarks
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(socialMediaPost: SocialMediaPost)
    }
}


