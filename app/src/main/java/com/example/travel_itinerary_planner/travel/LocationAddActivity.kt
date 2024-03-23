package com.example.travel_itinerary_planner.travel

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.EditText
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LocationAddActivity : LoggedInActivity(){

    private lateinit var timeEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_location) // Replace with the actual layout file name

        timeEditText = findViewById(R.id.timeEditText)
        timeEditText.setOnClickListener { showTimePickerDialog() }
    }

    private fun showTimePickerDialog() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            timeEditText.setText(timeFormat.format(calendar.time))
        }

        val now = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this, timeSetListener,
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

}