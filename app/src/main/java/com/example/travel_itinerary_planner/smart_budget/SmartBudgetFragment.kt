package com.example.travel_itinerary_planner.smart_budget

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentSmartBudgetBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class SmartBudgetFragment : LoggedInFragment() {

    companion object {
        fun newInstance() = SmartBudgetFragment()
    }

    private lateinit var viewModel: SmartBudgetViewModel
    private lateinit var binding: FragmentSmartBudgetBinding
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

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SmartBudgetViewModel::class.java)
        // TODO: Use the ViewModel
    }

}