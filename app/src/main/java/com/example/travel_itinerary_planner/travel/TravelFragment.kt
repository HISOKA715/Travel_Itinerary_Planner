package com.example.travel_itinerary_planner.travel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.databinding.FragmentTravelBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TravelFragment : LoggedInFragment() {

    private var _binding: FragmentTravelBinding? = null
    private val binding get() = _binding!!
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTravelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageButton4.setOnClickListener {
            showNewPlanDialog()
        }
        fetchTravelPlans()
    }


    private fun showNewPlanDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_new_plan, null)
        val editTextPlanName = dialogView.findViewById<EditText>(R.id.editTextPlanName)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val planName = editTextPlanName.text.toString().trim()
                if (planName.isEmpty()) {
                    Toast.makeText(context, "Plan name cannot be empty", Toast.LENGTH_SHORT).show()
                } else if (planName.length > 30) {
                    Toast.makeText(context, "Plan name cannot exceed 30 characters", Toast.LENGTH_SHORT).show()
                } else {
                    createNewTravelPlan(planName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createNewTravelPlan(planName: String) {
        if (!isAdded) return
        val userId = auth.currentUser?.uid ?: return
        val newPlan = hashMapOf(
            "PlanName" to planName,
            "TravelRegion" to "Malaysia",
            "CreateDate" to com.google.firebase.Timestamp.now(),
            "StartDate" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("users/$userId/Travel_Plan")
            .add(newPlan)
            .addOnSuccessListener {
                Toast.makeText(context, "New travel plan created successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error creating travel plan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun fetchTravelPlans() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users/$userId/Travel_Plan").orderBy("StartDate", Query.Direction.DESCENDING).get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener
            val allTravelData = mutableListOf<TravelData>()

            snapshot.documents.forEach { document ->
                val planName = document.getString("PlanName") ?: "Unnamed Plan"
                val docId = document.id

                document.reference.collection("LocationDate")
                    .orderBy("LocationDate", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener { locationSnapshot ->
                        if (locationSnapshot.documents.isNotEmpty()) {
                            val dates = locationSnapshot.documents.mapNotNull { it.getTimestamp("LocationDate")?.toDate() }
                            val startDate = formatDate(dates.first())
                            val endDate = formatDate(dates.last())
                            val startMonth = formatMonth(dates.first())
                            val endMonth = formatMonth(dates.last())
                            val year = formatyear(dates.first())
                            allTravelData.add(TravelData(docId, startDate, startMonth, endDate, endMonth, planName, "Malaysia", year))
                        } else {
                            document.getTimestamp("StartDate")?.toDate()?.let { startDate ->
                                val formattedStartDate = formatDate(startDate)
                                val formattedStartMonth = formatMonth(startDate)
                                val formattedYear = formatyear(startDate)
                                allTravelData.add(TravelData(docId, formattedStartDate, formattedStartMonth, formattedStartDate, formattedStartMonth, planName, "Malaysia", formattedYear))
                            }
                        }
                        displayTravelPlans(organizeDataByYear(allTravelData))
                    }
            }
        }.addOnFailureListener {
        }
    }

    private fun formatDate(date: Date?): String {
        val formatter = SimpleDateFormat("dd", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        return date?.let { formatter.format(it) } ?: ""
    }
    private fun formatMonth(date: Date?): String {
        val formatter = SimpleDateFormat("MMM", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        return date?.let { formatter.format(it) } ?: ""
    }

    private fun formatyear(date: Date?): String {
        val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        return date?.let { formatter.format(it) } ?: ""
    }
    private fun displayTravelPlans(sortedDataWithHeaders: List<TravelItem>) {
        val adapter = TravelListAdapter(
            requireContext(),
            sortedDataWithHeaders,
            onItemClick = { travelData ->

                val intent = Intent(context, TravelPlanActivity::class.java).apply {
                    putExtra("docId", travelData.docId)
                }
                startActivity(intent)
            },
            onItemLongClick = { travelData ->
                // Handle long click: Show delete confirmation dialog
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Delete Plan")
                    setMessage("Are you sure you want to delete this plan?")
                    setPositiveButton("Delete") { _, _ ->
                        deleteTravelPlan(travelData.docId)
                    }
                    setNegativeButton("Cancel", null)
                    show()
                }
            }
        )
        binding.myTravelPlan.adapter = adapter
    }
    private fun deleteTravelPlan(docId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users/$userId/Travel_Plan").document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Travel plan deleted successfully.", Toast.LENGTH_SHORT).show()
                fetchTravelPlans()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting travel plan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun organizeDataByYear(travelPlans: List<TravelData>): List<TravelItem> {
        val sortedDataWithHeaders = mutableListOf<TravelItem>()
        var lastYear = ""
        travelPlans.sortedBy { it.year }.forEach { plan ->
            if (plan.year != lastYear) {
                sortedDataWithHeaders.add(TravelItem(TYPE_HEADER, header = plan.year))
                lastYear = plan.year
            }
            sortedDataWithHeaders.add(TravelItem(TYPE_ITEM, data = plan))
        }
        return sortedDataWithHeaders
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }
}