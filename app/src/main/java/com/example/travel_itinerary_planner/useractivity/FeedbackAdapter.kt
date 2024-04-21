package com.example.travel_itinerary_planner.useractivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.travel_itinerary_planner.R


data class FeedbackItem(val id:String,val title: String, val content: String, val isRead: Boolean, val date: String)
class FeedbackAdapter (context: Context, private val items: List<FeedbackItem>)
    : ArrayAdapter<FeedbackItem>(context, 0, items){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.feedback_list, parent, false)
        }

        val item = getItem(position)

        val titleTextView = listItemView!!.findViewById<TextView>(R.id.tvTitle)
        val contentTextView = listItemView.findViewById<TextView>(R.id.tvContent)
        val dateTextView = listItemView.findViewById<TextView>(R.id.tvDate)
        val redDotView = listItemView.findViewById<View>(R.id.viewRedDot)

        titleTextView.text = item?.title
        contentTextView.text = item?.content
        dateTextView.text = item?.date
        redDotView.visibility = if (item?.isRead == false) View.VISIBLE else View.GONE

        return listItemView
    }
}