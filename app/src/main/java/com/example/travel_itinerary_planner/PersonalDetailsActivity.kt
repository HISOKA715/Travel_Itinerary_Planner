package com.example.travel_itinerary_planner

import android.content.Intent
import com.example.travel_itinerary_planner.databinding.ActivityPersonalDetailsBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalDetailsActivity : LoggedInActivity() {
    private lateinit var binding: ActivityPersonalDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarPersonalDetails.setNavigationOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToDrawerFragment", true)
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId?.let { fetchUserData(it) }

        setEditTextListeners()
    }

    private fun fetchUserData(userId: String) {
        val userRef = firestore.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userData = document.data
                    userData?.let { populateEditTextFields(it) }
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun populateEditTextFields(userData: Map<String, Any>) {
        binding.apply {
            email.setText(userData["Email"].toString())
            phone.setText(userData["PhoneNumber"].toString())
            fullName.setText(userData["Name"].toString())
            dateOfBirth.setText(userData["DateOfBirth"].toString())
            homeAddress.setText(userData["HomeAdd"].toString())
        }
    }

    private fun setEditTextListeners() {
        binding.apply {
            phone.setOnClickListener { showUpdateDialog(phone) }
            fullName.setOnClickListener { showUpdateDialog(fullName) }
            dateOfBirth.setOnClickListener { showUpdateDialog(dateOfBirth) }
            homeAddress.setOnClickListener { showUpdateDialog(homeAddress) }
        }
    }

    private fun showUpdateDialog(editText: EditText) {
        val currentValue = editText.text.toString()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update")

        val input = EditText(this)
        input.setText(currentValue)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val newValue = input.text.toString()
            editText.setText(newValue)
            dialog.dismiss()

            val currentUser = auth.currentUser
            val userId = currentUser?.uid

            userId?.let { updateUserData(it, editText.tag.toString(), newValue) }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
    private fun updateUserData(userId: String, field: String, value: String) {
        val userRef = firestore.collection("users").document(userId)

        userRef.update(field, value)
            .addOnSuccessListener {

                Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->

                Toast.makeText(this, "Failed to update data", Toast.LENGTH_SHORT).show()
            }
    }
}
