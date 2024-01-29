package com.example.travel_itinerary_planner.login_register_reset

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import com.example.travel_itinerary_planner.databinding.ActivityLoginBinding
import android.util.Patterns
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editTextEmail.requestFocus()
        binding.textViewCreateAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.textViewForgetPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
        binding.loginBtn.setOnClickListener {
            val emailLogin = binding.editTextEmail.text.toString().trim()
            val passwordLogin = binding.editPassword.text.toString().trim()
            val passwordRequirementMessage = """
                 Password must meet the criteria:
                 - At least 8 characters long
                 - At least one digit
                 - At least one lowercase letter
                 - At least one uppercase letter
                 - At least one special character
             """.trimIndent()

            if (emailLogin.isEmpty()) {
                binding.editTextEmail.error = "Email cannot be empty"
                binding.editTextEmail.requestFocus()
                return@setOnClickListener
            } else if (!isEmailValid(emailLogin)) {
                binding.editTextEmail.error = "Enter a valid email address"
                binding.editTextEmail.requestFocus()
                return@setOnClickListener
            } else if (passwordLogin.isEmpty()) {
                binding.editPassword.error = "Password cannot be empty"
                binding.editPassword.requestFocus()
                return@setOnClickListener
            } else if (!isPasswordValid(passwordLogin)) {
                binding.editPassword.error = passwordRequirementMessage
                binding.editPassword.requestFocus()
                return@setOnClickListener
            }else{
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
            }
        }
        setPasswordVisibility()
        binding.checkBoxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            isPasswordVisible = isChecked
            setPasswordVisibility()
        }

    }
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*()\\-_+=\\[\\]{}|;:'\",.<>?/])(?=\\S+$).{8,}$"
        return password.matches(passwordRegex.toRegex())
    }

    private fun setPasswordVisibility() {
        if (isPasswordVisible) {
            binding.editPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.editPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        binding.editPassword.setSelection(binding.editPassword.text?.length ?: 0)
    }


}