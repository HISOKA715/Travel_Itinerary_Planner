package com.example.travel_itinerary_planner.travel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.travel_itinerary_planner.R
import com.google.type.DateTime
import java.time.LocalDateTime


data class PlaceDetailsResult(
    val latitude: Double,
    val longitude: Double,
    val category: PlaceCategory,
    val photoReference: String?,
    val name: String,
    val nameToLower : String,
    val address: String,
    val description: String? = null,
    val state: String? = null,
    val clickRate: Int,
    val createDate: LocalDateTime
)

data class Address(val description: String, val placeId: String)

class AddressAdapter(context: Context, private val addresses: List<Address>) : ArrayAdapter<Address>(context, R.layout.list_location, addresses)  {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int = addresses.size

    override fun getItem(position: Int): Address = addresses[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_location, parent, false)
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
        val address = getItem(position)
        addressTextView.text = address?.description
        return view
    }
}

