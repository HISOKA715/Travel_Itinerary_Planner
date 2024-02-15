package com.example.travel_itinerary_planner.social

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment

class SocialFragment : LoggedInFragment() {

    companion object {
        fun newInstance() = SocialFragment()
    }

    private lateinit var viewModel: SocialViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_social, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SocialViewModel::class.java)
        // TODO: Use the ViewModel
    }

}