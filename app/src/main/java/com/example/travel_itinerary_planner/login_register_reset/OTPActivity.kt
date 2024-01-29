package com.example.travel_itinerary_planner.login_register_reset

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import com.example.travel_itinerary_planner.databinding.ActivityOtpBinding

class OTPActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        otpFocus()
        binding.loginSmail.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.submitOTPBtn.setOnClickListener{
            if (!isOTPComplete()) {
                binding.otpBox6.error = "Please fill in all OTP digits"
            } else{
                val intent = Intent(this, NewPasswordActivity::class.java)
                startActivity(intent)
            }
        }


    }


    private fun otpFocus(){
        binding.otpBox1.requestFocus()

        val otpTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                when (s) {
                    binding.otpBox1.text -> binding.otpBox2.requestFocus()
                    binding.otpBox2.text -> binding.otpBox3.requestFocus()
                    binding.otpBox3.text -> binding.otpBox4.requestFocus()
                    binding.otpBox4.text -> binding.otpBox5.requestFocus()
                    binding.otpBox5.text -> binding.otpBox6.requestFocus()
                }

            }
        }
        val otpKeyListener = View.OnKeyListener { v, keyCode, event ->

            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {

                when (v) {
                    binding.otpBox1 -> {
                        if (binding.otpBox1.text.isNullOrEmpty()) {
                            binding.otpBox1.requestFocus()

                        } else {
                            binding.otpBox1.text.clear()
                            binding.otpBox1.requestFocus()
                        }
                    }

                    binding.otpBox2 -> {
                        if (binding.otpBox2.text.isNullOrEmpty()) {
                            binding.otpBox1.text.clear()
                            binding.otpBox1.requestFocus()
                        } else {
                            binding.otpBox2.text.clear()
                            binding.otpBox2.requestFocus()
                        }
                    }

                    binding.otpBox3 -> {
                        if (binding.otpBox3.text.isNullOrEmpty()) {

                            binding.otpBox2.text.clear()
                            binding.otpBox2.requestFocus()
                        } else {

                            binding.otpBox3.text.clear()
                            binding.otpBox3.requestFocus()
                        }
                    }

                    binding.otpBox4 -> {
                        if (binding.otpBox4.text.isNullOrEmpty()) {
                            binding.otpBox3.text.clear()
                            binding.otpBox3.requestFocus()

                        } else {
                            binding.otpBox4.text.clear()
                            binding.otpBox4.requestFocus()
                        }
                    }

                    binding.otpBox5 -> {
                        if (binding.otpBox5.text.isNullOrEmpty()) {
                            binding.otpBox4.text.clear()
                            binding.otpBox4.requestFocus()
                        } else {
                            binding.otpBox5.text.clear()
                            binding.otpBox5.requestFocus()

                        }
                    }

                    binding.otpBox6 -> {
                        if (binding.otpBox6.text.isNullOrEmpty()) {
                            binding.otpBox5.text.clear()
                            binding.otpBox5.requestFocus()
                        } else {
                            binding.otpBox6.text.clear()
                            binding.otpBox6.requestFocus()
                        }

                    }

                }
                return@OnKeyListener true
            }
            false

        }
        binding.otpBox1.addTextChangedListener(otpTextWatcher)
        binding.otpBox2.addTextChangedListener(otpTextWatcher)
        binding.otpBox3.addTextChangedListener(otpTextWatcher)
        binding.otpBox4.addTextChangedListener(otpTextWatcher)
        binding.otpBox5.addTextChangedListener(otpTextWatcher)
        binding.otpBox6.addTextChangedListener(otpTextWatcher)
        binding.otpBox1.setOnKeyListener(otpKeyListener)
        binding.otpBox2.setOnKeyListener(otpKeyListener)
        binding.otpBox3.setOnKeyListener(otpKeyListener)
        binding.otpBox4.setOnKeyListener(otpKeyListener)
        binding.otpBox5.setOnKeyListener(otpKeyListener)
        binding.otpBox6.setOnKeyListener(otpKeyListener)
    }
    private fun isOTPComplete(): Boolean {
        val otpBox1 = binding.otpBox1.text.toString()
        val otpBox2 = binding.otpBox2.text.toString()
        val otpBox3 = binding.otpBox3.text.toString()
        val otpBox4 = binding.otpBox4.text.toString()
        val otpBox5 = binding.otpBox5.text.toString()
        val otpBox6 = binding.otpBox6.text.toString()

        return otpBox1.isNotEmpty() && otpBox2.isNotEmpty() &&
                otpBox3.isNotEmpty() && otpBox4.isNotEmpty() &&
                otpBox5.isNotEmpty() && otpBox6.isNotEmpty()
    }
}