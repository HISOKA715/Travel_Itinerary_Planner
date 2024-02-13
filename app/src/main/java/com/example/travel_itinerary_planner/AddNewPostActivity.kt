package com.example.travel_itinerary_planner

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.travel_itinerary_planner.databinding.ActivityAddNewPostBinding
import com.example.travel_itinerary_planner.databinding.ActivityBookmarksBinding

class AddNewPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarAddNewPost.setNavigationOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToProfileFragment", true)
            startActivity(intent)
        }

        val selectedImageUriString = intent.getStringExtra("selected_image_uri")
        selectedImageUriString?.let {
            val selectedImageUri = Uri.parse(selectedImageUriString)
            binding.imagePost.setImageURI(selectedImageUri)
        }
    }

}