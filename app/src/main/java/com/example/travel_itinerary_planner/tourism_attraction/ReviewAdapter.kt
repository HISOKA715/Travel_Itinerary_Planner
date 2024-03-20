package com.example.travel_itinerary_planner.tourism_attraction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.TextView
import com.example.travel_itinerary_planner.R
import com.google.android.material.imageview.ShapeableImageView

data class Review(
    val username: String,
    val profileImageResId: Int,
    val rating: Float,
    val date: String,
    val comment: String
)
class ReviewAdapter(context: Context, private val dataSource: List<Review>) :
    ArrayAdapter<Review>(context, R.layout.item_review, dataSource) {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Check if an existing view is being reused, otherwise inflate the view
        val view: View = convertView ?: inflater.inflate(R.layout.item_review, parent, false)

        // Get the data item for this position
        val review = getItem(position)

        // Lookup view for data population
        val tvUsername = view.findViewById<TextView>(R.id.usernameTextView)
        val ivProfileImage = view.findViewById<ShapeableImageView>(R.id.imageView2)
        val rbRating = view.findViewById<RatingBar>(R.id.rating_user)
        val tvDate = view.findViewById<TextView>(R.id.dateTextView)
        val tvComment = view.findViewById<TextView>(R.id.commentTextView)

        // Populate the data into the template view using the data object
        tvUsername.text = review?.username
        ivProfileImage.setImageResource(review?.profileImageResId ?: R.drawable.chat)
        rbRating.rating = review?.rating ?: 0f
        tvDate.text = review?.date
        tvComment.text = review?.comment

        // Return the completed view to render on screen
        return view
    }

}