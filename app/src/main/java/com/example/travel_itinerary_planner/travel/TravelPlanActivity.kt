package com.example.travel_itinerary_planner.travel

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.travel_itinerary_planner.databinding.PlanDetailBinding

class TravelPlanActivity : AppCompatActivity() {
    private lateinit var binding: PlanDetailBinding
    private lateinit var adapter: DateAdapter

    private var dateList = mutableListOf(
        DateAdapter.DateItem("Sat", "16", true),
        DateAdapter.DateItem("Fri", "22", false),
        DateAdapter.DateItem("Sat", "23", false)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

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