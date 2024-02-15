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

class AddNewPostActivity : LoggedInActivity() {
    private lateinit var binding: ActivityAddNewPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.multilineDescribe.requestFocus()
        binding.toolbarAddNewPost.setNavigationOnClickListener {
            showConfirmationDialog()
        }

        val selectedImageUriString = intent.getStringExtra("selected_image_uri")
        selectedImageUriString?.let {
            val selectedImageUri = Uri.parse(selectedImageUriString)
            binding.imagePost.setImageURI(selectedImageUri)
        }
        binding.buttonAddPost.setOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("navigateToProfileFragment", true)
            startActivity(intent)
            Toast.makeText(this, "Create New Post Successfully", Toast.LENGTH_SHORT).show()
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