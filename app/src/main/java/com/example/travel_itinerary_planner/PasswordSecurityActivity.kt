package com.example.travel_itinerary_planner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.travel_itinerary_planner.databinding.ActivityPasswordSecurityBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class PasswordSecurityActivity : LoggedInActivity() {
    private lateinit var binding: ActivityPasswordSecurityBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordSecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.oldPasswordEditText.requestFocus()
        binding.buttonResetPassword.setOnClickListener {
            val oldPass = binding.oldPasswordEditText.text.toString().trim()
            val newPass = binding.newPasswordEditText.text.toString().trim()
            val confirmPass = binding.confirmNewPasswordEditText.text.toString().trim()
            val passwordRequirementMessage = """
                 Password must meet the criteria:
                 - At least 8 characters long
                 - At least one digit
                 - At least one lowercase letter
                 - At least one uppercase letter
                 - At least one special character
             """.trimIndent()
            if (oldPass.isEmpty()) {
                binding.oldPasswordEditText.error = "Password cannot be empty"
                binding.oldPasswordEditText.requestFocus()
                return@setOnClickListener
            } else if (!isPasswordValid(oldPass)) {
                binding.oldPasswordEditText.error = passwordRequirementMessage
                binding.oldPasswordEditText.requestFocus()
            }else if (newPass.isEmpty()) {
                binding.newPasswordEditText.error = "Password cannot be empty"
                binding.newPasswordEditText.requestFocus()
                return@setOnClickListener
            } else if (!isPasswordValid(newPass)) {
                binding.newPasswordEditText.error = passwordRequirementMessage
                binding.newPasswordEditText.requestFocus()
                return@setOnClickListener
            } else if (confirmPass.isEmpty()){
                binding.confirmNewPasswordEditText.error = "Confirm Password cannot be empty"
                binding.confirmNewPasswordEditText.requestFocus()
                return@setOnClickListener
            } else if (newPass != confirmPass) {
                binding.confirmNewPasswordEditText.error = "Passwords do not match"
                binding.confirmNewPasswordEditText.requestFocus()
                return@setOnClickListener
            } else {
                val user = auth.currentUser
                if (user != null && user.email != null) {

                    val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)
                    user.reauthenticate(credential)
                        .addOnCompleteListener { reAuthTask ->
                            if (reAuthTask.isSuccessful) {
                                user.updatePassword(newPass)
                                    .addOnCompleteListener { updatePasswordTask ->
                                        if (updatePasswordTask.isSuccessful) {
                                            val intent = Intent(this, BottomNavigationActivity::class.java)
                                            intent.putExtra("navigateToDrawerFragment", true)
                                            startActivity(intent)
                                            finish()
                                            Toast.makeText(this, "Update Password Successfully", Toast.LENGTH_SHORT).show()
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(this, "Failed to re-authenticate", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
                }


            }
        }
        binding.toolbarPassword.setNavigationOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToDrawerFragment", true)
            startActivity(intent)
        }
        binding.textForget.setOnClickListener {
            Toast.makeText(this, "The password reset link has been sent via email", Toast.LENGTH_SHORT).show()
        }

    }
    private fun isPasswordValid(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*()\\-_+=\\[\\]{}|;:'\",.<>?/])(?=\\S+$).{8,}$"
        return password.matches(passwordRegex.toRegex())
    }




}