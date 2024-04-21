package com.example.travel_itinerary_planner.feedback

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FeedbackBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackFormActivity : LoggedInActivity() {
    private lateinit var binding: FeedbackBinding
    private var selectedAccuracy: Int = 0
    private var isRecommendation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.feedback)

        setupSpinner(binding.feedbackCate, R.array.feedback_category) { position ->
            isRecommendation = (position == 1)
            binding.feedbackAccurate.visibility = if (isRecommendation) View.VISIBLE else View.GONE
            binding.textshow.visibility = if (isRecommendation) View.VISIBLE else View.GONE
        }
        setupSpinner(binding.feedbackLocation, R.array.feedback_location, null)
        setupSpinner(binding.feedbackAccurate, R.array.feedback_accurate) { position ->
            selectedAccuracy = if (position == 1) 1 else 0
        }

        binding.imageButton3.setOnClickListener { finish() }
        binding.button.setOnClickListener {
            if (validateForm()) {
                submitFeedback()
            }
        }

        binding.travelNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun validateForm(): Boolean {
        val isCategoryValid = isSpinnerSelectionValid(binding.feedbackCate)
        val isLocationValid = isSpinnerSelectionValid(binding.feedbackLocation)
        val isAccuracyValid = if (isRecommendation) isSpinnerSelectionValid(binding.feedbackAccurate) else true
        val isDescriptionValid = isDescriptionValid()

        val isValid = isCategoryValid && isLocationValid && isAccuracyValid && isDescriptionValid

        if (!isValid) {
            when {
                !isCategoryValid -> showToast("Please select a feedback category.")
                !isLocationValid -> showToast("Please select a feedback location.")
                !isAccuracyValid && isRecommendation -> showToast("Please select the accuracy of your feedback.")
                !isDescriptionValid -> showToast("Please enter a description for your feedback.")
            }
        }

        return isValid
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int, visibilityHandler: ((Int) -> Unit)?) {
        ArrayAdapter.createFromResource(this, arrayResId, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                visibilityHandler?.invoke(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isSpinnerSelectionValid(spinner: Spinner): Boolean = spinner.selectedItemPosition != 0
    private fun isDescriptionValid(): Boolean = binding.travelNameEditText.text.toString().trim().isNotEmpty()
    private fun submitFeedback() {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val feedbackData = hashMapOf<String, Any>(
            "FeedbackCategory" to binding.feedbackCate.selectedItem.toString(),
            "FeedbackDate" to Timestamp.now(),
            "FeedbackDesc" to binding.travelNameEditText.text.toString().trim(),
            "FeedbackLocation" to binding.feedbackLocation.selectedItem.toString(),
            "read" to "true",
            "userID" to userId
        )
        if (isRecommendation) {
            feedbackData["FeedbackAccuracy"] = selectedAccuracy
        }
        firestore.collection("Feedback")
            .add(feedbackData)
            .addOnSuccessListener {
                Toast.makeText(this, "Feedback submitted successfully.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error submitting feedback: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

}