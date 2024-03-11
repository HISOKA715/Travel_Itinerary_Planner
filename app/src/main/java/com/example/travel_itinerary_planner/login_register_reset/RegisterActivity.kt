package com.example.travel_itinerary_planner.login_register_reset

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import com.example.travel_itinerary_planner.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var textWatcher: TextWatcher
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersCollection: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        usersCollection = firestore.collection("users")
        binding.editTextName.requestFocus()
        val genders = arrayOf("Female", "Male")

        val adapter = GenderAdapter(this, genders)
        binding.spinner.adapter = adapter
        binding.textViewLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        dobAction()
        registerBtnAction()

    }


    private fun dobAction(){
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

                val enteredDate = s.toString()
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.isLenient = false

                try {
                    val parsedDate = sdf.parse(enteredDate)

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
                    binding.editTextDOB.setText(selectedDate)
                    binding.editTextDOB.error = null
                },
                year,
                month,
                dayOfMonth
            )

            datePickerDialog.show()
        }
    }

    private fun registerBtnAction() {

        binding.registerBtn.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val username = binding.editTextUsername.text.toString().trim()
            val dob = binding.editTextDOB.text.toString().trim()
            val phone = binding.editTextPhone.text.toString().trim()
            val address = binding.editTextPostalAddress.text.toString().trim()
            val emailRegister = binding.editTextEmailAddress.text.toString().trim()
            val passwordRegister = binding.editTextPassword.text.toString().trim()
            val confirmPasswordRegister = binding.editTextConfirmPassword.text.toString().trim()
            val gender = binding.spinner.selectedItem.toString().trim()
            val passwordRequirementMessage = """
                 Password must meet the criteria:
                 - At least 8 characters long
                 - At least one digit
                 - At least one lowercase letter
                 - At least one uppercase letter
                 - At least one special character
             """.trimIndent()

            if (name.isEmpty()) {
                binding.editTextName.error = "Name cannot be empty"
                binding.editTextName.requestFocus()
                return@setOnClickListener
            } else if (!isNameValid(name)) {
                binding.editTextName.error = "Name must be alphabetical"
                binding.editTextName.requestFocus()
            } else if (username.isEmpty()) {
                binding.editTextUsername.error = "Username cannot be empty"
                binding.editTextUsername.requestFocus()
                return@setOnClickListener
            } else if (dob.isEmpty()) {
                binding.editTextDOB.error = "Date of Birth cannot be empty"
                binding.editTextDOB.requestFocus()
                return@setOnClickListener
            } else if (!isDateValid(dob)) {
                binding.editTextDOB.error = "Invalid date format (dd/MM/yyyy)"
                binding.editTextDOB.requestFocus()
                return@setOnClickListener
            } else if (emailRegister.isEmpty()) {
                binding.editTextEmailAddress.error = "Email cannot be empty"
                binding.editTextEmailAddress.requestFocus()
                return@setOnClickListener
            } else if (!isEmailValid(emailRegister)) {
                binding.editTextEmailAddress.error = "Enter a valid email address"
                binding.editTextEmailAddress.requestFocus()
                return@setOnClickListener
            } else if (phone.isEmpty()) {
                binding.editTextPhone.error = "Phone number cannot be empty"
                binding.editTextPhone.requestFocus()
                return@setOnClickListener
            } else if (!isPhoneValid(phone)) {
                binding.editTextPhone.error = "Invalid phone number format (+1234567890)"
                binding.editTextPhone.requestFocus()
                return@setOnClickListener
            } else if (address.isEmpty()) {
                binding.editTextPostalAddress.error = "Home address cannot be empty"
                binding.editTextPostalAddress.requestFocus()
                return@setOnClickListener
            } else if (passwordRegister.isEmpty()) {
                binding.editTextPassword.error = "Password cannot be empty"
                binding.editTextPassword.requestFocus()
                return@setOnClickListener
            } else if (!isPasswordValid(passwordRegister)) {
                binding.editTextPassword.error = passwordRequirementMessage
                binding.editTextPassword.requestFocus()
                return@setOnClickListener
            } else if (confirmPasswordRegister.isEmpty()) {
                binding.editTextConfirmPassword.error = "Confirm Password cannot be empty"
                binding.editTextConfirmPassword.requestFocus()
                return@setOnClickListener
            } else if (passwordRegister != confirmPasswordRegister) {
                binding.editTextConfirmPassword.error = "Passwords do not match"
                binding.editTextConfirmPassword.requestFocus()
                return@setOnClickListener
            } else {
                auth.createUserWithEmailAndPassword(emailRegister, passwordRegister)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            val uid = user?.uid

                            val newUser = hashMapOf(
                                "Username" to username,
                                "Password" to confirmPasswordRegister,
                                "Name" to name,
                                "DateOfBirth" to dob,
                                "Gender" to gender,
                                "PhoneNumber" to phone,
                                "Email" to emailRegister,
                                "HomeAdd" to address,
                                "ProfileImage" to null,
                                "UserCategory" to "User"
                            )

                            val newUserDocRef = firestore.collection("users").document(uid!!)
                            newUserDocRef.set(newUser)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Registration successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Error creating user: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Error creating user: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


            }
        }
    }






    private fun isNameValid(name: String): Boolean {
        val regex = "^[a-zA-Z\\s]+$".toRegex()
        return regex.matches(name)
    }
    private fun isDateValid(date: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        sdf.isLenient = false
        return try {
            sdf.parse(date)
            true
        } catch (e: ParseException) {
            false
        }
    }


    private fun isPhoneValid(phone: String): Boolean {
         //(+XX or +XXX)
        val internationalRegex = "^\\+(?:[0-9] ?){6,14}[0-9]\$"

        //(e.g., +XX (XXX) XXX-XXXX)
        val countryCodeAreaCodeRegex = "^\\+(?:[0-9] ?){6,14}\\((?:[0-9] ?){1,6}\\)(?:[0-9] ?){6,14}[0-9]\$"

        //  (e.g., +XX-XXX-XXXX-XXXX)
        val countryCodeAreaCodeSeparatorRegex = "^\\+(?:[0-9] ?){6,14}-[0-9](?: ?-[0-9] ?){5,14}[0-9]\$"

        // (e.g., +XX XXXX XXXX)
        val countryCodeAreaCodeSpaceRegex = "^\\+(?:[0-9] ?){6,14} (?:[0-9] ?){5,14}[0-9]\$"

        val combinedRegex = "$internationalRegex|$countryCodeAreaCodeRegex|$countryCodeAreaCodeSeparatorRegex|$countryCodeAreaCodeSpaceRegex"

        return phone.matches(combinedRegex.toRegex())
    }
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun isPasswordValid(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*()\\-_+=\\[\\]{}|;:'\",.<>?/])(?=\\S+$).{8,}$"
        return password.matches(passwordRegex.toRegex())
    }

}

