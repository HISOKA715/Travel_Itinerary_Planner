package com.example.travel_itinerary_planner.travel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.travel_itinerary_planner.R
data class TravelData(
    val docId: String,
    val date_start: String,
    val month_start: String,
    val date_end: String,
    val month_end: String,
    val title: String,
    val region: String,
    var year: String

)

data class TravelItem(
    val type: Int,
    val data: TravelData? = null,
    val header: String? = null
)


const val TYPE_HEADER = 0
const val TYPE_ITEM = 1


class TravelListAdapter(
    context: Context,
    items: List<TravelItem>,
    private val onItemClick: (TravelData) -> Unit,
    private val onItemLongClick: (TravelData) -> Unit
) : ArrayAdapter<TravelItem>(context, 0, items) {

    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.type ?: TYPE_ITEM
    }

    override fun getViewTypeCount(): Int {
        return 2 // because we have two types: header and item
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemType = getItemViewType(position)
        val itemView: View
        val holder: ViewHolder



        if (convertView == null) {
            if (itemType == TYPE_HEADER) {
                itemView = LayoutInflater.from(context).inflate(R.layout.header_travelyear, parent, false)
                holder = ViewHolder(itemView, itemType)
            } else {
                itemView = LayoutInflater.from(context).inflate(R.layout.travel_list_item, parent, false)
                holder = ViewHolder(itemView, itemType)
                itemView.setOnClickListener {
                    getItem(position)?.data?.let(onItemClick)
                }
            }
            itemView.tag = holder
        } else {
            itemView = convertView
            holder = itemView.tag as ViewHolder
            if (itemType == TYPE_ITEM) {
                itemView.setOnClickListener {
                    getItem(position)?.data?.let(onItemClick)
                }
            }
        }


        val item = getItem(position)
        itemView.setOnLongClickListener {
            item?.data?.let { data ->
                onItemLongClick(data)
            }
            true
        }
        if (itemType == TYPE_HEADER) {
            holder.headerTextView?.text = item?.header
        } else if (itemType == TYPE_ITEM) {
            item?.data?.let { data ->
                holder.dateTextView?.text = data.date_start
                holder.monthTextView?.text = data.month_start
                holder.titleTextView?.text = data.title
                holder.destinationTextView?.text = data.region
                holder.dateTextView1?.text = data.date_end
                holder.monthTextView1?.text = data.month_end
            }
        }

        return itemView
    }




    private class ViewHolder(view: View, type: Int) {
        var headerTextView: TextView? = null
        var dateTextView: TextView? = null
        var monthTextView: TextView? = null
        var dateTextView1: TextView? = null
        var monthTextView1: TextView? = null
        var titleTextView: TextView? = null
        var destinationTextView: TextView? = null

        init {
            if (type == TYPE_HEADER) {
                headerTextView = view.findViewById(R.id.headerText)
            } else {
                dateTextView = view.findViewById(R.id.dateTextView)
                monthTextView = view.findViewById(R.id.monthTextView)
                dateTextView1 = view.findViewById(R.id.dateTextView1)
                monthTextView1 = view.findViewById(R.id.monthTextView1)
                titleTextView = view.findViewById(R.id.titleTextView)
                destinationTextView = view.findViewById(R.id.titleTextView1)
            }
        }
    }
}