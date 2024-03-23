package com.example.travel_itinerary_planner.travel

import LocationAdapter
import LocationItem
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_itinerary_planner.R

import com.example.travel_itinerary_planner.databinding.PlanDetailBinding

class TravelPlanActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var binding: PlanDetailBinding
    private lateinit var adapter: DateAdapter
    private lateinit var locationAdapter: LocationAdapter
    private var locationItems: ArrayList<LocationItem> = ArrayList()

    private var dateList = mutableListOf(
        DateAdapter.DateItem("Sat", "16", true),
        DateAdapter.DateItem("Fri", "22", false),
        DateAdapter.DateItem("Sat", "23", false)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listView = findViewById(R.id.location_list)
        locationItems.add(LocationItem("2:00 PM", "Pavilion Kuala Lumpur", "(Executed)", "Pavilion Kuala Lumpur, Jalan Bukit Bintang"))
        locationAdapter = LocationAdapter(this, locationItems)
        listView.adapter = locationAdapter


        adapter = DateAdapter(dateList, this@TravelPlanActivity)
        with(binding.recyclerViewDates) {
            layoutManager = LinearLayoutManager(this@TravelPlanActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = this@TravelPlanActivity.adapter
            addItemDecoration(HorizontalSpaceItemDecoration(8))
        }
    }

    class HorizontalSpaceItemDecoration(private val spaceWidth: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.right = spaceWidth
        }
    }
}