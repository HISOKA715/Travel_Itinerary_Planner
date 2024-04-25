package com.example.travel_itinerary_planner.smart_budget

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ActivitySmartEditExpensesBinding
import com.example.travel_itinerary_planner.databinding.ActivitySmartExpensesBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SmartEditExpensesActivity : LoggedInActivity() {
    private lateinit var binding: ActivitySmartEditExpensesBinding
    private var selectedImageUri: Uri? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var convertedAmount: String? = null
    private var budgetCurrency: String? = null
    private val paymentMethods = arrayOf(
        "ACH Payment",
        "Alipay",
        "American Express",
        "Apple Pay",
        "Bank Transfer",
        "Barter",
        "Bitcoin",
        "Cash",
        "Credit Card",
        "Cryptocurrency",
        "Debit Card",
        "Diners Club",
        "Discover",
        "E-wallet",
        "Gift Card",
        "Google Pay",
        "JCB Card",
        "Layaway",
        "Mastercard",
        "Mobile Payment",
        "Money Order",
        "Money Transfer Service",
        "PayPal",
        "Prepaid Card",
        "Square",
        "Traveler's Check",
        "Venmo",
        "Visa",
        "WeChat Pay",
        "Western Union",
        "Zelle"
    )
    private lateinit var addressListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var budgetDetailsID: String
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartEditExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storage = FirebaseStorage.getInstance()

        firestore = FirebaseFirestore.getInstance()
        binding.toolbarEditExpenses.setNavigationOnClickListener {
            val budgetId = intent.getStringExtra("budgetId")
            val intent = Intent(this, BottomNavigationActivity::class.java).apply {
                putExtra("returnToSmartBudgetFragment", true)
                putExtra("budgetId", budgetId)
            }
            startActivity(intent)
        }
        budgetDetailsID = intent.getStringExtra("BudgetDetailsID") ?: ""
        setDecimalInputFilter(binding.textViewEditExpensesAmount)

        fetchAndPopulateData()

        binding.imageViewEditCategory.setOnClickListener {

            val budgetId = intent.getStringExtra("budgetId")
            val intent = Intent(this, SmartPickCategoryActivity::class.java)
            intent.putExtra("budgetId", budgetId)
            startActivity(intent)
        }
        binding. editTextEditPaymentMethod.setOnClickListener {
            showPaymentMethodDialog()
        }
        binding.editTextEditExpensesDate.setOnClickListener {
            val budgetId = intent.getStringExtra("budgetId")
            if (budgetId != null) {
                showDatePickerDialog(budgetId)
            }
        }

        binding.editTextEditExpensesLocation.setOnClickListener {
            showAddressSelectionDialog()
        }

        binding.editTextEditExpensesPhoto.setOnClickListener{
            selectImageFromGalleryOrCamera()
        }

        binding.buttonSaveEditExpenses.setOnClickListener{
            saveSmartBudget()
        }

        binding.crossExpenses.setOnClickListener {

            binding.imageViewEditPhoto.setImageURI(null)
            binding.imageViewEditPhoto.visibility = View.GONE
            binding.crossExpenses.visibility = View.GONE

            binding.editTextEditExpensesPhoto.setText("")
        }
        binding.textViewEditExpensesCurrency.setOnClickListener{
            showCurrencyDialog(binding.textViewEditExpensesCurrency, this)
        }
        binding.delete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete") { dialog, _ ->
                    dialog.dismiss()
                    deleteSmartBudgetDetails(budgetDetailsID)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }


    }
    private fun deleteSmartBudgetDetails(budgetDetailsID: String) {
        val imageUrl = binding.editTextEditExpensesPhoto.text.toString()

        firestore.collection("SmartBudgetDetails").document(budgetDetailsID)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Expense details deleted successfully", Toast.LENGTH_SHORT).show()


                if (!imageUrl.isNullOrEmpty()) {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                    storageRef.delete()
                        .addOnSuccessListener {
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Failed to delete expense image", Toast.LENGTH_SHORT).show()
                        }
                }

                val budgetId = intent.getStringExtra("budgetId")
                val intent = Intent(this, BottomNavigationActivity::class.java).apply {
                    putExtra("returnToSmartBudgetFragment", true)
                    putExtra("budgetId", budgetId)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to delete expense details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getBudgetCurrency(): String? {
        return budgetCurrency
    }
    fun getConvertedAmount(): String? {
        return convertedAmount
    }

    private fun saveSmartBudget() {

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





        val expensesNotes = binding.editTextEditExpensesNotes.text.toString().trim()
        val expensesDate = binding.editTextEditExpensesDate.text.toString().trim()
        val expensesAmount = binding.textViewEditExpensesAmount.text.toString().trim()
        val expensesPaymentMethod= binding.editTextEditPaymentMethod.text.toString().trim()
        val calculateExpensesAmount = expensesAmount.toDoubleOrNull() ?: 0.0


        if (expensesNotes.isEmpty()) {
            binding.editTextEditExpensesNotes.error = "Trip name cannot be empty"
            return
        }
        if (expensesDate.isEmpty()) {
            binding.editTextEditExpensesDate.error = "Currency cannot be empty"
            return
        }
        if (expensesAmount.isEmpty()) {
            binding.textViewEditExpensesAmount.text = Editable.Factory.getInstance().newEditable("0.00")
        }


        val expensesCurrency = binding.textViewEditExpensesCurrency.text.toString()

        val expensesToUsdExchangeRate = exchangeRates[expensesCurrency] ?: 1.0


        val amountInUSD = calculateExpensesAmount * expensesToUsdExchangeRate

        val budgetId = intent.getStringExtra("budgetId") ?: ""

        firestore.collection("SmartBudget").document(budgetId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    budgetCurrency = document.getString("BudgetCurrency").toString()

                    val usdToBudgetExchangeRate = exchangeRates[budgetCurrency] ?: 1.0



                    convertedAmount = String.format("%.2f", amountInUSD / usdToBudgetExchangeRate)

                    saveSmartBudgetDetails(
                        budgetId,
                        expensesNotes,
                        expensesAmount,
                        expensesDate,
                        expensesPaymentMethod
                    )

                }
            }
            .addOnFailureListener { exception ->
            }




    }

    private fun saveSmartBudgetDetails(
        budgetId: String,
        expensesNotes: String,
        expensesAmount: String,
        expensesDate: String,
        expensesPaymentMethod: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val randomString = generateRandomString()
        val storageRef = storage.reference
        val imageRef = storageRef.child("SmartBudgetDetails").child("$userId/$budgetId/$randomString.jpg")

        val selectedImageUri = selectedImageUri

        if (selectedImageUri != null ) {
            val uploadTask = imageRef.putFile(selectedImageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }

                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result


                    val smartBudgetData = hashMapOf(
                        "BudgetDetailsID" to budgetDetailsID,
                        "BudgetID" to budgetId,
                        "ExpensesNotes" to expensesNotes,
                        "ExpensesCurrency" to binding.textViewEditExpensesCurrency.text.toString(),
                        "ExpensesAmount" to expensesAmount,
                        "ConvertedCurrency" to budgetCurrency,
                        "ConvertedAmount" to convertedAmount,
                        "ExpensesDate" to expensesDate,
                        "ExpensesLocation" to binding.editTextEditExpensesLocation.text.toString(),
                        "ExpensesImage" to downloadUri.toString(),
                        "ExpensesCategory" to category,
                        "ExpensesPaymentMethod" to expensesPaymentMethod
                    )


                    firestore.collection("SmartBudgetDetails").document(budgetDetailsID)
                        .update(smartBudgetData as Map<String, Any>)
                        .addOnSuccessListener {
                            updateSmartBudgetTotalAmount(budgetId)
                            Toast.makeText(
                                this,
                                "Updated  successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, BottomNavigationActivity::class.java).apply {
                                putExtra("navigateToSmartBudgetFragment", true)
                                putExtra("budgetId", budgetId)
                            }
                            startActivity(intent)
                        }
                        .addOnFailureListener { exception ->

                        }
                } else {

                }
            }
        }else if (binding.editTextEditExpensesPhoto.text != null) {

            val smartBudgetData = hashMapOf(
                "BudgetDetailsID" to budgetDetailsID,
                "BudgetID" to budgetId,
                "ExpensesNotes" to expensesNotes,
                "ExpensesCurrency" to binding.textViewEditExpensesCurrency.text.toString(),
                "ExpensesAmount" to expensesAmount,
                "ConvertedCurrency" to  budgetCurrency,
                "ConvertedAmount" to convertedAmount,
                "ExpensesDate" to expensesDate,
                "ExpensesLocation" to binding.editTextEditExpensesLocation.text.toString(),
                "ExpensesImage" to binding.editTextEditExpensesPhoto.text.toString(),
                "ExpensesCategory" to category,
                "ExpensesPaymentMethod" to expensesPaymentMethod
            )

            firestore.collection("SmartBudgetDetails").document(budgetDetailsID)
                .update(smartBudgetData as Map<String, Any>)
                .addOnSuccessListener {
                    updateSmartBudgetTotalAmount(budgetId)
                    Toast.makeText(
                        this,
                        "Updated  successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, BottomNavigationActivity::class.java).apply {
                        putExtra("navigateToSmartBudgetFragment", true)
                        putExtra("budgetId", budgetId)
                    }
                    startActivity(intent)
                }
                .addOnFailureListener { exception ->

                }
        } else {

            val smartBudgetData = hashMapOf(
                "BudgetDetailsID" to budgetDetailsID,
                "BudgetID" to budgetId,
                "ExpensesNotes" to expensesNotes,
                "ExpensesCurrency" to binding.textViewEditExpensesCurrency.text.toString(),
                "ExpensesAmount" to expensesAmount,
                "ConvertedCurrency" to  budgetCurrency,

                "ConvertedAmount" to convertedAmount,
                "ExpensesDate" to expensesDate,
                "ExpensesLocation" to binding.editTextEditExpensesLocation.text.toString(),
                "ExpensesImage" to null,
                "ExpensesCategory" to category,
                "ExpensesPaymentMethod" to expensesPaymentMethod
            )

            firestore.collection("SmartBudgetDetails").document(budgetDetailsID)
                .update(smartBudgetData as Map<String, Any>)
                .addOnSuccessListener {
                    updateSmartBudgetTotalAmount(budgetId)
                    Toast.makeText(
                        this,
                        "Updated  successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this, BottomNavigationActivity::class.java).apply {
                        putExtra("navigateToSmartBudgetFragment", true)
                        putExtra("budgetId", budgetId)
                    }
                    startActivity(intent)
                }
                .addOnFailureListener { exception ->
                }
        }

    }

    private fun updateSmartBudgetTotalAmount(budgetId: String) {
        calculateTotalAmountFromDetails(budgetId) { totalAmount ->
            val formattedTotalAmount = String.format("%.2f", totalAmount)
            val newUpdateData = mapOf(
                "TotalExpensesAmount" to formattedTotalAmount,
                "TotalExpensesAmountCurrency" to budgetCurrency
            )

            val smartBudgetDocRef = firestore.collection("SmartBudget").document(budgetId)
            smartBudgetDocRef.update(newUpdateData)
                .addOnSuccessListener {

                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to update total amount: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun calculateTotalAmountFromDetails(budgetId: String, callback: (Double) -> Unit) {
        var totalAmount = 0.00

        firestore.collection("SmartBudgetDetails")
            .whereEqualTo("BudgetID", budgetId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val expensesAmountString = document.getString("ConvertedAmount")
                    val expensesAmount = expensesAmountString?.toDoubleOrNull() ?: 0.00
                    totalAmount += expensesAmount
                }
                callback(totalAmount)
            }
            .addOnFailureListener { exception ->

                callback(totalAmount)
            }
    }





    private fun fetchAndPopulateData() {
        firestore.collection("SmartBudgetDetails")
            .document(budgetDetailsID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.toObject(SmartBudgetDetails::class.java)
                    data?.let { smartBudgetDetails ->
                        smartBudgetDetails.ExpensesAmount.let { binding.textViewEditExpensesAmount.setText(it) }
                        smartBudgetDetails.ExpensesNotes.let { binding.editTextEditExpensesNotes.setText(it) }
                        smartBudgetDetails.ExpensesDate.let { binding.editTextEditExpensesDate.setText(it) }
                        smartBudgetDetails.ExpensesPaymentMethod.let { binding.editTextEditPaymentMethod.setText(it) }
                        smartBudgetDetails.ExpensesLocation.let { binding.editTextEditExpensesLocation.setText(it) }
                        smartBudgetDetails.ExpensesCurrency.let { binding.textViewEditExpensesCurrency.setText(it) }
                        if (!smartBudgetDetails.ExpensesImage.isNullOrEmpty()){
                            binding.editTextEditExpensesPhoto.setText(smartBudgetDetails.ExpensesImage)

                            val imageUrl = smartBudgetDetails.ExpensesImage
                            Glide.with(this@SmartEditExpensesActivity)
                                .load(imageUrl)
                                .into(binding.imageViewEditPhoto)

                            binding.imageViewEditPhoto.visibility = View.VISIBLE
                            binding.crossExpenses.visibility = View.VISIBLE
                        }

                        category = smartBudgetDetails.ExpensesCategory
                        if (category!!.isNotEmpty()) {
                            val drawableResId = getDrawableResourceId(category!!)
                            val categoryColor = getColorFromCategory(category!!)

                            if (drawableResId != -1) {
                                val drawable = ContextCompat.getDrawable(this@SmartEditExpensesActivity, drawableResId)

                                drawable?.let {
                                    binding.imageViewEditCategory.setImageDrawable(drawable)
                                    binding.imageViewEditCategory.setColorFilter(categoryColor, android.graphics.PorterDuff.Mode.SRC_ATOP)
                                    binding.textViewEditExpensesAmount.setTextColor(categoryColor)
                                    binding.textViewEditExpensesCurrency.setBackgroundColor(categoryColor)
                                }


                                (binding.root.background as? LayerDrawable)?.let { background ->
                                    val shape = background.findDrawableByLayerId(R.id.rectangle_shape) as? GradientDrawable
                                    shape?.setStroke(2.dpToPx(this@SmartEditExpensesActivity), categoryColor)
                                }
                            }
                        }
                    }
                } else {
                }
            }
            .addOnFailureListener { exception ->
            }
    }


    private fun Int.dpToPx(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
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
    private fun getDrawableResourceId(category: String): Int {
        return when (category) {
            "Transportation" -> R.drawable.baseline_directions_transit_24
            "Restaurants" -> R.drawable.baseline_restaurant_menu_24
            "Accommodation" -> R.drawable.baseline_single_bed_24
            "Groceries" -> R.drawable.baseline_local_grocery_store_24
            "Shopping" -> R.drawable.baseline_shopping_bag_24
            "Activities" -> R.drawable.baseline_kayaking_24
            "Drinks" -> R.drawable.baseline_wine_bar_24
            "Coffee" -> R.drawable.baseline_coffee_24
            "Flights" -> R.drawable.baseline_flight_24
            "Fees & Charges" -> R.drawable.baseline_monetization_on_24
            "Sightseeing" -> R.drawable.baseline_museum_24
            "Entertainment" -> R.drawable.baseline_movie_24
            "Laundry" -> R.drawable.baseline_local_laundry_service_24
            "Exchange Fees" -> R.drawable.baseline_currency_exchange_24
            "Others" -> R.drawable.baseline_other_houses_24
            else -> -1
        }
    }

    private fun getColorFromCategory(category: String): Int {
        return when (category) {
            "Transportation" -> Color.parseColor("#FF5722")
            "Restaurants" -> Color.parseColor("#FF9800")
            "Accommodation" -> Color.parseColor("#FFC107")
            "Groceries" -> Color.parseColor("#FFEB3B")
            "Shopping" -> Color.parseColor("#CDDC39")
            "Activities" -> Color.parseColor("#4CAF50")
            "Drinks" -> Color.parseColor("#009688")
            "Coffee" -> Color.parseColor("#00BCD4")
            "Flights" -> Color.parseColor("#03A9F4")
            "Fees & Charges" -> Color.parseColor("#2196F3")
            "Sightseeing" -> Color.parseColor("#3F51B5")
            "Entertainment" -> Color.parseColor("#673AB7")
            "Laundry" -> Color.parseColor("#9C27B0")
            "Exchange Fees" -> Color.parseColor("#E91E63")
            "Others" -> Color.parseColor("#F44336")
            else -> Color.parseColor("#000000")
        }
    }
    private fun showCurrencyDialog(currency: TextView, context: Context) {


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
    private fun showPaymentMethodDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Payment Method")
        builder.setItems(paymentMethods) { dialog, which ->
            val selectedPaymentMethod = paymentMethods[which]
            binding.editTextEditPaymentMethod.setText(selectedPaymentMethod)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }


    private fun showDatePickerDialog(budgetId: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        firestore.collection("SmartBudget").document(budgetId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val startDateString = document.getString("TripStartDate")
                    val endDateString = document.getString("TripEndDate")

                    if (!startDateString.isNullOrEmpty() && !endDateString.isNullOrEmpty()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val startDate = sdf.parse(startDateString)
                        val endDate = sdf.parse(endDateString)

                        startDate?.let { start ->
                            endDate?.let { end ->
                                val datePickerDialog = DatePickerDialog(
                                    this,
                                    { _, year, monthOfYear, dayOfMonth ->
                                        val selectedDateCalendar = Calendar.getInstance()
                                        selectedDateCalendar.set(year, monthOfYear, dayOfMonth)
                                        val selectedDateTime = selectedDateCalendar.timeInMillis

                                        if (selectedDateTime in start.time..end.time) {
                                            val selectedDate = sdf.format(selectedDateCalendar.time)
                                            binding.editTextEditExpensesDate.setText(selectedDate)
                                        } else {

                                            Toast.makeText(this, "Please select a date within the range", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    year,
                                    month,
                                    dayOfMonth
                                )

                                startDate.time?.let { datePickerDialog.datePicker.minDate = it }
                                endDate.time?.let { datePickerDialog.datePicker.maxDate = it }

                                datePickerDialog.show()
                            }
                        }
                    }
                } else {
                }
            }
            .addOnFailureListener { exception ->
            }
    }



    private fun showAddressSelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_address_select, null)
        addressListView = dialogView.findViewById(R.id.addressListView)
        val addressEditText: EditText = dialogView.findViewById(R.id.addressEditText)
        val searchButton: ImageView = dialogView.findViewById(R.id.searchButton)

        searchButton.setOnClickListener {
            val query = addressEditText.text.toString()
            if (query.isNotBlank()) {
                SearchLocationTask().execute(query)
            } else {
                Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show()
            }
        }

        val alertDialogBuilder = AlertDialog.Builder(this)
        val dialog = alertDialogBuilder.setView(dialogView).create()

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Clear") { _, _ ->
            binding.editTextEditExpensesLocation.setText("")
            binding.editTextEditExpensesLocation.compoundDrawables.forEach { drawable ->
                drawable?.setTint(resources.getColor(R.color.dark_blue))

            }
        }
        dialog.show()

        addressListView.setOnItemClickListener { _, _, position, _ ->
            val selectedAddress = adapter.getItem(position)
            binding.editTextEditExpensesLocation.setText(selectedAddress)
            dialog.dismiss()
        }


    }
    private fun generateRandomString(length: Int = 10): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
    private inner class SearchLocationTask : AsyncTask<String, Void, ArrayList<String>>() {
        override fun doInBackground(vararg params: String?): ArrayList<String> {
            val query = params[0]
            val apiUrl = "https://nominatim.openstreetmap.org/search?q=$query&format=json"

            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            val response = StringBuilder()

            try {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
            } finally {
                connection.disconnect()
            }

            val addresses = ArrayList<String>()
            if (response.isNotEmpty()) {
                try {
                    val jsonArray = JSONArray(response.toString())
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val address = jsonObject.getString("display_name")
                        addresses.add(address)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return addresses
        }


        override fun onPostExecute(result: ArrayList<String>?) {
            super.onPostExecute(result)
            result?.let {
                adapter = ArrayAdapter(
                    this@SmartEditExpensesActivity,
                    android.R.layout.simple_list_item_1,
                    it
                )
                addressListView.adapter = adapter


                binding.editTextEditExpensesLocation.setTextColor(resources.getColor(R.color.blue))

                binding.editTextEditExpensesLocation.compoundDrawables.forEach { drawable ->
                    drawable?.setTint(resources.getColor(R.color.blue))
                }
            }
        }


    }




    private fun selectImageFromGalleryOrCamera() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Add Photo")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> openCamera()
                options[item] == "Choose from Gallery" -> openGallery()
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun getImageUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile("Title", ".jpg", imagesDir)
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            Uri.fromFile(imageFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val maxWidth = 1024
        val maxHeight = 1024

        val width = bitmap.width
        val height = bitmap.height


        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio: Float = width.toFloat() / height.toFloat()

        val finalWidth: Int
        val finalHeight: Int
        if (width > height) {
            finalWidth = minOf(width, maxWidth)
            finalHeight = (finalWidth / ratio).toInt()
        } else {
            finalHeight = minOf(height, maxHeight)
            finalWidth = (finalHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }






    private fun writeUriToTextField(uri: Uri) {
        binding.editTextEditExpensesPhoto.setText(uri.toString())


    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val photo: Bitmap? = it.extras?.get("data") as? Bitmap
                photo?.let { bitmap ->
                    val resizedBitmap = resizeBitmap(bitmap)
                    val selectedImageUri = getImageUri(this, resizedBitmap)
                    selectedImageUri?.let { uri ->

                        binding.imageViewEditPhoto.setImageBitmap(resizedBitmap)
                        binding.imageViewEditPhoto.visibility = View.VISIBLE
                        binding.crossExpenses.visibility = View.VISIBLE
                        this.selectedImageUri = uri
                        writeUriToTextField(uri)
                    }
                }
            }
        }
    }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let { data: Intent ->
                val uri = data.data
                uri?.let { selectedImageUri = it
                    val inputStream = this.contentResolver.openInputStream(uri)
                    inputStream?.use { stream: InputStream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        val resizedBitmap = resizeBitmap(bitmap)
                        selectedImageUri = getImageUri(this, resizedBitmap)
                        binding.imageViewEditPhoto.setImageBitmap(resizedBitmap)
                        binding.imageViewEditPhoto.visibility = View.VISIBLE
                        binding.crossExpenses.visibility = View.VISIBLE
                        writeUriToTextField(uri)
                    }
                }
            }
        }
    }


}