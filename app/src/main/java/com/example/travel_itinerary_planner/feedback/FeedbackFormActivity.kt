package com.example.travel_itinerary_planner.feedback

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FeedbackBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity

class FeedbackFormActivity : LoggedInActivity() {

    // Create a binding property
    private lateinit var binding: FeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.feedback)

        setupSpinner(binding.feedbackCate, R.array.feedback_category)
        setupSpinner(binding.feedbackLocation, R.array.feedback_location)
        setupSpinner(binding.feedbackAccurate, R.array.feedback_accurate)
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int) {
        ArrayAdapter.createFromResource(
            this,
            arrayResId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }
}