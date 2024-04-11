package com.example.travel_itinerary_planner.travel

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.AddLocationBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class LocationAddActivity : LoggedInActivity() {

    private lateinit var binding: AddLocationBinding
    private var selectedHour: Int = -1
    private var selectedMinute: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extras = intent.extras

        val travelPlanID = extras?.getString("travel_planID")
        val locationDateID = extras?.getString("locationDateID")
        val photo = extras?.getString("photo")
        val locationName = extras?.getString("name")
        val locationAddress = extras?.getString("address")
        val locationstate = extras?.getString("state")
        val latitude = extras?.getDouble("latitude")
        val longitude = extras?.getDouble("longitude")
        val point = latitude?.let { lat ->
            longitude?.let { lng ->
                GeoPoint(lat, lng)
            }
        }
        binding.textView14.text = locationName
        binding.textView15.text = locationstate

        binding.textField.editText?.setText(locationAddress)
        binding.textField.isEnabled = false
        binding.textField.editText?.isEnabled = false
        binding.timeEditText.setOnClickListener { showTimePickerDialog() }
        binding.imageButton13.setOnClickListener { finish() }
        photo?.let {
            Glide.with(this)
                .load(it)
                .into(binding.imageView2)
        }

        binding.executedButton.setOnClickListener {
            showStatusSelectionDialog()
        }
        binding.PlanButton.setOnClickListener {
            addLocationRecord(point,travelPlanID,locationDateID,locationAddress,binding.travelNameEditText.text.toString())
        }
    }

    private fun addLocationRecord(point: GeoPoint?,travelPlanID:String?,locationDateID:String?, locationAddress:String?,locationName:String? ) {
        if (!validateInput()) return

        if (!validateInput() || selectedHour == -1 || selectedMinute == -1) {
            Toast.makeText(this, "Time not set", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val firestore = FirebaseFirestore.getInstance()

        val dateRef = firestore.collection("users/$userId/Travel_Plan/$travelPlanID/LocationDate")
            .document(locationDateID ?: return)

        dateRef.get().addOnSuccessListener { documentSnapshot ->
            val locationDateTimestamp = documentSnapshot.getTimestamp("LocationDate") ?: return@addOnSuccessListener
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"), Locale.getDefault()).apply {
                time = locationDateTimestamp.toDate()
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            val locationTime = Timestamp(calendar.time)

            val locationData = hashMapOf(
                "LocationAddress" to locationAddress,
                "LocationName" to locationName,
                "LocationStatus" to binding.executedButton.text.toString(),
                "LocationTime" to locationTime,
                "point" to point
            )
            firestore.collection("users/$userId/Travel_Plan/$travelPlanID/LocationDate/$locationDateID/Location")
                .add(locationData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Location added successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, TravelPlanActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun validateInput(): Boolean {
        if (binding.travelNameEditText.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a travel plan name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.timeEditText.text.toString().isEmpty()) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.executedButton.text.toString() == "Select Status") {
            Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun showStatusSelectionDialog() {
        val statusOptions = arrayOf("Ready", "Executed")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Status")
        builder.setItems(statusOptions) { dialog, which ->
            val selectedStatus = statusOptions[which]
            binding.executedButton.text = selectedStatus
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showTimePickerDialog() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            selectedHour = hour
            selectedMinute = minute

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"), Locale.getDefault())
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
            }
            binding.timeEditText.setText(timeFormat.format(calendar.time))
        }

        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"), Locale.getDefault())
        val timePickerDialog = TimePickerDialog(this, timeSetListener, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false)
        timePickerDialog.show()
    }
}