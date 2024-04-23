package com.example.travel_itinerary_planner.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentHomeBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.notification.NotificationActivity
import com.example.travel_itinerary_planner.notification.NotificationDetailActivity
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
        setupCurrencyYearSpinner()
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
    private fun setupCurrencyYearSpinner(){
        val years = mutableListOf<String>()
        firestore.collection("SmartBudgetDetails")
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { document ->
                    val expensesDate = document.getString("ExpensesDate")
                    if (expensesDate != null) {
                        val year = expensesDate.substring(0, 4)
                        if (!years.contains(year)) {
                            years.add(year)
                        }
                    }
                }
                val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
                yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.yearSpinner.adapter = yearAdapter
            }
            .addOnFailureListener { exception ->
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
        val currencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencyOptions)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = currencyAdapter

        binding.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchDataForYearAndCurrency(parent?.getItemAtPosition(position).toString(), binding.currencySpinner.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchDataForYearAndCurrency(binding.currencySpinner.selectedItem.toString(), parent?.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
    private fun fetchDataForYearAndCurrency(selectedYear: String, selectedCurrency: String) {

        for (i in 1..12) {
            val month = String.format("%02d", i)
            val startDate = "$selectedYear-$month-01"
            val endDate = "$selectedYear-$month-31"

            firestore.collection("SmartBudgetDetails")
                .whereGreaterThanOrEqualTo("ExpensesDate", startDate)
                .whereLessThanOrEqualTo("ExpensesDate", endDate)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    var totalExpenses = 0.0
                    querySnapshot.documents.forEach { document ->
                        val amountStr = document.getString("ConvertedAmount")
                        val currency = document.getString("ConvertedCurrency")
                        if (amountStr != null && currency != null) {
                            val amount = amountStr.toDoubleOrNull()
                            if (amount != null && currency == selectedCurrency) {
                                totalExpenses += amount
                            }
                        }
                    }


                    displayMonthlyExpenses(selectedYear,month, totalExpenses)
                }
                .addOnFailureListener { exception ->
                }
        }
    }
    private fun displayMonthlyExpenses(selectedYear: String, month: String, expenses: Double) {
        val barEntries = mutableListOf<BarEntry>()

        barEntries.add(BarEntry(month.toFloat(), expenses.toFloat()))

        val barDataSet = BarDataSet(barEntries, "Monthly Expenses")
        barDataSet.color = Color.parseColor("#2979FF")

        val barData = BarData(barDataSet)
        barData.barWidth = 0.5f

        binding.barChartSmart.data = barData


        val xAxis = binding.barChartSmart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(getMonthLabels())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        val leftAxis = binding.barChartSmart.axisLeft
        leftAxis.axisMinimum = 0f

        binding.barChartSmart.description.text = "Monthly Expenses $selectedYear"



        binding.barChartSmart.description.isEnabled = false
        binding.barChartSmart.legend.isEnabled = false
        binding.barChartSmart.setPinchZoom(false)
        binding.barChartSmart.isDoubleTapToZoomEnabled = false
        binding.barChartSmart.animateY(1000)

        binding.barChartSmart.invalidate()
    }

    private fun getMonthLabels(): List<String> {
        return listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }

    private fun fetchTrips() {
        firestore.collection("SmartBudget")
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