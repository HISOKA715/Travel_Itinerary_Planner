package com.example.travel_itinerary_planner.travel

import LocationAdapter
import LocationItem
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.PlanDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TravelPlanActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var binding: PlanDetailBinding
    private lateinit var dateAdapter: DateAdapter
    private lateinit var locationAdapter: LocationAdapter
    private var locationItems: ArrayList<LocationItem> = ArrayList()
    private var dateList = mutableListOf<DateAdapter.DateItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val docId = intent.getStringExtra("docId") ?: return
        fetchAndDisplayPlanName(docId)
        locationAdapter = LocationAdapter(this, locationItems) { clickedItem ->
            Log.d("LocationClicked", "Clicked on: ${clickedItem.title}")
        }
        listView = findViewById(R.id.location_list)
        listView.adapter = locationAdapter
        setupDateRecyclerView(docId)
        if (docId.isNotEmpty()) {
            fetchLocationDates(docId)
        }
        binding.imageButton3.setOnClickListener {
            finish()
        }
        binding.imageButton5.setOnClickListener {
            showDatePicker()
        }

        binding.imageButton10.setOnClickListener {
            val intent = Intent(this@TravelPlanActivity, LocationFindActivity::class.java)
            startActivity(intent)
        }

    }






    private fun showDatePicker() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"), Locale.getDefault())
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"), Locale.getDefault())
            selectedDate.set(year, monthOfYear, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
            }
            val dateStr = sdf.format(selectedDate.time)

            checkAndAddDate(dateStr)
        }, year, month, day)

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { dialog, which ->
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss()
            }
        }

        datePickerDialog.show()
    }
    private fun checkAndAddDate(selectedDateString: String) {
        val docId = intent.getStringExtra("docId") ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val locationDateRef = FirebaseFirestore.getInstance().collection("users/$userId/Travel_Plan/$docId/LocationDate")
        locationDateRef.whereEqualTo("LocationDateString", selectedDateString).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val sdfParse = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                    }
                    val newDate = hashMapOf(
                        "LocationDate" to sdfParse.parse(selectedDateString),
                        "LocationDateString" to selectedDateString
                    )
                    locationDateRef.add(newDate)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Date added successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error adding date: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "This date already exists.", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun setupDateRecyclerView(docId: String) {
        dateAdapter = DateAdapter(dateList, this,
            { documentId ->
                fetchLocations(docId, documentId)
            },
            { documentId ->
                showDeleteConfirmationDialog(docId, documentId)
            },
            object : OnItemDoubleTapListener {
                override fun onItemDoubleTapped(documentId: String, date: Date) {
                    showDatePicker(documentId, date,docId)
                }

                private fun showDatePicker(documentId: String, currentDate: Date,docId:String) {
                    val calendar = Calendar.getInstance().apply {
                        time = currentDate
                        timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                    }

                    DatePickerDialog(this@TravelPlanActivity, { _, year, month, dayOfMonth ->
                        val newDateCalendar = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth)
                            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                        }
                        Log.d("DatePicker", "Selected: ${newDateCalendar.time}")
                        updateDate(documentId, newDateCalendar.time,docId)
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                }
            }
        )
        binding.recyclerViewDates.apply {
            layoutManager = LinearLayoutManager(this@TravelPlanActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
            addItemDecoration(HorizontalSpaceItemDecoration(8))
        }
    }

    private fun updateDate(documentId: String, newDate: Date, docId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Prepare a Calendar instance with the newDate
        val calendar = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
            time = newDate
            add(Calendar.HOUR_OF_DAY, 8) // Add 8 hours to the date
        }

        val adjustedNewDate = calendar.time

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        }
        val newDateString = sdf.format(adjustedNewDate)

        val locationDateRef = FirebaseFirestore.getInstance().collection("users/$userId/Travel_Plan/$docId/LocationDate")
        locationDateRef.whereEqualTo("LocationDateString", newDateString).get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                Toast.makeText(this, "This date already exists. Please choose another date.", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }

            // Prepare update map with adjusted date
            val updateMap = mapOf(
                "LocationDate" to adjustedNewDate,
                "LocationDateString" to newDateString
            )

            // Perform update with the adjusted date
            locationDateRef.document(documentId).update(updateMap).addOnSuccessListener {
                updateLocationTimes(documentId, adjustedNewDate, docId) // Pass adjusted date for further updates
                Toast.makeText(this, "Date updated successfully.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error updating date: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateLocationTimes(documentId: String, newDate: Date, docId: String) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users/$userId/Travel_Plan/$docId/LocationDate/$documentId/Location")

        locationsRef.get().addOnSuccessListener { snapshot ->
            val newCalendar = Calendar.getInstance().apply {
                time = newDate
                timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
            }
            Log.d("UpdateLocationTime", "Updating to: ${newCalendar.time}")

            snapshot.documents.forEach { document ->
                document.getTimestamp("LocationTime")?.toDate()?.let { originalTime ->

                    val originalCalendar = Calendar.getInstance().apply {
                        time = originalTime
                        timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                    }
                    val newCalendar = Calendar.getInstance().apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                        time = newDate
                        set(Calendar.HOUR_OF_DAY, originalCalendar.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, originalCalendar.get(Calendar.MINUTE))
                    }
                    Log.d("FinalAdjustment", "Final: ${newCalendar.time}")
                    locationsRef.document(document.id).update("LocationTime", newCalendar.time)
                }
            }
        }
    }

    private fun fetchLocations(travelPlanDocId: String, locationDateDocId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users/$userId/Travel_Plan/$travelPlanDocId/LocationDate/$locationDateDocId/Location").
            orderBy("LocationTime")
            .get()
            .addOnSuccessListener { snapshot ->
                locationItems.clear()
                for (document in snapshot.documents) {
                    val name = document.getString("LocationName") ?: "N/A"
                    val address = document.getString("LocationAddress") ?: "N/A"
                    val status = document.getString("LocationStatus") ?: "N/A"

                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                    }
                    val time = document.getTimestamp("LocationTime")?.toDate()?.let { sdf.format(it) } ?: "N/A"
                    locationItems.add(LocationItem(time, name, status, address))
                }
                locationAdapter.notifyDataSetChanged()
            }
    }

    private fun showDeleteConfirmationDialog(travelPlanDocId: String, locationDateDocId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Date")
            .setMessage("Are you sure you want to delete this date?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteDate(travelPlanDocId, locationDateDocId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteDate(travelPlanDocId: String, locationDateDocId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users/$userId/Travel_Plan/$travelPlanDocId/LocationDate")
            .document(locationDateDocId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Date deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting date: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun fetchAndDisplayPlanName(docId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users/$userId/Travel_Plan")
            .document(docId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val planName = documentSnapshot.getString("PlanName") ?: "Unnamed Plan"
                    binding.textView18.text = planName

                    binding.textView18.setOnClickListener {
                        showEditPlanNameDialog(planName, docId)
                    }
                } else {
                    Log.d("TravelPlanActivity", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w("TravelPlanActivity", "Error fetching document", e)
            }
    }
    private fun showEditPlanNameDialog(currentPlanName: String, docId: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_plan_name, null)
        val editText = dialogView.findViewById<EditText>(R.id.etPlanName)
        editText.setText(currentPlanName)

        AlertDialog.Builder(this)
            .setTitle("Edit Plan Name")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, which ->
                val newPlanName = editText.text.toString().trim()
                if (newPlanName.isNotEmpty()) {
                    updatePlanName(docId, newPlanName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun updatePlanName(docId: String, newPlanName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users/$userId/Travel_Plan")
            .document(docId)
            .update("PlanName", newPlanName)
            .addOnSuccessListener {
                Log.d("TravelPlanActivity", "Plan Name updated successfully")
                binding.textView18.text = newPlanName
                Toast.makeText(this, "Plan Name updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("TravelPlanActivity", "Error updating document", e)
                Toast.makeText(this, "Error updating Plan Name", Toast.LENGTH_SHORT).show()
            }
    }
    private fun fetchLocationDates(docId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users/$userId/Travel_Plan/$docId/LocationDate")
            .orderBy("LocationDate")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    return@addSnapshotListener
                }

                dateList.clear()

                for (document in snapshot.documents) {
                    document.getTimestamp("LocationDate")?.toDate()?.let { date ->

                        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                        }
                        val dayOfMonthFormat = SimpleDateFormat("dd", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                        }
                        val dayOfWeek = dayOfWeekFormat.format(date)
                        val dayOfMonth = dayOfMonthFormat.format(date)

                        dateList.add(DateAdapter.DateItem(dayOfWeek, dayOfMonth, false, date,document.id))
                    }
                }
                dateAdapter.notifyDataSetChanged()
            }
    }
    class HorizontalSpaceItemDecoration(private val spaceWidth: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.right = spaceWidth
        }
    }
}