package com.example.travel_itinerary_planner.login_register_reset

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.travel_itinerary_planner.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.loginSmallView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.emailEdit.requestFocus()

        binding.nextBtn.setOnClickListener {
            val email = binding.emailEdit.text.toString().trim()

            if (email.isEmpty()) {
                binding.emailEdit.error = "Email cannot be empty"
                binding.emailEdit.requestFocus()
                return@setOnClickListener
            } else if (!isEmailValid(email)) {
                binding.emailEdit.error = "Enter a valid email address"
                binding.emailEdit.requestFocus()
                return@setOnClickListener
            } else {
                val usersRef = firestore.collection("users")
                usersRef.get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val userEmail = document.getString("Email")
                        if (userEmail != null && userEmail == email) {
                            auth.sendPasswordResetEmail(email).addOnCompleteListener { resetTask ->
                                if (resetTask.isSuccessful) {
                                    Toast.makeText(this, "Email sent to reset password", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Failed to send email", Toast.LENGTH_SHORT).show()
                                }
                            }
                            return@addOnSuccessListener
                        }
                    }

                    Toast.makeText(this, "Email does not exist", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to query Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            }

        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }







}
