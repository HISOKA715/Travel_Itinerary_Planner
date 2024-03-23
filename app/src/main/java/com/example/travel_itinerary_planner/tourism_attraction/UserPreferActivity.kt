package com.example.travel_itinerary_planner.tourism_attraction


import android.os.Bundle
import android.widget.Toast
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.UserPreferManagementBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.SeekBar

class UserPreferActivity : LoggedInActivity() {
    private lateinit var binding: UserPreferManagementBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserPreferManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fetchUserPreferences()
        setupSeekBarListeners()
        setupSaveButton()
        binding.imageButton3.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserPreferences() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {

                        val preferShopping = document.getLong("PreferShopping")?.toInt() ?: 0
                        val preferRestaurant = document.getLong("PreferRestaurant")?.toInt() ?: 0
                        val preferOther = document.getLong("PreferOther")?.toInt() ?: 0
                        val preferAttraction = document.getLong("PreferAttraction")?.toInt() ?: 0


                        val totalPreferences = (preferShopping + preferRestaurant + preferOther + preferAttraction).toFloat()


                        binding.seekBarShopping.progress = preferShopping
                        binding.Shopping.text = calculatePercentage(preferShopping, totalPreferences)

                        binding.seekBarRestaurant.progress = preferRestaurant
                        binding.Restaurant.text = calculatePercentage(preferRestaurant, totalPreferences)

                        binding.seekBarOther.progress = preferOther
                        binding.Other.text = calculatePercentage(preferOther, totalPreferences)

                        binding.seekBarAttraction.progress = preferAttraction
                        binding.Attraction.text = calculatePercentage(preferAttraction, totalPreferences)
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching preferences: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun calculatePercentage(preference: Int, total: Float): String {
        val percentage = if (total > 0) (preference / total) * 100 else 0f
        return String.format("%.2f%%", percentage)
    }
    private fun setupSeekBarListeners() {
        val seekBars = listOf(
            binding.seekBarShopping,
            binding.seekBarRestaurant,
            binding.seekBarOther,
            binding.seekBarAttraction
        )

        val textViews = listOf(
            binding.Shopping,
            binding.Restaurant,
            binding.Other,
            binding.Attraction
        )
        seekBars.forEachIndexed { index, seekBar ->
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val totalPreferences = seekBars.sumOf { it.progress }.toFloat()
                    textViews.forEachIndexed { textViewIndex, textView ->
                        val preferenceProgress = seekBars[textViewIndex].progress.toFloat()
                        val percentage = if (totalPreferences > 0) preferenceProgress / totalPreferences else 0f
                        textView.text = String.format("%.2f%%", percentage * 100, seekBars[textViewIndex].progress, totalPreferences.toInt())
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }
    private fun setupSaveButton() {
        binding.button.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val preferences = hashMapOf(
                    "PreferShopping" to binding.seekBarShopping.progress,
                    "PreferRestaurant" to binding.seekBarRestaurant.progress,
                    "PreferOther" to binding.seekBarOther.progress,
                    "PreferAttraction" to binding.seekBarAttraction.progress
                )

                firestore.collection("users").document(userId).update(preferences as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Preferences saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving preferences: ${e.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

