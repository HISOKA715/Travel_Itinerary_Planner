package com.example.travel_itinerary_planner.home

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.tourism_attraction.TourismActivity

data class TourismAttraction(
    val id: String,
    val imageUrl: String,
    val name: String,
    val location: String
)

class TourismListAdapter(
    private val attractions: List<TourismAttraction>,
    private val onItemClicked: (TourismAttraction) -> Unit
) : RecyclerView.Adapter<TourismListAdapter.AttractionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tourism_attaction_view, parent, false)
        return AttractionViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        holder.bind(attractions[position])
    }

    override fun getItemCount(): Int = attractions.size

    class AttractionViewHolder(
        itemView: View,
        private val onItemClicked: (TourismAttraction) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView5)
        private val nameTextView: TextView = itemView.findViewById(R.id.textView31)
        private val locationTextView: TextView = itemView.findViewById(R.id.textView32)
        private val planButton: Button = itemView.findViewById(R.id.PlanButton)

        fun bind(attraction: TourismAttraction) {
            Glide.with(itemView.context)
                .load(attraction.imageUrl)
                .into(imageView)
            nameTextView.text = attraction.name
            locationTextView.text = attraction.location

            planButton.setOnClickListener {
                Log.d("PlanButtonClicked", "Document ID: ${attraction.id}")
                onItemClicked(attraction)

                val context = itemView.context
                val intent = Intent(context, TourismActivity::class.java).apply {
                    putExtra("documentId", attraction.id)
                }
                context.startActivity(intent)
            }
        }
    }
}