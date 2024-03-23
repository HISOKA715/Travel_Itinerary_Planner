package com.example.travel_itinerary_planner.tourism_attraction
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.example.travel_itinerary_planner.R
data class ReviewItem1(
    val username: String,
    val date: String,
    val comment: String,
    val rating: Float,
    val imageResource: Int? // Use null for reviews without an image
)
class ReviewDetailAdapter (context: Context, private val reviews: List<ReviewItem1>) :
    ArrayAdapter<ReviewItem1>(context, 0, reviews) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.item_reviewdetail, parent, false)
        }

        val reviewItem = getItem(position)

        listItemView?.findViewById<TextView>(R.id.usernameTextView)?.text = reviewItem?.username
        listItemView?.findViewById<TextView>(R.id.dateTextView)?.text = reviewItem?.date
        listItemView?.findViewById<TextView>(R.id.commentTextView)?.text = reviewItem?.comment
        listItemView?.findViewById<RatingBar>(R.id.rating_user)?.rating = reviewItem?.rating ?: 0f

        // Handle the ImageView visibility
        val imageView = listItemView?.findViewById<ImageView>(R.id.imageView3)
        if (reviewItem?.imageResource != null) {
            imageView?.setImageResource(reviewItem.imageResource)
            imageView?.visibility = View.VISIBLE
        } else {
            imageView?.visibility = View.GONE
        }

        return listItemView!!
    }
}