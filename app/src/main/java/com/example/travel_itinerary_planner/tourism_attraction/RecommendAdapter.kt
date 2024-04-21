package com.example.travel_itinerary_planner.tourism_attraction

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R

data class TravelPlanItem(val id:String,val title: String, val location: String, val imageUrl: String)

class RecommendAdapter(private val context: Context, private val dataSource: List<TravelPlanItem>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.item_tourism, parent, false)
            holder = ViewHolder()
            holder.titleTextView = view.findViewById(R.id.textView31)
            holder.locationTextView = view.findViewById(R.id.textView32)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val travelPlanItem = getItem(position) as TravelPlanItem
        holder.titleTextView?.text = travelPlanItem.title
        holder.locationTextView?.text = travelPlanItem.location
        val imageView = view.findViewById<ImageView>(R.id.imageView5)
        Glide.with(context).load(travelPlanItem.imageUrl).into(imageView)
        val planButton = view.findViewById<Button>(R.id.PlanButton)
        planButton.setOnClickListener {
            val intent = Intent(context, TourismActivity::class.java).apply {
                putExtra("documentId", travelPlanItem.id)
            }
            context.startActivity(intent)
        }
        return view
    }

    private class ViewHolder {
        var titleTextView: TextView? = null
        var locationTextView: TextView? = null
    }
}