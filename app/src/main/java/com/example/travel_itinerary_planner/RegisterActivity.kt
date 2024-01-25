package com.example.travel_itinerary_planner

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CalendarView
import android.widget.EditText
import com.example.travel_itinerary_planner.databinding.ActivityMainBinding
import com.example.travel_itinerary_planner.databinding.ActivityRegisterBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var textWatcher: TextWatcher
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val genders = arrayOf("Female", "Male")

        val adapter = GenderAdapter(this, genders)
        binding.spinner.adapter = adapter

        binding.textViewLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No implementation needed
            }

            override fun afterTextChanged(s: Editable?) {
                // Validate the entered date and update the EditText text if necessary
                val enteredDate = s.toString()
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.isLenient = false // Ensure strict date parsing

                try {
                    val parsedDate = sdf.parse(enteredDate)
                    // If parsing succeeds, update the EditText text to the parsed date
                    parsedDate?.let {
                        val cal = Calendar.getInstance()
                        cal.time = it
                        val year = cal.get(Calendar.YEAR)
                        val month = cal.get(Calendar.MONTH)
                        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                        val selectedDate = "$dayOfMonth/${month + 1}/$year"
                        if (selectedDate != enteredDate) {
                            binding.editTextDOB.removeTextChangedListener(this)
                            binding.editTextDOB.setText(selectedDate)
                            binding.editTextDOB.setSelection(selectedDate.length)
                            binding.editTextDOB.addTextChangedListener(this)
                        }
                    }
                } catch (e: ParseException) {
                    // Date is invalid, do nothing
                }
            }
        }

        binding.editTextDOB.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                    binding.editTextDOB.removeTextChangedListener(textWatcher)
                    binding.editTextDOB.setText(selectedDate)
                    binding.editTextDOB.addTextChangedListener(textWatcher)
                },
                year,
                month,
                dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.editTextDOB.addTextChangedListener(textWatcher)
    }

    }
