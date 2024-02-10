package com.example.travel_itinerary_planner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.travel_itinerary_planner.databinding.ActivityNewPasswordBinding
import com.example.travel_itinerary_planner.databinding.ActivityPasswordSecurityBinding
import com.example.travel_itinerary_planner.login_register_reset.LoginActivity

class PasswordSecurityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordSecurityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordSecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
                val intent = Intent(this, BottomNavigationActivity::class.java)
                intent.putExtra("navigateToDrawerFragment", true)
                startActivity(intent)
                finish()
                Toast.makeText(this, "Reset Password Successfully", Toast.LENGTH_SHORT).show()
            }
        }
        binding.toolbarPassword.setOnClickListener {
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