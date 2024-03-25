package com.example.travel_itinerary_planner.travel

import android.content.Context
import android.graphics.Color
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.R
import java.util.Date

interface OnItemDoubleTapListener {
    fun onItemDoubleTapped(documentId: String, date: Date)
}
class DateAdapter(
    private val dates: MutableList<DateItem>,
    private val context: Context,
    private val onDateSelected: (String) -> Unit,
    private val onDateLongPressed: (String) -> Unit,
    private val onItemDoubleTapListener: OnItemDoubleTapListener
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfWeek: TextView = itemView.findViewById(R.id.text_day_of_week)
        val dayOfMonth: TextView = itemView.findViewById(R.id.text_day_of_month)
        val container: LinearLayout = itemView.findViewById(R.id.container)

        init {
            // Setup GestureDetector inside ViewHolder
            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onItemDoubleTapListener.onItemDoubleTapped(dates[position].documentId, dates[position].fullDate)
                    }
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    // Handle item click here to ensure it's recognized as a single tap
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val previousSelectedPosition = selectedPosition
                        selectedPosition = adapterPosition
                        notifyItemChanged(previousSelectedPosition)
                        notifyItemChanged(selectedPosition)
                        onDateSelected(dates[adapterPosition].documentId)
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onDateLongPressed(dates[adapterPosition].documentId)
                    }
                }
            })

            itemView.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        }

        fun bind(dateItem: DateItem) {
            dayOfWeek.text = dateItem.dayOfWeek
            dayOfMonth.text = dateItem.dayOfMonth

            updateVisuals(adapterPosition == selectedPosition)
        }

        private fun updateVisuals(isSelected: Boolean) {
            container.setBackgroundColor(
                if (isSelected) ContextCompat.getColor(context, R.color.selectedColor)
                else ContextCompat.getColor(context, R.color.defaultColor)
            )
            val textColor = if (isSelected) Color.WHITE else Color.BLACK
            dayOfWeek.setTextColor(textColor)
            dayOfMonth.setTextColor(textColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plan_date_list, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateItem = dates[position]
        holder.bind(dateItem)
    }

    override fun getItemCount(): Int = dates.size

    data class DateItem(
        val dayOfWeek: String,
        val dayOfMonth: String,
        var isSelected: Boolean,
        val fullDate: Date,
        val documentId: String
    )
}