package com.example.travel_itinerary_planner.smart_budget

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.databinding.ActivitySmartEditTripBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SmartEditTripActivity: LoggedInActivity(){
    private lateinit var binding: ActivitySmartEditTripBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        binding = ActivitySmartEditTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val budgetId = intent.getStringExtra("budgetId")
        if (budgetId != null) {
            fetchSmartBudgetData(budgetId)
        }
        binding.toolbarEditTrip.setNavigationOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToSmartBudgetFragment", true)
            startActivity(intent)
        }
        binding.currencyEdit.setOnClickListener {
            showCurrencyDialog(binding.currencyEdit, this)
        }

        binding.buttonSaveEditTrip.setOnClickListener {
            saveSmartBudget()
        }
        setupDatePickers()
        setDecimalInputFilter(binding.budgetEdit)
    }

    private fun setDecimalInputFilter(editText: EditText) {
        editText.filters = arrayOf<InputFilter>(object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                val decimalSeparator = '.'
                val sourceBuilder = StringBuilder(dest.toString())
                source?.let { sourceBuilder.replace(dstart, dend, source.toString()) }
                val sourceString = sourceBuilder.toString()

                val separatorIndex = sourceString.indexOf(decimalSeparator)

                if (separatorIndex != -1) {

                    val decimalPlaces = sourceString.length - separatorIndex - 1
                    if (decimalPlaces > 2) {

                        return ""
                    }
                }
                return null
            }
        })
    }
    private fun fetchSmartBudgetData(budgetId: String) {
        firestore.collection("SmartBudget").document(budgetId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                    val data = documentSnapshot.data
                    if (data != null) {

                        binding.tripEditName.setText(data["TripName"].toString())
                        binding.currencyEdit.setText(data["BudgetCurrency"].toString())
                        binding.budgetEdit.setText(data["Budget"].toString())
                        binding.startDateEdit.setText(data["TripStartDate"].toString())
                        binding.endDateEdit.setText(data["TripEndDate"].toString())
                        binding.textViewEditTripDays.setText(data["TripDuration"].toString())

                    }
                } else {

                    Toast.makeText(this, "Budget Trip not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->

                Toast.makeText(this, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveSmartBudget() {
        val tripName = binding.tripEditName.text.toString().trim()
        val budgetCurrency = binding.currencyEdit.text.toString().trim()
        val tripStartDate = binding.startDateEdit.text.toString().trim()
        val tripEndDate = binding.endDateEdit.text.toString().trim()
        val budgetId = intent.getStringExtra("budgetId")
        if (tripName.isEmpty()) {
            binding.tripEditName.error = "Trip name cannot be empty"
            return
        }
        if (budgetCurrency.isEmpty()) {
            binding.currencyEdit.error = "Currency cannot be empty"
            return
        }
        if (tripStartDate.isEmpty()) {
            binding.startDateEdit.error = "Trip start date cannot be empty"
            return
        }
        if (tripEndDate.isEmpty()) {
            binding.endDateEdit.error = "Trip end date cannot be empty"
            return
        }

            val userId = auth.currentUser?.uid


            val smartBudgetData = hashMapOf(
                "BudgetID" to budgetId,
                "UserID" to userId.toString(),
                "TripName" to binding.tripEditName.text.toString(),
                "BudgetCurrency" to binding.currencyEdit.text.toString(),
                "Budget" to String.format("%.2f", binding.budgetEdit.text.toString().toDoubleOrNull() ?: 0.00),
                "TripStartDate" to binding.startDateEdit.text.toString(),
                "TripEndDate" to binding.endDateEdit.text.toString(),
                "TripDuration" to binding.textViewEditTripDays.text.toString(),
            )


        firestore.collection("SmartBudget").document(budgetId!!)
            .update(smartBudgetData as MutableMap<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Trip updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, BottomNavigationActivity::class.java)
                    intent.putExtra("navigateToSmartBudgetFragment", true)
                    startActivity(intent)
                }
                .addOnFailureListener {

                }
    }


    private fun showCurrencyDialog(currency: EditText, context: Context) {


        val currencyOptions = arrayOf(
            "AED - United Arab Emirates Dirham",
            "AFN - Afghan Afghani",
            "ALL - Albanian Lek",
            "AMD - Armenian Dram",
            "ANG - Netherlands Antillean Guilder",
            "AOA - Angolan Kwanza",
            "ARS - Argentine Peso",
            "AUD - Australian Dollar",
            "AWG - Aruban Florin",
            "AZN - Azerbaijani Manat",
            "BAM - Bosnia-Herzegovina Convertible Mark",
            "BBD - Barbadian Dollar",
            "BDT - Bangladeshi Taka",
            "BGN - Bulgarian Lev",
            "BHD - Bahraini Dinar",
            "BIF - Burundian Franc",
            "BMD - Bermudian Dollar",
            "BND - Brunei Dollar",
            "BOB - Bolivian Boliviano",
            "BRL - Brazilian Real",
            "BSD - Bahamian Dollar",
            "BTN - Bhutanese Ngultrum",
            "BWP - Botswana Pula",
            "BYN - Belarusian Ruble",
            "BZD - Belize Dollar",
            "CAD - Canadian Dollar",
            "CDF - Congolese Franc",
            "CHF - Swiss Franc",
            "CLP - Chilean Peso",
            "CNY - Chinese Yuan",
            "COP - Colombian Peso",
            "CRC - Costa Rican Colón",
            "CUP - Cuban Peso",
            "CVE - Cape Verdean Escudo",
            "CZK - Czech Koruna",
            "DJF - Djiboutian Franc",
            "DKK - Danish Krone",
            "DOP - Dominican Peso",
            "DZD - Algerian Dinar",
            "EGP - Egyptian Pound",
            "ERN - Eritrean Nakfa",
            "ETB - Ethiopian Birr",
            "EUR - Euro",
            "FJD - Fijian Dollar",
            "FKP - Falkland Islands Pound",
            "FOK - Fijian Dollar",
            "GBP - British Pound",
            "GEL - Georgian Lari",
            "GGP - Guernsey Pound",
            "GHS - Ghanaian Cedi",
            "GIP - Gibraltar Pound",
            "GMD - Gambian Dalasi",
            "GNF - Guinean Franc",
            "GTQ - Guatemalan Quetzal",
            "GYD - Guyanaese Dollar",
            "HKD - Hong Kong Dollar",
            "HNL - Honduran Lempira",
            "HRK - Croatian Kuna",
            "HTG - Haitian Gourde",
            "HUF - Hungarian Forint",
            "IDR - Indonesian Rupiah",
            "ILS - Israeli Shekel",
            "IMP - Isle of Man Pound",
            "INR - Indian Rupee",
            "IQD - Iraqi Dinar",
            "IRR - Iranian Rial",
            "ISK - Icelandic Króna",
            "JEP - Jersey Pound",
            "JMD - Jamaican Dollar",
            "JOD - Jordanian Dinar",
            "JPY - Japanese Yen",
            "KES - Kenyan Shilling",
            "KGS - Kyrgyzstani Som",
            "KHR - Cambodian Riel",
            "KID - Kiribati Dollar",
            "KMF - Comorian Franc",
            "KPW - North Korean Won",
            "KRW - South Korean Won",
            "KWD - Kuwaiti Dinar",
            "KYD - Cayman Islands Dollar",
            "KZT - Kazakhstani Tenge",
            "LAK - Laotian Kip",
            "LBP - Lebanese Pound",
            "LKR - Sri Lankan Rupee",
            "LRD - Liberian Dollar",
            "LSL - Lesotho Loti",
            "LYD - Libyan Dinar",
            "MAD - Moroccan Dirham",
            "MDL - Moldovan Leu",
            "MGA - Malagasy Ariary",
            "MKD - Macedonian Denar",
            "MMK - Myanma Kyat",
            "MNT - Mongolian Tugrik",
            "MOP - Macanese Pataca",
            "MRU - Mauritanian Ouguiya",
            "MUR - Mauritian Rupee",
            "MVR - Maldivian Rufiyaa",
            "MWK - Malawian Kwacha",
            "MXN - Mexican Peso",
            "MYR - Malaysian Ringgit",
            "MZN - Mozambican Metical",
            "NAD - Namibian Dollar",
            "NGN - Nigerian Naira",
            "NIO - Nicaraguan Córdoba",
            "NOK - Norwegian Krone",
            "NPR - Nepalese Rupee",
            "NZD - New Zealand Dollar",
            "OMR - Omani Rial",
            "PAB - Panamanian Balboa",
            "PEN - Peruvian Nuevo Sol",
            "PGK - Papua New Guinean Kina",
            "PHP - Philippine Peso",
            "PKR - Pakistani Rupee",
            "PLN - Polish Złoty",
            "PYG - Paraguayan Guarani",
            "QAR - Qatari Riyal",
            "RON - Romanian Leu",
            "RSD - Serbian Dinar",
            "RUB - Russian Ruble",
            "RWF - Rwandan Franc",
            "SAR - Saudi Riyal",
            "SBD - Solomon Islands Dollar",
            "SCR - Seychellois Rupee",
            "SDG - Sudanese Pound",
            "SEK - Swedish Krona",
            "SGD - Singapore Dollar",
            "SHP - Saint Helena Pound",
            "SLL - Sierra Leonean Leone",
            "SOS - Somali Shilling",
            "SRD - Surinamese Dollar",
            "STN - São Tomé and Príncipe Dobra",
            "SYP - Syrian Pound",
            "SZL - Eswatini Lilangeni",
            "THB - Thai Baht",
            "TJS - Tajikistani Somoni",
            "TMT - Turkmenistani Manat",
            "TND - Tunisian Dinar",
            "TOP - Tongan Pa'anga",
            "TRY - Turkish Lira",
            "TTD - Trinidad and Tobago Dollar",
            "TVD - Tuvaluan Dollar",
            "TWD - New Taiwan Dollar",
            "TZS - Tanzanian Shilling",
            "UAH - Ukrainian Hryvnia",
            "UGX - Ugandan Shilling",
            "USD - United States Dollar",
            "UYU - Uruguayan Peso",
            "UZS - Uzbekistan Som",
            "VES - Venezuelan Bolívar Soberano",
            "VND - Vietnamese Đồng",
            "VUV - Vanuatu Vatu",
            "WST - Samoan Tala",
            "XAF - Central African CFA Franc",
            "XCD - East Caribbean Dollar",
            "XDR - Special Drawing Rights",
            "XOF - West African CFA Franc",
            "XPF - CFP Franc",
            "YER - Yemeni Rial",
            "ZAR - South African Rand",
            "ZMW - Zambian Kwacha",
            "ZWL - Zimbabwean Dollar"
        )




        val dialogBuilder = AlertDialog.Builder(context)
            .setTitle("Select Currency")
            .setItems(currencyOptions) { _, which ->
                val selectedCurrency = currencyOptions[which].substringBefore(" - ")
                currency.setText(selectedCurrency)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialogBuilder.show()
    }
    private fun setupDatePickers() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val startDateListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = sdf.format(Date(year - 1900, monthOfYear, dayOfMonth))
            binding.startDateEdit.setText(selectedDate)
            updateEndDateMinDate(selectedDate)
            updateTripDuration()
        }

        val endDateListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = sdf.format(Date(year - 1900, monthOfYear, dayOfMonth))
            binding.endDateEdit.setText(selectedDate)
            updateTripDuration()
        }

        binding.startDateEdit.setOnClickListener {
            showDatePickerDialog(startDateListener)
        }

        binding.endDateEdit.setOnClickListener {
            showEndDatePickerDialog(endDateListener)
        }
    }

    private fun updateEndDateMinDate(selectedDate: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(selectedDate)!!

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val endDate = Calendar.getInstance()
                endDate.set(year, monthOfYear, dayOfMonth)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                if (endDate.before(calendar)) {
                    Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                } else {
                    binding.endDateEdit.setText(sdf.format(endDate.time))
                    updateTripDuration()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun showEndDatePickerDialog(dateListener: DatePickerDialog.OnDateSetListener) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val startDateString = binding.startDateEdit.text.toString()
        if (startDateString.isNotEmpty()) {
            calendar.time = sdf.parse(startDateString)!!
        }

        val datePickerDialog = DatePickerDialog(
            this,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }


    private fun showDatePickerDialog(dateListener: DatePickerDialog.OnDateSetListener) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            dateListener,
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }

    private fun updateTripDuration() {
        val startDateString = binding.startDateEdit.text.toString()
        val endDateString = binding.endDateEdit.text.toString()

        if (startDateString.isNotEmpty() && endDateString.isNotEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val startDate = sdf.parse(startDateString)
                val endDate = sdf.parse(endDateString)

                val diffInMillis = endDate.time - startDate.time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1
                binding.textViewEditTripDays.text = diffInDays.toString()
            } catch (e: ParseException) {

            }
        } else {
            binding.textViewEditTripDays.text = ""
        }
    }


}
