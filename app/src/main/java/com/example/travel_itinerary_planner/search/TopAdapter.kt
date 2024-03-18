package com.example.travel_itinerary_planner.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.travel_itinerary_planner.R

data class LocationData(val name: String, val distance: String)
class TopAdapter (context: Context, private val dataSource: List<LocationData>) :
    ArrayAdapter<LocationData>(context, R.layout.top_attraction, dataSource) {
    private class ViewHolder {
        lateinit var numberLabel: TextView
        lateinit var locationName: TextView
        lateinit var distance: TextView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.top_attraction, parent, false)
            holder = ViewHolder()
            holder.numberLabel = view.findViewById(R.id.numberLabel)
            holder.locationName = view.findViewById(R.id.locationName)
            holder.distance = view.findViewById(R.id.distance)
            view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        val locationData = getItem(position)
        holder.numberLabel.text = (position + 1).toString()
        holder.locationName.text = locationData?.name
        holder.distance.text = locationData?.distance

        return view
    }

}