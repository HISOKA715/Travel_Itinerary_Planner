import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.travel_itinerary_planner.R // Make sure to import your R file correctly

data class LocationItem(
    val time: String,
    val title: String,
    val status: String,
    val address: String
)
class LocationAdapter(context: Context, items: List<LocationItem>, private val onItemClick: (LocationItem) -> Unit) :
    ArrayAdapter<LocationItem>(context, 0, items) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position) ?: return LayoutInflater.from(context).inflate(R.layout.item_travel_location, parent, false)

        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.item_travel_location, parent, false)
        }

        listItemView?.findViewById<TextView>(R.id.textView21)?.text = item.time
        listItemView?.findViewById<TextView>(R.id.textView22)?.text = item.title
        listItemView?.findViewById<TextView>(R.id.textView23)?.text = item.status
        listItemView?.findViewById<TextView>(R.id.location_address)?.text = item.address

        listItemView?.findViewById<ImageButton>(R.id.imageButton9)?.setOnClickListener {
            onItemClick(item)
        }

        return listItemView!!
    }
}