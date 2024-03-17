package com.example.travel_itinerary_planner.smart_budget

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel_itinerary_planner.R

class SmartBudgetFragment : Fragment() {

    companion object {
        fun newInstance() = SmartBudgetFragment()
    }

    private lateinit var viewModel: SmartBudgetViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_smart_budget, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SmartBudgetViewModel::class.java)
        // TODO: Use the ViewModel
    }

}