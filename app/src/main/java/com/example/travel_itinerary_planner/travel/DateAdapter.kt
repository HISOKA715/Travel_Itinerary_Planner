package com.example.travel_itinerary_planner.travel

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.R

class DateAdapter(private val dates: MutableList<DateItem>, private val context: Context) :
    RecyclerView.Adapter<DateAdapter.DateViewHolder>(){
    data class DateItem(val dayOfWeek: String, val dayOfMonth: String, var isSelected: Boolean)


    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfWeek: TextView = itemView.findViewById(R.id.text_day_of_week)
        val dayOfMonth: TextView = itemView.findViewById(R.id.text_day_of_month)
        val container: LinearLayout = itemView as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plan_date_list, parent, false)
        return DateViewHolder(view)
    }
    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateItem = dates[position]
        holder.dayOfWeek.text = dateItem.dayOfWeek
        holder.dayOfMonth.text = dateItem.dayOfMonth
        holder.itemView.isSelected = dateItem.isSelected

        holder.itemView.setBackgroundColor(
            if (dateItem.isSelected) ContextCompat.getColor(context, R.color.selectedColor) else ContextCompat.getColor(context, R.color.defaultColor)
        )

        holder.dayOfWeek.setTextColor(
            if (dateItem.isSelected) ContextCompat.getColor(context, R.color.selectedTextColor) else ContextCompat.getColor(context, R.color.defaultTextColor)
        )
        holder.dayOfMonth.setTextColor(
            if (dateItem.isSelected) ContextCompat.getColor(context, R.color.selectedTextColor) else ContextCompat.getColor(context, R.color.defaultTextColor)
        )

        val textColor = if (dateItem.isSelected) Color.WHITE else Color.BLACK
        holder.dayOfWeek.setTextColor(textColor)
        holder.dayOfMonth.setTextColor(textColor)

        // Handle the item click
        holder.itemView.setOnClickListener {

            if (!dateItem.isSelected) {

                dates.forEach { it.isSelected = false }
                dateItem.isSelected = true

                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return dates.size
    }
}