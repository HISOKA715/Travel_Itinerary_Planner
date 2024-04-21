package com.example.travel_itinerary_planner.travel

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.EditLocationBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class LocationEditActivity : LoggedInActivity() {

    private lateinit var binding: EditLocationBinding
    private var selectedHour: Int = -1
    private var selectedMinute: Int = -1
    private val userId1 = FirebaseAuth.getInstance().currentUser?.uid
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extras = intent.extras
        val locationID = extras?.getString("locationID")
        val travelPlanID = extras?.getString("travel_planID")
        val locationDateID = extras?.getString("locationDateID")
        val photo = extras?.getString("photo")
        val locationName = extras?.getString("name")
        val locationAddress = extras?.getString("address")
        val locationstate = extras?.getString("state")
        val latitude = extras?.getDouble("latitude")
        val longitude = extras?.getDouble("longitude")
        Log.d("LocationEditActivity", "Latitude: $latitude, Longitude: $longitude")
        val point = latitude?.let { lat ->
            longitude?.let { lng ->
                GeoPoint(lat, lng)
            }
        }
        binding.textView14.text = locationName
        binding.textView15.text = locationstate
        binding.deleteIcon.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Delete Location")
                setMessage("Are you sure you want to delete this location?")
                setPositiveButton("Yes") { dialog, which ->

                    deleteLocation(locationID, travelPlanID, locationDateID)
                }
                setNegativeButton("No", null)
                show()
            }
        }
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
            editLocationRecord(locationID,point,travelPlanID,locationDateID,locationAddress,locationName)
        }

        fetchLocationDetails(userId1,travelPlanID,locationDateID,locationID)
    }

    private fun deleteLocation(locationID: String?, travelPlanID: String?, locationDateID: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null || locationID == null || travelPlanID == null || locationDateID == null) {
            Toast.makeText(this, "Error deleting location: Invalid parameters.", Toast.LENGTH_SHORT).show()
            return
        }
        val locationRef = FirebaseFirestore.getInstance()
            .collection("users/$userId/Travel_Plan/$travelPlanID/LocationDate/$locationDateID/Location")
            .document(locationID)
        locationRef.delete().addOnSuccessListener {
            Toast.makeText(this, "Location deleted successfully.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TravelPlanActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error deleting location: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchLocationDetails(userId: String?, travelPlanID: String?, locationDateID: String?, locationID: String?) {
        val db = FirebaseFirestore.getInstance()
        userId?.let { uid ->
            travelPlanID?.let { tpID ->
                locationDateID?.let { ldID ->
                    locationID?.let { lID ->
                        val locationRef = db.collection("users/$uid/Travel_Plan/$tpID/LocationDate/$ldID/Location").document(lID)
                        locationRef.get().addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val locationName = documentSnapshot.getString("LocationName")
                                val locationStatus = documentSnapshot.getString("LocationStatus")
                                val locationTime = documentSnapshot.getTimestamp("LocationTime")

                                binding.travelNameEditText.text = Editable.Factory.getInstance().newEditable(locationName ?: "No Name")
                                binding.executedButton.text = locationStatus ?: "No Status"

                                locationTime?.toDate()?.let { date ->
                                    val calendar = Calendar.getInstance().apply {
                                        timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                                        time = date
                                    }
                                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
                                        timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                                    }
                                    selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
                                    selectedMinute = calendar.get(Calendar.MINUTE)

                                    Log.d("min", "$selectedHour")
                                    Log.d("min", "$selectedMinute")
                                    binding.timeEditText.text = Editable.Factory.getInstance().newEditable(sdf.format(date))
                                } ?: run {
                                    binding.timeEditText.text = Editable.Factory.getInstance().newEditable("No Time Set")
                                }
                            } else {
                                Toast.makeText(this, "Location does not exist", Toast.LENGTH_LONG).show()
                            }
                        }.addOnFailureListener { exception ->
                            Log.d("LocationEditActivity", "get failed with ", exception)
                            Toast.makeText(this, "Failed to fetch location details", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        } ?: return
    }

        private fun editLocationRecord(locationID: String?, point: GeoPoint?, travelPlanID: String?, locationDateID: String?, locationAddress: String?, locationName: String?) {
            if (!validateInput()) return

            if (selectedHour == -1 || selectedMinute == -1) {
                Toast.makeText(this, "Time not set", Toast.LENGTH_SHORT).show()
                return
            }
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val locationRef = FirebaseFirestore.getInstance()
                .collection("users/$userId/Travel_Plan/$travelPlanID/LocationDate/$locationDateID/Location")
                .document(locationID ?: return)

            val dateRef = FirebaseFirestore.getInstance().collection("users/$userId/Travel_Plan/$travelPlanID/LocationDate")
                .document(locationDateID ?: return)

            dateRef.get().addOnSuccessListener { documentSnapshot ->
                val locationDateTimestamp = documentSnapshot.getTimestamp("LocationDate") ?: return@addOnSuccessListener
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"), Locale.getDefault()).apply {
                    time = locationDateTimestamp.toDate()
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                val locationTime = Timestamp(calendar.time)
                val updateMap = hashMapOf<String, Any>().apply {
                    put("LocationAddress", locationAddress ?: "")
                    put("LocationName", binding.travelNameEditText.text.toString())
                    put("LocationStatus", binding.executedButton.text.toString())
                    put("LocationTime", locationTime)
                    Log.d("LocationEditActivity", "GeoPoint to update: $point")
                    put("point", point ?: GeoPoint(0.0, 0.0))
                }
                locationRef.update(updateMap).addOnSuccessListener {
                    Toast.makeText(this, "Location updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, TravelPlanActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating location: ${e.message}", Toast.LENGTH_SHORT).show()
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