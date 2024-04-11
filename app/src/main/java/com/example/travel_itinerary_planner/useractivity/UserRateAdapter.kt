package com.example.travel_itinerary_planner.useractivity

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

data class UserReview(
    var id: String,
    var attractionId: String,
    var username: String,
    var profileImage: String,
    var rateNo: String,
    var rateDate: String,
    var rateDesc: String,
    var rateImg: String
)
class UserRateAdapter(
    context: Context,
    private val userReviewList: List<UserReview>
) : ArrayAdapter<UserReview>(context, 0, userReviewList) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.rate_list, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        val userReview = userReviewList[position]
        viewHolder.usernameTextView.text = userReview.username
        viewHolder.rateDateTextView.text = userReview.rateDate
        viewHolder.commentTextView.text = userReview.rateDesc
        viewHolder.ratingBar.rating = userReview.rateNo.toFloat()

        if (userReview.profileImage.isNotEmpty()) {
            Glide.with(context)
                .load(userReview.profileImage)
                .into(viewHolder.profileImageView)
        }

        if (userReview.rateImg.isNotEmpty()) {
            Glide.with(context)
                .load(userReview.rateImg)
                .into(viewHolder.rightSideImageView)
        }

        return view
    }


    private class ViewHolder(view: View) {
        val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        val profileImageView: ImageView = view.findViewById(R.id.imageView2)
        val rateDateTextView: TextView = view.findViewById(R.id.dateTextView)
        val commentTextView: TextView = view.findViewById(R.id.commentTextView)
        val ratingBar: RatingBar = view.findViewById(R.id.rating_user)
        val rightSideImageView: ImageView = view.findViewById(R.id.rightSideImageView)
    }
}