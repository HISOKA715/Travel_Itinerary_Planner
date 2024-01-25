package com.example.travel_itinerary_planner
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class GenderAdapter(context: Context, private val genders: Array<String>) :
    ArrayAdapter<String>(context, R.layout.item_gender, genders) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_gender, parent, false)
        val genderImageView = view.findViewById<ImageView>(R.id.genderImageView)
        val genderTextView = view.findViewById<TextView>(R.id.genderTextView)

        val colorBlue = ContextCompat.getColor(context, R.color.dark_blue)
        genderImageView.setColorFilter(colorBlue)

        val gender = genders[position]
        genderTextView.text = gender
        if (gender == "Female") {
            genderImageView.setImageResource(R.drawable.baseline_female_24)
        } else if (gender == "Male") {
            genderImageView.setImageResource(R.drawable.baseline_male_24)
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }
}
