package com.example.travel_itinerary_planner.tourism_attraction
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
data class ReviewItem1(
    val username: String,
    val profileImage :String,
    val date: String,
    val comment: String,
    val rating: String,
    val imageResource: String
)
class ReviewDetailAdapter(context: Context, private val reviews: List<ReviewItem1>) :
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
        listItemView?.findViewById<RatingBar>(R.id.rating_user)?.rating =  reviewItem?.rating?.toFloatOrNull() ?: 0f
        val imageView = listItemView?.findViewById<ImageView>(R.id.imageView3)
        val imageProfile= listItemView?.findViewById<ImageView>(R.id.imageView2)
        if (reviewItem?.imageResource != null && reviewItem.imageResource.isNotEmpty()) {
            Glide.with(context)
                .load(reviewItem.imageResource)
                .into(imageView!!)
            imageView.visibility = View.VISIBLE
        } else {
            imageView?.visibility = View.GONE
        }
        Glide.with(context)
            .load(reviewItem?.profileImage)
            .into(imageProfile!!)
        return listItemView!!
    }
}