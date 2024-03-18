package com.example.travel_itinerary_planner.tourism_attraction
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.TourismAttractionBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import androidx.activity.OnBackPressedCallback
class TourismActivity : LoggedInActivity() {
    private lateinit var binding:TourismAttractionBinding
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TourismAttractionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                finish()
            }
        }
        setUtilitiesSelected()
        binding.textView16.setOnClickListener {
            setUtilitiesSelected()
        }

        binding.textView17.setOnClickListener {
            setReviewSelected()
        }
        onBackPressedDispatcher.addCallback(this, callback)
        binding.imageButtonSearch1.setOnClickListener {
            callback.handleOnBackPressed()
        }

        // Handle favorite button click
        binding.imageButtonSearch.setOnClickListener {
            isFavorite = !isFavorite // Toggle the favorite state
            updateFavoriteIcon()
        }
}


    private fun setUtilitiesSelected() {
        binding.textView16.setTextColor(resources.getColor(R.color.black, null))
        binding.textView17.setTextColor(resources.getColor(R.color.grayText, null)) // Assuming grayText is defined in your colors.xml as #c0bcbc
    }

    private fun setReviewSelected() {
        binding.textView16.setTextColor(resources.getColor(R.color.grayText, null)) // Assuming grayText is defined in your colors.xml as #c0bcbc
        binding.textView17.setTextColor(resources.getColor(R.color.black, null))
    }
    private fun updateFavoriteIcon() {
        // Update the icon based on the isFavorite flag
        val iconResId = if (isFavorite) {
            R.drawable.baseline_favorite_24 // Favorite icon
        } else {
            R.drawable.baseline_favorite_border_24 // Non-favorite icon
        }
        binding.imageButtonSearch.setImageResource(iconResId)
    }
}


