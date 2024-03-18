// MyGridAdapter.kt
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.travel_itinerary_planner.R

data class DataModel(val primaryText: String)
class MyGridAdapter(context: Context, private val resource: Int, private val items: List<DataModel>)
    : ArrayAdapter<DataModel>(context, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val primaryTextView = itemView.findViewById<TextView>(R.id.textViewPrimary)
        val item = getItem(position)
        primaryTextView.text = item?.primaryText
        return itemView
    }

}