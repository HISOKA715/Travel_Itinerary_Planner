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
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddNewPostActivity : LoggedInActivity() {
    private lateinit var binding: ActivityAddNewPostBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedImageUri: Uri
    private lateinit var addressListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
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

        generateSocialId { socialId ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            val user = FirebaseAuth.getInstance().currentUser
            val uid = user?.uid


            val filename = UUID.randomUUID().toString()

            val imageRef = storage.reference.child("SocialMedia").child(uid!!).child("$filename.jpg")
            imageRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val post = hashMapOf(
                            "SocialID" to socialId,
                            "UserID" to uid,
                            "SocialImage" to uri.toString(),
                            "SocialContent" to description,
                            "SocialDate" to currentDate,
                            "SocialLocation" to location,
                            "SocialSharingOptions" to sharingOptions,
                            "SocialCommentCounts" to null
                        )

                        firestore.collection("SocialMedia").document(socialId)
                            .set(post)
                            .addOnSuccessListener {
                                val intent = Intent(this, BottomNavigationActivity::class.java)
                                intent.putExtra("navigateToProfileFragment", true)
                                startActivity(intent)
                                Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to add post", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun generateSocialId(callback: (String) -> Unit) {
        firestore.collection("SocialMedia")
            .orderBy("SocialID", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val newSocialId = if (!querySnapshot.isEmpty) {
                    val latestSocialId = querySnapshot.documents[0].getString("SocialID")
                    val latestSocialNumber = latestSocialId?.substring(1)?.toInt() ?: 0
                    val newSocialNumber = latestSocialNumber + 1
                    "S${String.format("%09d", newSocialNumber)}"
                } else {
                    "S000000001"
                }
                callback(newSocialId)
            }
            .addOnFailureListener { exception ->

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