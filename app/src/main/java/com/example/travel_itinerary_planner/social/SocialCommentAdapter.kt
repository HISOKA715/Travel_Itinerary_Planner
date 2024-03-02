package com.example.travel_itinerary_planner.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ItemCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SocialCommentAdapter( private val deleteCommentCallback: (UserSocial) -> Unit) : RecyclerView.Adapter<SocialCommentAdapter.SocialCommentViewHolder>() {

    private var commentList: List<UserSocial> = ArrayList()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialCommentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCommentBinding.inflate(inflater, parent, false)
        return SocialCommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialCommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun submitList(comments: List<UserSocial>) {
        commentList = comments
        notifyDataSetChanged()
    }

    inner class SocialCommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userSocial: UserSocial) {
            firestore = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()

            binding.apply {
                firestore.collection("users").document(userSocial.UserID)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val userData = document.toObject(UserData::class.java)
                            if (userData != null) {
                                usernameCommentTextView.text = userData.Username

                                Glide.with(binding.root)
                                    .load(userData.ProfileImage)
                                    .error(R.drawable.travel_main)
                                    .override(300, 300)
                                    .centerCrop()
                                    .into(profileCommentImageView)
                            }
                            commentContentTextView.text = userSocial.UserComment
                            timeCommentTextView.text = userSocial.CreateDate
                            if (!userSocial.CommentImage.isNullOrEmpty()) {
                                Glide.with(binding.root)
                                    .load(userSocial.CommentImage)
                                    .error(R.drawable.travel_main)
                                    .override(300, 300)
                                    .centerCrop()
                                    .into(imageComments)
                                imageComments.visibility = View.VISIBLE
                            } else {
                                imageComments.visibility = View.GONE
                            }
                        }

                    }

                if (userSocial.UserID == auth.currentUser?.uid) {
                        commentBar.setOnLongClickListener{
                        deleteCommentIcon.visibility = View.VISIBLE
                        deleteCommentIcon.setOnClickListener {

                        deleteCommentCallback(userSocial)
                        }
                        true
                        }

                } else {

                    deleteCommentIcon.visibility = View.GONE
                }


            }

        }
    }
}
