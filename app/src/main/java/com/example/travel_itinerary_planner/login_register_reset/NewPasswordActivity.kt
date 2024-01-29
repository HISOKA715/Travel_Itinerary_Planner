package com.example.travel_itinerary_planner.login_register_reset

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.travel_itinerary_planner.databinding.ActivityNewPasswordBinding

class NewPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.editTextTextPassword.requestFocus()
        binding.resetPasswordBtn.setOnClickListener {

            val newPassword = binding.editTextTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextTextConfirmPassword.text.toString().trim()
            val passwordRequirementMessage = """
                 Password must meet the criteria:
                 - At least 8 characters long
                 - At least one digit
                 - At least one lowercase letter
                 - At least one uppercase letter
                 - At least one special character
             """.trimIndent()
            if (newPassword.isEmpty()) {
                binding.editTextTextPassword.error = "Password cannot be empty"
                binding.editTextTextPassword.requestFocus()
                return@setOnClickListener
            } else if (!isPasswordValid(newPassword)) {
                binding.editTextTextPassword.error = passwordRequirementMessage
                binding.editTextTextPassword.requestFocus()
                return@setOnClickListener
            } else if (confirmPassword.isEmpty()){
                binding.editTextTextConfirmPassword.error = "Confirm Password cannot be empty"
                binding.editTextTextConfirmPassword.requestFocus()
                return@setOnClickListener
            } else if (newPassword != confirmPassword) {
                binding.editTextTextConfirmPassword.error = "Passwords do not match"
                binding.editTextTextConfirmPassword.requestFocus()
                return@setOnClickListener
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Reset Password Successfully", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun isPasswordValid(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*()\\-_+=\\[\\]{}|;:'\",.<>?/])(?=\\S+$).{8,}$"
        return password.matches(passwordRegex.toRegex())
    }

}