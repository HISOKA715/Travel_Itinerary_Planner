package com.example.travel_itinerary_planner.smart_budget

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ActivitySmartPickCategoryBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity


class SmartPickCategoryActivity : LoggedInActivity() {
    private lateinit var binding: ActivitySmartPickCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartPickCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarCategory.setNavigationOnClickListener {
            val budgetId = intent.getStringExtra("budgetId")
            val intent = Intent(this, BottomNavigationActivity::class.java).apply {
                putExtra("returnToSmartBudgetFragment", true)
                putExtra("budgetId", budgetId)
            }
            startActivity(intent)
        }

        binding.btnTransportation.setOnClickListener { openSmartExpensesActivity("#FF5722", R.drawable.baseline_directions_transit_24, "Transportation") }
        binding.btnRestaurants.setOnClickListener { openSmartExpensesActivity("#FF9800", R.drawable.baseline_restaurant_menu_24, "Restaurants") }
        binding.btnAccommodation.setOnClickListener { openSmartExpensesActivity("#FFC107", R.drawable.baseline_single_bed_24, "Accommodation") }
        binding.btnGroceries.setOnClickListener { openSmartExpensesActivity("#FFEB3B", R.drawable.baseline_local_grocery_store_24, "Groceries") }
        binding.btnShopping.setOnClickListener { openSmartExpensesActivity("#CDDC39", R.drawable.baseline_shopping_bag_24, "Shopping") }
        binding.btnActivities.setOnClickListener { openSmartExpensesActivity("#4CAF50", R.drawable.baseline_kayaking_24, "Activities") }
        binding.btnDrinks.setOnClickListener { openSmartExpensesActivity("#009688", R.drawable.baseline_wine_bar_24, "Drinks") }
        binding.btnCoffee.setOnClickListener { openSmartExpensesActivity("#00BCD4", R.drawable.baseline_coffee_24, "Coffee") }
        binding.btnFlights.setOnClickListener { openSmartExpensesActivity("#03A9F4", R.drawable.baseline_flight_24, "Flights") }
        binding.btnFeesAndCharges.setOnClickListener { openSmartExpensesActivity("#2196F3", R.drawable.baseline_monetization_on_24, "Fees & Charges") }
        binding.btnSightseeing.setOnClickListener { openSmartExpensesActivity("#3F51B5", R.drawable.baseline_museum_24, "Sightseeing") }
        binding.btnEntertainment.setOnClickListener { openSmartExpensesActivity("#673AB7", R.drawable.baseline_movie_24, "Entertainment") }
        binding.btnLaundry.setOnClickListener { openSmartExpensesActivity("#9C27B0", R.drawable.baseline_local_laundry_service_24, "Laundry") }
        binding.btnExchangeFees.setOnClickListener { openSmartExpensesActivity("#E91E63", R.drawable.baseline_currency_exchange_24, "Exchange Fees") }
        binding.btnOthers.setOnClickListener { openSmartExpensesActivity("#F44336", R.drawable.baseline_other_houses_24, "Others") }



    }

    private fun openSmartExpensesActivity(drawableTint: String, drawableTop: Int, buttonText: String) {
        val budgetId = intent.getStringExtra("budgetId")
        if (!budgetId.isNullOrEmpty()) {
            val intent = Intent(this, SmartExpensesActivity::class.java)
            intent.putExtra("categoryTint", drawableTint)
            intent.putExtra("categoryIcon", drawableTop)
            intent.putExtra("categoryText", buttonText)
            intent.putExtra("budgetId", budgetId)

            startActivity(intent)
        } else {

            Toast.makeText(this, "Budget ID is null or empty", Toast.LENGTH_SHORT).show()
        }
    }
}
