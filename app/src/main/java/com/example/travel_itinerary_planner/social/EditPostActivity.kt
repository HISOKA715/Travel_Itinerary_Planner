package com.example.travel_itinerary_planner.social

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ActivityEditPostBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class EditPostActivity : LoggedInActivity() {

    private lateinit var binding: ActivityEditPostBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var addressListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        val postId = intent.getStringExtra("postId")

        if (postId != null) {
            firestore.collection("SocialMedia")
                .document(postId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {

                        val socialMediaPost = documentSnapshot.toObject(SocialMediaPost::class.java)

                        socialMediaPost?.let { post ->
                            Glide.with(this)
                                .load(post.SocialImage)
                                .override(300, 300)
                                .error(R.drawable.travel_main)
                                .centerCrop()
                                .into(binding.imageEditPost)

                            binding.multilineEditDescribe.setText(post.SocialContent)
                            binding.locationEdit.setText(post.SocialLocation)
                            binding.sharingOptionsEdit.setText(post.SocialSharingOptions)


                            binding.toolbarEditPost.setNavigationOnClickListener {
                                val intent = Intent(this, BottomNavigationActivity::class.java)
                                intent.putExtra("returnToPostDetailsFragment", true)
                                startActivity(intent)
                            }
                            binding.locationEdit.setOnClickListener {
                                showAddressSelectionDialog()
                            }
                            binding.sharingOptionsEdit.setOnClickListener {
                                toggleSharingOption()
                            }
                            binding.buttonEditPost.setOnClickListener {

                                val newDescription = binding.multilineEditDescribe.text.toString()
                                val newLocation = binding.locationEdit.text.toString()
                                val newSharingOptions = binding.sharingOptionsEdit.text.toString()

                                val postRef = firestore.collection("SocialMedia").document(postId!!)
                                postRef.update(
                                    mapOf(
                                        "SocialContent" to newDescription,
                                        "SocialLocation" to newLocation,
                                        "SocialSharingOptions" to newSharingOptions
                                    )
                                )
                                    .addOnSuccessListener {
                                        val intent = Intent(this, BottomNavigationActivity::class.java)
                                        intent.putExtra("navigateToPostDetailsFragment", true)
                                        startActivity(intent)
                                        Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show()

                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Failed to update post: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }


                            }




                        }
                    } else {

                        Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to retrieve post: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Post ID not provided", Toast.LENGTH_SHORT).show()
        }
    }



private fun showAddressSelectionDialog() {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_address_select, null)
    addressListView = dialogView.findViewById(R.id.addressListView)
    val addressEditText: EditText = dialogView.findViewById(R.id.addressEditText)
    val searchButton: ImageView = dialogView.findViewById(R.id.searchButton)

    searchButton.setOnClickListener {
        val query = addressEditText.text.toString()
        if (query.isNotBlank()) {
            SearchLocationTask().execute(query)
        } else {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show()
        }
    }

    val alertDialogBuilder = AlertDialog.Builder(this)
    val dialog = alertDialogBuilder.setView(dialogView).create()

    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
        dialog.dismiss()
    }

    dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Clear") { _, _ ->
        binding.locationEdit.setText("")
        binding.locationEdit.compoundDrawables.forEach { drawable ->
            drawable?.setTint(resources.getColor(R.color.dark_blue))

        }
    }
    dialog.show()

    addressListView.setOnItemClickListener { _, _, position, _ ->
        val selectedAddress = adapter.getItem(position)
        binding.locationEdit.setText(selectedAddress)
        dialog.dismiss()
    }


}


private inner class SearchLocationTask : AsyncTask<String, Void, ArrayList<String>>() {
    override fun doInBackground(vararg params: String?): ArrayList<String> {
        val query = params[0]
        val apiUrl = "https://nominatim.openstreetmap.org/search?q=$query&format=json"

        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        val response = StringBuilder()

        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
        } finally {
            connection.disconnect()
        }

        val addresses = ArrayList<String>()
        if (response.isNotEmpty()) {
            try {
                val jsonArray = JSONArray(response.toString())
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val address = jsonObject.getString("display_name")
                    addresses.add(address)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return addresses
    }

    override fun onPostExecute(result: ArrayList<String>?) {
        super.onPostExecute(result)
        result?.let {
            adapter = ArrayAdapter(this@EditPostActivity, android.R.layout.simple_list_item_1, it)
            addressListView.adapter = adapter


            binding.locationEdit.setTextColor(resources.getColor(R.color.blue))

            binding.locationEdit.compoundDrawables.forEach { drawable ->
                drawable?.setTint(resources.getColor(R.color.blue))
            }
        }
    }
}

private fun toggleSharingOption() {
    val currentText = binding.sharingOptionsEdit.text.toString()

    val options = arrayOf("Public", "Private")
    val currentIndex = if (currentText == "Public") 0 else 1

    val builder = AlertDialog.Builder(this)
    builder.setTitle("Select Sharing Option")
    builder.setSingleChoiceItems(options, currentIndex) { dialog, which ->
        val newText = options[which]
        binding.sharingOptionsEdit.setText(newText)
        dialog.dismiss()
    }
    builder.show()
}

}
