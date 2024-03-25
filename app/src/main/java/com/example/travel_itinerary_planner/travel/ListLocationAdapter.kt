package com.example.travel_itinerary_planner.travel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.travel_itinerary_planner.R


data class Address(val text: String)

class ListLocationAdapter(private val context: Context, private val addresses: List<Address>) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = addresses.size

    override fun getItem(position: Int): Address = addresses[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.list_location, parent, false)
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
        val address: Address = getItem(position)
        addressTextView.text = address.text
        return view
    }
}

