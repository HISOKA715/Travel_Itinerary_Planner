package com.example.travel_itinerary_planner.travel

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.search.TopAdapter
import com.example.travel_itinerary_planner.databinding.FragmentTravelBinding

class TravelFragment : LoggedInFragment() {
    private lateinit var viewModel: TravelViewModel
    private var _binding: FragmentTravelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTravelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val travelPlans = listOf(
            TravelData("16", "Mar", "22", "Mar", "Testing Malaysia", "Malaysia","2024"),
            TravelData("1", "Apr", "7", "Apr", "Adventure in Japan", "Malaysia","2024"),
            // ... add more items here
        )

        val sortedDataWithHeaders = organizeDataByYear(travelPlans)

        val adapter = TravelListAdapter(requireContext(), sortedDataWithHeaders)
        binding.myTravelPlan.adapter = adapter
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
        _binding = null // Avoid memory leaks
    }
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }

}