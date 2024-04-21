import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.travel_itinerary_planner.R // Make sure to import your R file correctly

data class LocationItem(
    val documentId: String,
    val time: String,
    val title: String,
    val status: String,
    val address: String
)


class LocationAdapter(
    context: Context,
    items: List<LocationItem>,
    private val onLocationItemClickListener: OnLocationItemClickListener,
    private val travelPlanID: String?,
    private val locationDateID: String?
) : ArrayAdapter<LocationItem>(context, 0, items){
    interface OnLocationItemClickListener {
        fun onLocationItemClick(address: String,documentId: String, travelPlanID: String?, locationDateID: String?)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position) ?: return LayoutInflater.from(context).inflate(R.layout.item_travel_location, parent, false)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_travel_location, parent, false)
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.item_travel_location, parent, false)
        }

        listItemView?.findViewById<TextView>(R.id.textView21)?.text = item.time
        listItemView?.findViewById<TextView>(R.id.textView22)?.text = item.title
        listItemView?.findViewById<TextView>(R.id.textView23)?.text = item.status
        listItemView?.findViewById<TextView>(R.id.location_address)?.text = item.address

        listItemView?.findViewById<ImageButton>(R.id.imageButton9)?.setOnClickListener {
            onLocationItemClickListener.onLocationItemClick(item.address,item.documentId, travelPlanID, locationDateID)
        }
        return listItemView!!
    }
}