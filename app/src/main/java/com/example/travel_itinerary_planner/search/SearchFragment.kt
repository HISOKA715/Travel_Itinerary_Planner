package com.example.travel_itinerary_planner.search

import DataModel
import MyGridAdapter
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ListView
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentProfileBinding
import com.example.travel_itinerary_planner.databinding.FragmentSearchBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.tourism_attraction.TourismActivity

class SearchFragment : LoggedInFragment() {
    private lateinit var viewModel: SearchViewModel
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var isExpanded = false

    private val collapsedItems = listOf(
        DataModel("Sunway Velocity"),
        DataModel("1 Utama Shopping Center"),
        DataModel("Paradigm Mall"),
        DataModel("Kerana Jaya"),
        DataModel("Mid Valley"),
        DataModel("KLCC")
    )

    private val expandedItems = collapsedItems + listOf(
        DataModel("Pavilion"),
        DataModel("Berjaya Times Square"),
        DataModel("Sunway Pyramid"),
        DataModel("IOI City Mall")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGridView(collapsedItems, binding.myGridView)
        setupGridView(collapsedItems, binding.myGridView2)

        binding.myGridView2.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val intent = Intent(context, TourismActivity::class.java)
            startActivity(intent)
        }

        val data = listOf(
            LocationData("Suria KLCC", "3.0 W"),
            LocationData("Sunway Lagoon", "3.0 W"),
            LocationData("1 Utama Shopping Center", "2.0 W")
            // ... add more items
        )
        val adapter = TopAdapter(requireContext(), data)
        binding.listView1.adapter = adapter

        binding.imageButtonSearch.setOnClickListener {
            Log.d("SearchFragment", "Hello from ImageButtonSearch")
        }
        var switchClickListener = View.OnClickListener {
            Log.d("SearchFragment", "Hello from Switch Button")
        }

        val expandClickListener = View.OnClickListener {
            isExpanded = !isExpanded
            binding.imageButton6.setImageResource(if (isExpanded) R.drawable.baseline_arrow_drop_up_24 else R.drawable.baseline_arrow_drop_down_24)
            if (isExpanded) {
                setupGridView(expandedItems, binding.myGridView) // Adjust for expanded state
                adjustGridViewHeight(binding.myGridView, true) // Make sure to update adjustGridViewHeight method accordingly
            } else {
                setupGridView(collapsedItems, binding.myGridView) // Adjust back for collapsed state
                adjustGridViewHeight(binding.myGridView, false) // Update this method accordingly
            }
        }

        binding.textView7.setOnClickListener(expandClickListener)
        binding.imageButton6.setOnClickListener(expandClickListener)
        binding.textView1.setOnClickListener(switchClickListener)
        binding.imageButton1.setOnClickListener(switchClickListener)
    }


    private fun setupGridView(items: List<DataModel>, gridView: GridView) {
        val adapter = MyGridAdapter(requireContext(), R.layout.grid_item, items)
        gridView.adapter = adapter
    }



    private fun adjustGridViewHeight(gridView: GridView, isExpanded: Boolean) {
        val heightInPixels = if (isExpanded) dpToPx(250) else dpToPx(155)
        val params = gridView.layoutParams
        params.height = heightInPixels
        gridView.layoutParams = params
    }

    private fun dpToPx(dp: Int): Int {

        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}