package com.example.travel_itinerary_planner.tourism_attraction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.google.android.material.imageview.ShapeableImageView

data class Review(
    val username: String,
    val profileImage: String,
    val rateNo: String,
    val rateDate: String,
    val rateDesc: String
)
class ReviewAdapter(context: Context, private val dataSource: List<Review>) :
    ArrayAdapter<Review>(context, R.layout.item_review, dataSource) {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.item_review, parent, false)
        val review = getItem(position)

        val tvUsername = view.findViewById<TextView>(R.id.usernameTextView)
        val ivProfileImage = view.findViewById<ShapeableImageView>(R.id.imageView2)
        val rbRating = view.findViewById<RatingBar>(R.id.rating_user)
        val tvDate = view.findViewById<TextView>(R.id.dateTextView)
        val tvComment = view.findViewById<TextView>(R.id.commentTextView)

        tvUsername.text = review?.username

        // Use Glide to load the image from URL
        review?.profileImage?.let { imageUrl ->
            Glide.with(context)
                .load(imageUrl)
                .into(ivProfileImage)
        }

        rbRating.rating = review?.rateNo?.toFloatOrNull() ?: 0f
        tvDate.text = review?.rateDate
        tvComment.text = review?.rateDesc

        return view
    }
}