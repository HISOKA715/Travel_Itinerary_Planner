package com.example.travel_itinerary_planner.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentHomeBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.notification.NotificationActivity
import com.example.travel_itinerary_planner.tourism_attraction.RecommandActivity
import com.example.travel_itinerary_planner.useractivity.UserListActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : LoggedInFragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var firestore: FirebaseFirestore
    private var attractions: MutableList<TourismAttraction> = mutableListOf()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonSmart.setOnClickListener {
            val intent = Intent(requireContext(), BottomNavigationActivity::class.java)
            intent.putExtra("navigateToSmartBudgetFragment", true)
            startActivity(intent)
        }
        binding.imageButtonSearch2.setOnClickListener {
            val intent = Intent(requireContext(), UserListActivity::class.java)
            startActivity(intent)
        }

        binding.textView29.setOnClickListener {
            val intent = Intent(requireContext(), RecommandActivity::class.java)
            startActivity(intent)
        }

        binding.imageButtonSearch1.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }
        setupRecyclerView()
        fetchTourismAttractions()
        firestore = FirebaseFirestore.getInstance()
        fetchTrips()
        retrieveSmartBudgetForYear()
    }
    private fun setupRecyclerView() {
        binding.horizontalRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = TourismListAdapter(attractions) { attraction ->
            }
    }
    private fun fetchTourismAttractions() {
        FirebaseFirestore.getInstance().collection("Tourism Attractions")
            .orderBy("clickRate",Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                attractions.clear()
                for (document in result) {
                    val id = document.id
                    val imageUrl = document.getString("TourismImage") ?: ""
                    val name = document.getString("TourismName") ?: ""
                    val location = document.getString("TourismState") ?: ""
                    val attraction = TourismAttraction(id,imageUrl, name, location)
                    attractions.add(attraction)
                }
                binding.horizontalRecyclerView.adapter?.notifyDataSetChanged()
            }.addOnFailureListener { exception ->

            }
    }

    private fun retrieveSmartBudgetForYear() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        firestore.collection("SmartBudget")
            .whereEqualTo("UserID", userId)
            .get()
            .addOnSuccessListener { smartBudgetQuerySnapshot ->
                val budgetIds = smartBudgetQuerySnapshot.documents.map { it.id }
                setupCurrencyYearSpinner(budgetIds)
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun setupCurrencyYearSpinner(budgetIds: List<String>) {
        val years = mutableSetOf<String>()
        val fireCollect = firestore.collection("SmartBudgetDetails")
        CoroutineScope(Dispatchers.IO).launch {
            budgetIds.forEach { budgetId ->
                val querySnapshot = fireCollect.whereEqualTo("BudgetID", budgetId).get().await()
                querySnapshot.documents.forEach { document ->
                    val expensesDate = document.getString("ExpensesDate")

                    expensesDate?.let {
                        val year = it.substring(0, 4)
                        years.add(year)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                val yearAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    years.toList()
                )

                yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.yearSpinner.adapter = yearAdapter
            }
        }


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
        val currencyCodes = currencyOptions.map { it.substring(0, 3) }.toTypedArray()
        val currencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencyOptions)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = currencyAdapter

        binding.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                parent?.let {
                    val selectedYear = parent.getItemAtPosition(position)?.toString()
                    val selectedCurrency = binding.currencySpinner.selectedItem?.toString()?.substring(0, 3)
                    if (selectedYear != null && selectedCurrency != null ) {

                            retrieveBarChart(selectedYear, selectedCurrency)

                    }

                    val currencyPosition = currencyOptions.indexOf(selectedYear)
                    if (currencyPosition != -1) {
                        binding.currencySpinner.setSelection(currencyPosition)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCurrency =
                    currencyCodes.getOrNull(position)
                val selectedCurrencyOption = currencyOptions.getOrNull(position)
                val selectedYear = binding.yearSpinner.selectedItem?.toString()
                if (selectedYear != null && selectedCurrency != null) {
                    retrieveBarChart(selectedYear, selectedCurrency)
                }

                selectedCurrencyOption?.let {
                    binding.currencySpinner.setSelection(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }


        }

    }

    private fun retrieveBarChart(selectedYear:String,selectedCurrency:String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val startOfYear = "$selectedYear-01-01"
        val endOfYear = "$selectedYear-12-31"

        firestore.collection("SmartBudget")
            .whereEqualTo("UserID", userId)
            .get()
            .addOnSuccessListener { smartBudgetQuerySnapshot ->
                val budgetIds = mutableSetOf<String>()
                smartBudgetQuerySnapshot.documents.forEach { budgetDoc ->
                    val budgetId = budgetDoc.id
                    val tripStartDate = budgetDoc.getString("TripStartDate")
                    val tripEndDate = budgetDoc.getString("TripEndDate")

                    if (tripStartDate != null && tripEndDate != null) {
                        if (tripStartDate >= startOfYear && tripEndDate <= endOfYear) {
                            budgetIds.add(budgetId)
                        }
                    }
                }

                searchConvertedAmount(budgetIds.toList(),selectedYear,selectedCurrency)


            }
            .addOnFailureListener { exception ->
            }
    }


    private fun searchConvertedAmount(budgetIds: List<String>,selectedYear: String,selectedCurrency: String) {
        fun hasNonZeroAmount(monthName: String, amounts: Map<String, Double>): Boolean {
            return amounts.containsKey(monthName) && amounts[monthName] != 0.0
        }
        val exchangeRates = mapOf(
            "AED" to 0.27, "AFN" to 0.014, "ALL" to 0.011, "AMD" to 0.0026, "ANG" to 0.56,
            "AOA" to 0.0012, "ARS" to 0.0011, "AUD" to 0.64, "AWG" to 0.55, "AZN" to 0.59,
            "BAM" to 0.55, "BBD" to 0.5, "BDT" to 0.0091, "BGN" to 0.54, "BHD" to 2.65,
            "BIF" to 0.00035, "BMD" to 1.0, "BND" to 0.74, "BOB" to 0.14, "BRL" to 0.19,
            "BSD" to 1.0, "BTN" to 0.012, "BWP" to 0.072, "BYN" to 0.31, "BZD" to 0.5,
            "CAD" to 0.73, "CDF" to 0.00036, "CHF" to 1.1, "CLP" to 0.001, "CNY" to 0.14,
            "COP" to 0.00026, "CRC" to 0.002, "CUP" to 0.042, "CVE" to 0.0096, "CZK" to 0.042,
            "DJF" to 0.0056, "DKK" to 0.14, "DOP" to 0.017, "DZD" to 0.0074, "EGP" to 0.021,
            "ERN" to 0.067, "ETB" to 0.018, "EUR" to 1.06, "FJD" to 0.44, "FKP" to 1.25,
            "FOK" to 0.14, "GBP" to 1.24, "GEL" to 0.38, "GGP" to 1.24, "GHS" to 0.07,
            "GIP" to 1.24, "GMD" to 0.02, "GNF" to 0.0001, "GTQ" to 0.13, "GYD" to 0.005,
            "HKD" to 0.13, "HNL" to 0.04, "HRK" to 0.14, "HTG" to 0.008, "HUF" to 0.003,
            "IDR" to 0.00006, "ILS" to 0.26, "IMP" to 1.24, "INR" to 0.01, "IQD" to 0.0008,
            "IRR" to 0.00002, "ISK" to 0.007, "JEP" to 1.25, "JMD" to 0.006, "JOD" to 1.41,
            "JPY" to 0.007, "KES" to 0.008, "KGS" to 0.01, "KHR" to 0.0003, "KID" to 0.66,
            "KMF" to 0.002, "KPW" to 0.001, "KRW" to 0.0007, "KWD" to 3.24, "KYD" to 1.2,
            "KZT" to 0.002, "LAK" to 0.00005, "LBP" to 0.00001, "LKR" to 0.003, "LRD" to 0.005,
            "LSL" to 0.05, "LYD" to 0.2, "MAD" to 0.098, "MDL" to 0.06, "MGA" to 0.0002,
            "MKD" to 0.02, "MMK" to 0.0005, "MNT" to 0.0003, "MOP" to 0.12, "MRU" to 0.025,
            "MUR" to 0.021, "MVR" to 0.065, "MWK" to 0.00057, "MXN" to 0.058, "MYR" to 0.20905,
            "MZN" to 0.016, "NAD" to 0.052, "NGN" to 0.0009, "NIO" to 0.027, "NOK" to 0.091,
            "NPR" to 0.0075, "NZD" to 0.59, "OMR" to 2.6, "PAB" to 1.0, "PEN" to 0.27,
            "PGK" to 0.26, "PHP" to 0.017, "PKR" to 0.0036, "PLN" to 0.25, "PYG" to 0.00013,
            "QAR" to 0.27, "RON" to 0.21, "RSD" to 0.0091, "RUB" to 0.011, "RWF" to 0.00077,
            "SAR" to 0.27, "SBD" to 0.12, "SCR" to 0.07, "SDG" to 0.0017, "SEK" to 0.091,
            "SGD" to 0.73, "SHP" to 1.24, "SLL" to 0.000051, "SOS" to 0.0018, "SRD" to 0.029,
            "STN" to 0.043429473, "SYP" to 0.000076912101, "SZL" to 0.052, "THB" to 0.027,
            "TJS" to 0.091, "TMT" to 0.29, "TND" to 0.32, "TOP" to 0.4213, "TRY" to 0.031,
            "TTD" to 0.15, "TVD" to 0.64214269, "TWD" to 0.031, "TZS" to 0.00039, "UAH" to 0.025,
            "UGX" to 0.00026, "USD" to 1.0, "UYU" to 0.026, "UZS" to 0.000078, "VES" to 0.027554463,
            "VND" to 0.000039, "VUV" to 0.0081621725, "WST" to 0.3598, "XAF" to 0.0016, "XCD" to 0.37,
            "XDR" to 1.3153172, "XOF" to 0.0016, "XPF" to 0.0089, "YER" to 0.004, "ZAR" to 0.052,
            "ZMW" to 0.039, "ZWL" to 0.003106
        )
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val collectionRef = firestore.collection("SmartBudgetDetails")

        val totalConvertedAmounts = mutableMapOf<String, Double>()

        for (month in 1..12) {
            val monthStartDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear.toInt())
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val monthEndDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear.toInt())
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val convertedAmounts = mutableListOf<Double>()

            for (budgetId in budgetIds) {
                collectionRef.whereEqualTo("BudgetID", budgetId).get()
                    .addOnSuccessListener { querySnapshot ->
                        for (documentSnapshot in querySnapshot.documents) {
                            val expensesDate = documentSnapshot.getString("ExpensesDate")
                            if (expensesDate != null) {
                                val expensesDateParsed = dateFormat.parse(expensesDate)
                                if (expensesDateParsed in monthStartDate..monthEndDate) {
                                    val convertedAmount = documentSnapshot.getString("ConvertedAmount")?.toDouble() ?: 0.0
                                    val convertedCurrency = documentSnapshot.getString("ConvertedCurrency") ?: "USD"
                                    val expensesToUsdExchangeRate = exchangeRates[convertedCurrency] ?: 1.0
                                    val amountInUSD = convertedAmount * expensesToUsdExchangeRate
                                    val usdToSelectedExchangeRate = exchangeRates[selectedCurrency] ?: 1.0
                                    val finalConvertedAmount = amountInUSD / usdToSelectedExchangeRate
                                    convertedAmounts.add(finalConvertedAmount)
                                }
                            }
                        }
                        val totalConvertedAmountForMonth = convertedAmounts.sum()
                        val formattedTotalConvertedAmountForMonth = String.format("%.2f", totalConvertedAmountForMonth)
                        val monthName = DateFormatSymbols().months[month - 1]
                        totalConvertedAmounts["Total Converted Amount for $monthName"] = formattedTotalConvertedAmountForMonth.toDouble()
                        Log.d("FirestoreQuery", "Total Converted Amount for $monthName: $formattedTotalConvertedAmountForMonth")

                        if (totalConvertedAmounts.size == 12) {

                            val nonZeroAmounts = totalConvertedAmounts.filter {
                                hasNonZeroAmount(
                                    it.key,
                                    totalConvertedAmounts
                                )
                            }
                            if (nonZeroAmounts.isNotEmpty()) {
                                setupBarChart(
                                    nonZeroAmounts.keys.map { it.substringAfterLast(" ") },
                                    nonZeroAmounts.values.toList()
                                )
                            } else {

                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FirestoreQuery", "Error getting documents for Budget ID: $budgetId", exception)
                    }
            }
        }



    }


        private fun setupBarChart(months: List<String>, amounts: List<Double>) {
        val entries = mutableListOf<BarEntry>()

        for (i in months.indices) {
            entries.add(BarEntry(i.toFloat(), amounts[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "")

        dataSet.color = resources.getColor(R.color.blue)
        dataSet.valueTextSize = 16f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {

                return String.format("%.2f", value) }
        }
        binding.barChartSmart.xAxis.textSize = 12f
        val barData = BarData(dataSet)
        barData.barWidth = 0.5f
        val leftYAxis = binding.barChartSmart.axisLeft
        leftYAxis.textSize = 12f
        binding.barChartSmart.data = barData
        val rightYAxis = binding.barChartSmart.axisRight
        rightYAxis.isEnabled = false
        val xAxis = binding.barChartSmart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val abbreviatedMonthNames = DateFormatSymbols().shortMonths

        xAxis.valueFormatter = IndexAxisValueFormatter(abbreviatedMonthNames)
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(false)
        xAxis.isGranularityEnabled = true
        xAxis.labelCount = months.size
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.labelRotationAngle = 0f
        binding.barChartSmart.axisLeft.setDrawGridLines(false)
        binding.barChartSmart.xAxis.setDrawGridLines(false)

        binding.barChartSmart.setScaleEnabled(false)
        binding.barChartSmart.setPinchZoom(false)



        binding.barChartSmart.description.isEnabled = false

        binding.barChartSmart.invalidate()
    }

    private fun fetchTrips() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        firestore.collection("SmartBudget").whereEqualTo("UserID",userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tripList = mutableListOf<String>()
                val budgetIds = mutableMapOf<String, String>()

                for (document in querySnapshot.documents) {
                    val tripName = document.getString("TripName")
                    val budgetId = document.getString("BudgetID")

                    tripName?.let { name ->
                        budgetId?.let { id ->
                            tripList.add("Smart Budget Trip: $name")
                            budgetIds["Smart Budget Trip: $name"] = id
                        }
                    }
                }
                if (tripList.isEmpty()) {
                    tripList.add("No Smart Budget Trips")
                }
                tripList.reverse()

                val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, tripList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.SmartTripSpinner.adapter = adapter

                binding.SmartTripSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedTripName = parent?.getItemAtPosition(position).toString()
                        val selectedBudgetId = budgetIds[selectedTripName]

                        selectedBudgetId?.let {
                            fetchPieChartData(selectedBudgetId)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }


            }
            .addOnFailureListener { exception ->
            }
    }
    private fun fetchPieChartData(budgetId: String) {
        firestore.collection("SmartBudgetDetails")
            .whereEqualTo("BudgetID", budgetId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val categoryMap = mutableMapOf<String, Double>()

                for (document in querySnapshot.documents) {
                    val category = document.getString("ExpensesCategory")
                    val amountStr = document.getString("ConvertedAmount")

                    category?.let { categoryName ->
                        val amount = amountStr?.toDoubleOrNull() ?: 0.0
                        val currentAmount = categoryMap[categoryName] ?: 0.0
                        categoryMap[categoryName] = currentAmount + amount
                    }
                }

                setupPieChart(categoryMap)
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun setupPieChart(categoryMap: Map<String, Double>) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        val categoryColors = mapOf(
            "Transportation" to Color.parseColor("#FF5722"),
            "Restaurants" to Color.parseColor("#FF9800"),
            "Accommodation" to Color.parseColor("#FFC107"),
            "Groceries" to Color.parseColor("#FFEB3B"),
            "Shopping" to Color.parseColor("#CDDC39"),
            "Activities" to Color.parseColor("#4CAF50"),
            "Drinks" to Color.parseColor("#009688"),
            "Coffee" to Color.parseColor("#00BCD4"),
            "Flights" to Color.parseColor("#03A9F4"),
            "Fees & Charges" to Color.parseColor("#2196F3"),
            "Sightseeing" to Color.parseColor("#3F51B5"),
            "Entertainment" to Color.parseColor("#673AB7"),
            "Laundry" to Color.parseColor("#9C27B0"),
            "Exchange Fees" to Color.parseColor("#E91E63"),
            "Others" to Color.parseColor("#F44336")
        )

        for ((category, amount) in categoryMap) {
            entries.add(PieEntry(amount.toFloat(), category))
            colors.add(categoryColors[category] ?: Color.BLACK)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 16f
        dataSet.valueFormatter = CustomValueFormatter()

        val pieData = PieData(dataSet)

        binding.pieChart.data = pieData
        binding.pieChart.centerText = "Expense Distribution by Category"
        binding.pieChart.centerText = "Expense Distribution by Category"
        binding.pieChart.setEntryLabelTextSize(16f)
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.legend.isEnabled = true
        binding.pieChart.animateY(1400, Easing.EaseInOutQuad)
        binding.pieChart.invalidate()
    }

    class CustomValueFormatter : ValueFormatter() {
        override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
            return String.format("%.2f", value)
        }
    }



}