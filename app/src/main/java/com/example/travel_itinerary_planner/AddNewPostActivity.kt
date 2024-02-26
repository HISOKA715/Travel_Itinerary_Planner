package com.example.travel_itinerary_planner

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.travel_itinerary_planner.databinding.ActivityAddNewPostBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.InputStreamReader
import androidx.core.content.ContextCompat

class AddNewPostActivity : LoggedInActivity() {
    private lateinit var binding: ActivityAddNewPostBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedImageUri: Uri
    private lateinit var addressListView: ListView
    private lateinit var adapter: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firestore = FirebaseFirestore.getInstance()
        binding.multilineDescribe.requestFocus()
        binding.toolbarAddNewPost.setNavigationOnClickListener {
            showConfirmationDialog()
        }

        val selectedImageUriString = intent.getStringExtra("selected_image_uri")
        selectedImageUriString?.let {
            selectedImageUri = Uri.parse(selectedImageUriString)
            binding.imagePost.setImageURI(selectedImageUri)
        }
        binding.buttonAddPost.setOnClickListener {
            addPostToFirestore()
        }
        binding.locationEditText.setOnClickListener {
            showAddressSelectionDialog()



        }




        binding.sharingOptionsEditText.setOnClickListener {
            toggleSharingOption()
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
            binding.locationEditText.setText("")
            binding.locationEditText.compoundDrawables.forEach { drawable ->
                drawable?.setTint(resources.getColor(R.color.dark_blue))

            }
        }
            dialog.show()

            addressListView.setOnItemClickListener { _, _, position, _ ->
                val selectedAddress = adapter.getItem(position)
                binding.locationEditText.setText(selectedAddress)
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
                adapter = ArrayAdapter(this@AddNewPostActivity, android.R.layout.simple_list_item_1, it)
                addressListView.adapter = adapter


                binding.locationEditText.setTextColor(resources.getColor(R.color.blue))

                binding.locationEditText.compoundDrawables.forEach { drawable ->
                    drawable?.setTint(resources.getColor(R.color.blue))
                }
            }
        }
    }

    private fun toggleSharingOption() {
        val currentText = binding.sharingOptionsEditText.text.toString()

        val options = arrayOf("Public", "Private")
        val currentIndex = if (currentText == "Public") 0 else 1

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Sharing Option")
        builder.setSingleChoiceItems(options, currentIndex) { dialog, which ->
            val newText = options[which]
            binding.sharingOptionsEditText.setText(newText)
            dialog.dismiss()
        }
        builder.show()
    }



    private fun addPostToFirestore() {

        val description = binding.multilineDescribe.text.toString().trim()
        val location = binding.locationEditText.text.toString().trim()
        val sharingOptions = binding.sharingOptionsEditText.text.toString().trim()



        firestore.collection("SocialMedia")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val socialId = String.format("S%09d", querySnapshot.size() + 1)



                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDate = dateFormat.format(Date())
                val user = FirebaseAuth.getInstance().currentUser
                val uid = user?.uid
                val post = hashMapOf(
                    "SocialID" to socialId,
                    "UserID" to uid,
                    "SocialImage" to selectedImageUri,
                    "SocialContent" to description,
                    "SocialDate" to currentDate,
                    "SocialLocation" to location,
                    "SocialSharingOptions" to sharingOptions,
                    "SocialCommentCounts" to null
                )

                firestore.collection("SocialMedia").document(socialId)
                    .set(post)
                    .addOnSuccessListener { documentReference ->

                        val intent = Intent(this, BottomNavigationActivity::class.java)
                        intent.putExtra("navigateToProfileFragment", true)
                        startActivity(intent)
                        Toast.makeText(this,"Post added successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->

                        Toast.makeText(this,"Failed to add post", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->

                Toast.makeText(this,"Failed to get number of posts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Discard Post")
        alertDialogBuilder.setMessage("Are you sure you want to discard this post?")
        alertDialogBuilder.setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
            navigateBack()
        }
        alertDialogBuilder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun navigateBack() {
        val intent = Intent(this, BottomNavigationActivity::class.java)
        intent.putExtra("returnToProfileFragment", true)
        startActivity(intent)
    }

}