package com.example.travel_itinerary_planner

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.travel_itinerary_planner.databinding.ActivityAddNewPostBinding
import com.example.travel_itinerary_planner.databinding.ActivityBookmarksBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddNewPostActivity : LoggedInActivity() {
    private lateinit var binding: ActivityAddNewPostBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedImageUri: Uri
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