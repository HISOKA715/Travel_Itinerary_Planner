package com.example.travel_itinerary_planner.tourism_attraction

import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity

class AddReviewActivity :  LoggedInActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_review)

    }
}