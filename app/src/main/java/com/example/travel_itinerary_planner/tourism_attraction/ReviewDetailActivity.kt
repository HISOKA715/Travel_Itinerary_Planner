package com.example.travel_itinerary_planner.tourism_attraction
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity

class ReviewDetailActivity: LoggedInActivity() {
    private lateinit var listView: ListView
    private lateinit var reviews: List<ReviewItem1>
    private lateinit var adapter: ReviewDetailAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_review) // Replace with your actual layout file
        val button: Button = findViewById(R.id.button)
        button.background = ContextCompat.getDrawable(this, R.drawable.rounded_button)

        button.setOnClickListener {
            val intent = Intent(this, AddReviewActivity::class.java)
            startActivity(intent)
        }

        listView = findViewById(R.id.item_review_detail)

        // Populate the review list (example data)
        reviews = listOf(
            ReviewItem1("Tan chun keat", "10/03/2024", "very bad", 3.7f, R.drawable.lake_symphony),
            ReviewItem1("Jane Doe", "12/04/2024", "very good", 4.5f, null) // No image for this item
            // Add more items...
        )

        adapter = ReviewDetailAdapter(this, reviews)
        listView.adapter = adapter
    }
}