package com.example.travel_itinerary_planner.smart_budget

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.PasswordSecurityActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentSmartBudgetBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SmartBudgetFragment : LoggedInFragment(), SmartBudgetTripAdapter.OnItemClickListener, SmartBudgetDetailsAdapter.OnSmartBudgetDetailsClickListener {

    companion object {
        fun newInstance() = SmartBudgetFragment()
    }

    private var isItemClicked: Boolean = false
    private var isUpdated: Boolean = false
    private lateinit var binding: FragmentSmartBudgetBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: SmartBudgetTripAdapter
    private lateinit var detailsAdapter: SmartBudgetDetailsAdapter
    private var currentBudgetId: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSmartBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        adapter = SmartBudgetTripAdapter(this)
        detailsAdapter = SmartBudgetDetailsAdapter(this)
        binding.recyclerViewSmartTrip.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSmartTrip.adapter = adapter
        binding.recyclerViewSmart.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSmart.adapter = detailsAdapter
        fetchSmartBudgetData()

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        val menu = bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            if (menuItem.itemId == R.id.navigation_home) {
                menuItem.isChecked = true
                break
            }
        }


        binding.toolbarSmart.setNavigationOnClickListener {
            val intent = Intent(requireContext(), BottomNavigationActivity::class.java)
            intent.putExtra("returnToHomeFragment", true)
            startActivity(intent)
        }

        binding.imageButtonAddExpenses.setOnClickListener {
            currentBudgetId?.let { budgetId ->
                val intent = Intent(requireContext(), SmartPickCategoryActivity::class.java).apply {
                    putExtra("budgetId", budgetId)
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(requireContext(), "Budget ID is null or empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.toolbarSmart.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_smart_drawer -> {
                    val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_smart_layout)
                    drawerLayout.openDrawer(GravityCompat.END)
                    true
                }
                else -> false
            }
        }

        binding.toolbarSmartTrip.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_add_trip -> {
                    val intent = Intent(requireContext(), SmartAddTripActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val budgetId = requireActivity().intent.getStringExtra("budgetId")

        if (budgetId!=null){
            currentBudgetId = budgetId
            isUpdated = true
            fetchSmartBudgetDetailsData(budgetId)
        }


    }

    private fun fetchSmartBudgetDetailsData(budgetId: String) {

        firestore.collection("SmartBudgetDetails")
            .whereEqualTo("BudgetID", budgetId)
            .get()
            .addOnSuccessListener { detailsQuerySnapshot ->
                val detailsList = mutableListOf<SmartBudgetDetails>()
                for (document in detailsQuerySnapshot.documents) {
                    val details = document.toObject(SmartBudgetDetails::class.java)
                    details?.let {
                        detailsList.add(it)
                    }
                }
                if (detailsList.isNotEmpty()) {
                    detailsAdapter.setData(detailsList)
                    fetchTotalExpenses(budgetId)
                    handleDetailsExistence(true)
                } else {
                    handleDetailsExistence(false)
                }
            }
            .addOnFailureListener { exception ->
            }
    }
    private fun fetchTotalExpenses(budgetId: String) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val smartBudgetDocRef = firestore.collection("SmartBudget")
        smartBudgetDocRef.whereEqualTo("UserID", userId)
            .whereEqualTo("BudgetID", budgetId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val smartBudget = document.toObject(SmartBudget::class.java)

                    if (smartBudget != null) {
                        val totalExpensesString = smartBudget.TotalExpensesAmount
                        val totalAmount = totalExpensesString?.toDoubleOrNull() ?: 0.0
                        val currency = smartBudget.TotalExpensesAmountCurrency

                        val totalExpensesText = String.format("%s %.2f", currency, totalAmount)
                        binding.totalCurrencyAmount.text = totalExpensesText

                    }
                }
            }
            .addOnFailureListener { exception ->
            }
    }


    private fun fetchSmartBudgetData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        firestore.collection("SmartBudget")
            .whereEqualTo("UserID", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val smartBudgetList = mutableListOf<SmartBudget>()
                for (document in querySnapshot.documents) {
                    val smartBudget = document.toObject(SmartBudget::class.java)
                    smartBudget?.let {
                        smartBudgetList.add(it)
                    }
                }
                smartBudgetList.reverse()
                adapter.setData(smartBudgetList)

                if (smartBudgetList.isNotEmpty() && !isItemClicked && !isUpdated) {
                    val newestTrip = smartBudgetList.first()
                    currentBudgetId = newestTrip.BudgetID
                    fetchSmartBudgetDetailsData(newestTrip.BudgetID)
                } else if (isItemClicked) {
                    isItemClicked = false
                }else if (isUpdated) {
                    isUpdated = false
                } else {
                    handleNoSmartBudget()
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    override fun onItemClick(budgetId: String) {
        currentBudgetId = budgetId
        isItemClicked = true
        fetchSmartBudgetDetailsData(budgetId)
    }
    override fun onSmartBudgetDetailsClick(budgetDetailsID: String,budgetID: String) {

        val intent = Intent(requireContext(), SmartEditExpensesActivity::class.java).apply {
            putExtra("BudgetDetailsID", budgetDetailsID)
            putExtra("budgetId",budgetID)
        }
        startActivity(intent)
    }

    private fun handleDetailsExistence(hasDetails: Boolean) {
        if (hasDetails) {
            binding.recyclerViewSmart.visibility = View.VISIBLE
            binding.imageViewSmart.visibility = View.GONE
            binding.imageViewSmartExpenses.visibility = View.GONE
            binding.imageButtonAddExpenses.visibility = View.VISIBLE
            binding.recyclerViewSmartTrip.visibility = View.VISIBLE
            binding.linearTotalExpenses.visibility =View.VISIBLE
        } else {
            binding.recyclerViewSmartTrip.visibility = View.VISIBLE
            binding.imageViewSmartTrip.visibility = View.GONE
            binding.imageViewSmartExpenses.visibility = View.VISIBLE
            binding.imageButtonAddExpenses.visibility = View.VISIBLE
            binding.recyclerViewSmart.visibility = View.GONE
            binding.linearTotalExpenses.visibility =View.GONE
        }
    }

    private fun handleNoSmartBudget() {
        binding.recyclerViewSmartTrip.visibility = View.GONE
        binding.imageViewSmartTrip.visibility = View.VISIBLE
        binding.imageViewSmart.visibility = View.VISIBLE
        binding.imageViewSmartExpenses.visibility = View.GONE
        binding.imageButtonAddExpenses.visibility = View.GONE
        binding.recyclerViewSmart.visibility = View.GONE
        binding.linearTotalExpenses.visibility =View.GONE
    }
}
