package com.example.travel_itinerary_planner.tourism_attraction
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.TourismAttractionBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import androidx.activity.OnBackPressedCallback
class TourismActivity : LoggedInActivity() {
    private lateinit var binding:TourismAttractionBinding
    private lateinit var reviewsAdapter: ReviewAdapter
    private var isFavorite = false
    private var reviewsList = mutableListOf<Review>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TourismAttractionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imageButton: ImageButton = binding.imageButton2
        imageButton.setOnClickListener {
            val intent = Intent(this, ReviewDetailActivity::class.java)
            startActivity(intent)
        }
        reviewsList.add(Review("Tan chun keat", R.drawable.lake_symphony, 3.7f, "10/03/2024", "very badlol"))
        reviewsAdapter = ReviewAdapter(this, reviewsList)
        binding.listReview.adapter = reviewsAdapter
        binding.textView20.post {
            // Check if the TextView with maxLines=3 is truncated
            if (isTextViewTruncated(binding.textView20)) {
                binding.viewmore.visibility = View.VISIBLE
            } else {
                binding.viewmore.visibility = View.GONE
            }
        }


        var isExpanded = false
        binding.viewmore.setOnClickListener {
            toggleTextExpansion()
        }

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

        binding.viewmore.setOnClickListener {
            if (isExpanded) {
                // Collapse the text view
                binding.textView20.maxLines = 3
                binding.viewmore.text = getString(R.string.view_more)
            } else {
                // Expand the text view
                binding.textView20.maxLines = Integer.MAX_VALUE
                binding.viewmore.text = getString(R.string.view_less)
            }
            isExpanded = !isExpanded
        }
}

    private fun toggleTextExpansion() {
        with(binding) {
            if (textView20.maxLines == 3) {
                textView20.maxLines = Integer.MAX_VALUE
                viewmore.text = getString(R.string.view_less)
            } else {
                textView20.maxLines = 3
                viewmore.text = getString(R.string.view_more)
            }
        }
    }

    private fun isTextViewTruncated(textView: TextView): Boolean {
        val layout = textView.layout ?: return false
        val lines = layout.lineCount
        if (lines > 0) {
            val ellipsisCount = layout.getEllipsisCount(lines - 1)
            if (ellipsisCount > 0) {
                return true
            }
        }
        return false
    }

    private fun setUtilitiesSelected() {
        binding.textView16.setTextColor(resources.getColor(R.color.black, null))
        binding.textView17.setTextColor(resources.getColor(R.color.grayText, null))

        binding.utilitiesContainer.visibility = View.VISIBLE
        binding.reviewContainer.visibility = View.GONE
    }

    private fun setReviewSelected() {
        binding.textView16.setTextColor(resources.getColor(R.color.grayText, null))
        binding.textView17.setTextColor(resources.getColor(R.color.black, null))

        binding.utilitiesContainer.visibility = View.GONE
        binding.reviewContainer.visibility = View.VISIBLE
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


