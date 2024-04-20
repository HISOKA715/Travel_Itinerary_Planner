package com.example.travel_itinerary_planner.smart_budget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.R

class SmartBudgetDetailsAdapter(private val clickListener: OnSmartBudgetDetailsClickListener) : RecyclerView.Adapter<SmartBudgetDetailsAdapter.SmartBudgetDetailsViewHolder>() {


    val detailsList = mutableListOf<SmartBudgetDetails>()
    fun setData(data: List<SmartBudgetDetails>) {
        detailsList.clear()
        detailsList.addAll(data.sortedByDescending { it.ExpensesDate })
        notifyDataSetChanged()
    }
    interface OnSmartBudgetDetailsClickListener {
        fun onSmartBudgetDetailsClick(budgetDetailsID: String,budgetID:String)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmartBudgetDetailsViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_smart_budget_details, parent, false)
        return SmartBudgetDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmartBudgetDetailsViewHolder, position: Int) {
        val details = detailsList[position]
        holder.bind(details)
        holder.itemView.setOnClickListener {
            clickListener.onSmartBudgetDetailsClick(details.BudgetDetailsID,details.BudgetID)
        }
    }

    override fun getItemCount(): Int {
        return detailsList.size
    }

    class SmartBudgetDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var expensesDate: TextView = itemView.findViewById(R.id.textExpensesDate)
        private var expensesCurrency: TextView = itemView.findViewById(R.id.expensesCurrency)
        private var expensesAmount: TextView = itemView.findViewById(R.id.expensesAmount)
        private var expensesCategory: TextView = itemView.findViewById(R.id.categoryName)
        private var convertedCurrency: TextView = itemView.findViewById(R.id.conversionCurrency)
        private var convertedAmount: TextView = itemView.findViewById(R.id.conversionAmount)
        private var categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        private var linearItemDetails: LinearLayout = itemView.findViewById(R.id.linearItemDetails)
        fun bind(details: SmartBudgetDetails) {
            expensesDate.text = details.ExpensesDate
            expensesCurrency.text = details.ExpensesCurrency
            expensesCategory.text = details.ExpensesCategory
            expensesAmount.text = details.ExpensesAmount
            convertedCurrency.text = details.ConvertedCurrency
            convertedAmount.text = details.ConvertedAmount
            val category = details.ExpensesCategory
            val drawableResId = getDrawableResourceId(category)
            if (drawableResId != -1) {
                val drawable = ContextCompat.getDrawable(itemView.context, drawableResId)
                drawable?.let {
                    categoryIcon.setImageDrawable(drawable)
                    categoryIcon.setColorFilter(
                        getColorFromCategory(category),
                        android.graphics.PorterDuff.Mode.SRC_ATOP
                    )
                }
            }
            val categoryColor = getColorFromCategory(details.ExpensesCategory)
            linearItemDetails.background?.let { background ->
                if (background is LayerDrawable) {
                    val shape = background.findDrawableByLayerId(R.id.rectangle_shape) as? GradientDrawable
                    shape?.setStroke(2.dpToPx(itemView.context), categoryColor)
                }
            }


        }
        fun Int.dpToPx(context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }

        private fun getDrawableResourceId(category: String): Int {
            return when (category) {
                "Transportation" -> R.drawable.baseline_directions_transit_24
                "Restaurants" -> R.drawable.baseline_restaurant_menu_24
                "Accommodation" -> R.drawable.baseline_single_bed_24
                "Groceries" -> R.drawable.baseline_local_grocery_store_24
                "Shopping" -> R.drawable.baseline_shopping_bag_24
                "Activities" -> R.drawable.baseline_kayaking_24
                "Drinks" -> R.drawable.baseline_wine_bar_24
                "Coffee" -> R.drawable.baseline_coffee_24
                "Flights" -> R.drawable.baseline_flight_24
                "Fees & Charges" -> R.drawable.baseline_monetization_on_24
                "Sightseeing" -> R.drawable.baseline_museum_24
                "Entertainment" -> R.drawable.baseline_movie_24
                "Laundry" -> R.drawable.baseline_local_laundry_service_24
                "Exchange Fees" -> R.drawable.baseline_currency_exchange_24
                "Others" -> R.drawable.baseline_other_houses_24
                else -> -1
            }
        }

        private fun getColorFromCategory(category: String): Int {
            return when (category) {
                "Transportation" -> Color.parseColor("#FF5722")
                "Restaurants" -> Color.parseColor("#FF9800")
                "Accommodation" -> Color.parseColor("#FFC107")
                "Groceries" -> Color.parseColor("#FFEB3B")
                "Shopping" -> Color.parseColor("#CDDC39")
                "Activities" -> Color.parseColor("#4CAF50")
                "Drinks" -> Color.parseColor("#009688")
                "Coffee" -> Color.parseColor("#00BCD4")
                "Flights" -> Color.parseColor("#03A9F4")
                "Fees & Charges" -> Color.parseColor("#2196F3")
                "Sightseeing" -> Color.parseColor("#3F51B5")
                "Entertainment" -> Color.parseColor("#673AB7")
                "Laundry" -> Color.parseColor("#9C27B0")
                "Exchange Fees" -> Color.parseColor("#E91E63")
                "Others" -> Color.parseColor("#F44336")
                else -> Color.parseColor("#000000")
            }
        }

    }
}
