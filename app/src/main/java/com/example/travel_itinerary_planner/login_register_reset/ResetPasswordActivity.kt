package com.example.travel_itinerary_planner.login_register_reset

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.example.travel_itinerary_planner.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            }else {
                val intent = Intent(this, OTPActivity::class.java)
                startActivity(intent)
            }

        }

    }
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}