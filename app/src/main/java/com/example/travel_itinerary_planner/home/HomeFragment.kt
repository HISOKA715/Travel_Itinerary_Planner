package com.example.travel_itinerary_planner.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.databinding.FragmentHomeBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment

class HomeFragment : LoggedInFragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonSmart.setOnClickListener {
            val intent = Intent(requireContext(), BottomNavigationActivity::class.java)
            intent.putExtra("navigateToSmartBudgetFragment", true)
            startActivity(intent)


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}