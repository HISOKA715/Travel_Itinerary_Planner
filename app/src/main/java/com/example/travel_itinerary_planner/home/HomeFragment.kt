package com.example.travel_itinerary_planner.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.databinding.FragmentHomeBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.notification.NotificationActivity
import com.example.travel_itinerary_planner.notification.NotificationDetailActivity
import com.example.travel_itinerary_planner.tourism_attraction.RecommandActivity
import com.example.travel_itinerary_planner.useractivity.UserListActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : LoggedInFragment() {

    private var _binding: FragmentHomeBinding? = null

    private var attractions: MutableList<TourismAttraction> = mutableListOf()
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
        binding.imageButtonSearch2.setOnClickListener {
            val intent = Intent(requireContext(), UserListActivity::class.java)
            startActivity(intent)
        }

        binding.textView29.setOnClickListener {
            val intent = Intent(requireContext(), RecommandActivity::class.java)
            startActivity(intent)
        }

        binding.imageButtonSearch1.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }
        setupRecyclerView()
        fetchTourismAttractions()
    }
    private fun setupRecyclerView() {
        binding.horizontalRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = TourismListAdapter(attractions) { attraction ->
        }
    }
    private fun fetchTourismAttractions() {
        FirebaseFirestore.getInstance().collection("Tourism Attractions")
            .orderBy("clickRate",Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                attractions.clear()
                for (document in result) {
                    val id = document.id
                    val imageUrl = document.getString("TourismImage") ?: ""
                    val name = document.getString("TourismName") ?: ""
                    val location = document.getString("TourismState") ?: ""
                    val attraction = TourismAttraction(id,imageUrl, name, location)
                    attractions.add(attraction)
                }
                binding.horizontalRecyclerView.adapter?.notifyDataSetChanged()
            }.addOnFailureListener { exception ->

            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}