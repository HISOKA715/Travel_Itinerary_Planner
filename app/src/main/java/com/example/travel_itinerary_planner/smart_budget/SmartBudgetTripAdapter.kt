package com.example.travel_itinerary_planner.smart_budget

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.R
import com.google.firebase.firestore.FirebaseFirestore

class SmartBudgetTripAdapter (private val itemClickListener: OnItemClickListener): RecyclerView.Adapter<SmartBudgetTripAdapter.SmartBudgetTripViewHolder>() {

    val smartBudgetList = mutableListOf<SmartBudget>()

    interface OnItemClickListener {
        fun onItemClick(budgetId: String)
    }
    fun setData(data: List<SmartBudget>) {
        smartBudgetList.clear()
        smartBudgetList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmartBudgetTripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_smart_budget_trip, parent, false)
        return SmartBudgetTripViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmartBudgetTripViewHolder, position: Int) {
        val smartBudget = smartBudgetList[position]
        holder.bind(smartBudget, this, itemClickListener)
    }

    override fun getItemCount(): Int {
        return smartBudgetList.size
    }
    fun getItem(position: Int): SmartBudget {
        return smartBudgetList[smartBudgetList.size - 1 - position]
    }

    class SmartBudgetTripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var textViewTripName: TextView = itemView.findViewById(R.id.textViewTripName)
        private var textViewTripDates: TextView = itemView.findViewById(R.id.textViewTripDates)
        private var textViewCurrency: TextView = itemView.findViewById(R.id.textViewCurrency)
        private var textViewBudget: TextView = itemView.findViewById(R.id.textViewBudget)
        private var imageViewSmartMoreOptions: ImageView = itemView.findViewById(R.id.imageViewSmartMoreOptions)

        fun bind(smartBudget: SmartBudget, adapter: SmartBudgetTripAdapter, itemClickListener: OnItemClickListener) {
            textViewTripName.text = smartBudget.TripName
            textViewTripDates.text = "${smartBudget.TripStartDate} - ${smartBudget.TripEndDate}"
            textViewCurrency.text = smartBudget.BudgetCurrency
            textViewBudget.text = smartBudget.Budget
            imageViewSmartMoreOptions.setOnClickListener {
                showMoreOptionsDialog(smartBudget, adapter)
            }
            itemView.setOnClickListener {
                val budgetId = smartBudget.BudgetID
                itemClickListener.onItemClick(budgetId)
            }
        }

        private fun showMoreOptionsDialog(smartBudget: SmartBudget, adapter: SmartBudgetTripAdapter) {
            AlertDialog.Builder(itemView.context)
                .setTitle("More Options")
                .setItems(arrayOf("Edit", "Delete")) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(itemView.context, SmartEditTripActivity::class.java).apply {
                                putExtra("budgetId", smartBudget.BudgetID)
                            }
                            itemView.context.startActivity(intent)
                        }
                        1 -> {
                            showDeleteConfirmationDialog(smartBudget, adapter)
                        }
                    }
                }
                .show()
        }

        private fun showDeleteConfirmationDialog(smartBudget: SmartBudget, adapter: SmartBudgetTripAdapter) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteBudget(smartBudget, adapter)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun deleteBudget(smartBudget: SmartBudget, adapter: SmartBudgetTripAdapter) {
            val firestore = FirebaseFirestore.getInstance()
            val budgetId = smartBudget.BudgetID

            val budgetRef = firestore.collection("SmartBudget")
            val budgetDocRef = budgetRef.document(budgetId)
            budgetDocRef.delete()
                .addOnSuccessListener {

                    val detailsRef = firestore.collection("SmartBudgetDetails")
                        .whereEqualTo("BudgetID", budgetId)
                    detailsRef.get()
                        .addOnSuccessListener { snapshot ->
                            val batch = firestore.batch()
                            for (doc in snapshot.documents) {
                                val detailDocRef = doc.reference
                                batch.delete(detailDocRef)
                            }
                            batch.commit()
                                .addOnSuccessListener {

                                    adapter.smartBudgetList.remove(smartBudget)
                                    adapter.notifyDataSetChanged()
                                    Toast.makeText(
                                        itemView.context,
                                        "Budget and associated details deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        itemView.context,
                                        "Failed to delete SmartBudgetDetails: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                itemView.context,
                                "Failed to fetch SmartBudgetDetails: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        itemView.context,
                        "Failed to delete budget: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        }

    }
