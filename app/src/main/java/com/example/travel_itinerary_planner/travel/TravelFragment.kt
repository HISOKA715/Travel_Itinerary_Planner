package com.example.travel_itinerary_planner.travel

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel_itinerary_planner.R

class TravelFragment : Fragment() {

    companion object {
        fun newInstance() = TravelFragment()
    }

    private lateinit var viewModel: TravelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_travel, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TravelViewModel::class.java)
        // TODO: Use the ViewModel
    }

}