package com.example.travel_itinerary_planner.tourism_attraction
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.TourismAttractionBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint

class TourismActivity : LoggedInActivity(),OnMapReadyCallback {
    private lateinit var binding:TourismAttractionBinding
    private lateinit var reviewsAdapter: ReviewAdapter
    private var isFavorite = false
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var tourismCategory: String? = null
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var geoPoint: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TourismAttractionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        reviewsAdapter = ReviewAdapter(this, mutableListOf())
        binding.listReview.adapter = reviewsAdapter
        val documentId = intent.getStringExtra("documentId")
        val imageButton: ImageButton = binding.imageButton2
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        imageButton.setOnClickListener {
            val intent = Intent(this, ReviewDetailActivity::class.java).apply {
                putExtra("documentId", documentId)
            }
            startActivity(intent)
        }

        if (documentId != null) {
            fetchReviewsWithUserDetails(documentId)
            fetchAndCalculateReviews(documentId)
            checkUserLike(documentId,userId)
            fetchTourismAttraction(documentId)
        }

        binding.textView20.post {

            if (isTextViewTruncated(binding.textView20)) {
                binding.viewmore.visibility = View.VISIBLE
            } else {
                binding.viewmore.visibility = View.GONE
            }
        }
        var isExpanded = false
        binding.viewmore.setOnClickListener {
            toggleTextExpansion()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

        setUtilitiesSelected()
        binding.textView16.setOnClickListener {
            setUtilitiesSelected()
        }

        binding.textView17.setOnClickListener {
            setReviewSelected()
        }
        onBackPressedDispatcher.addCallback(this, callback)
        binding.imageButtonSearch1.setOnClickListener {
            callback.handleOnBackPressed()
        }


        binding.imageButtonSearch.setOnClickListener {
            if (documentId != null && userId != null) {
                toggleLike(documentId, userId)
            }else{
                Log.d("haha","cannot find the id")
            }
        }
        binding.imageButtonSearch2.setOnClickListener {
            if (documentId != null && userId != null) {
                updateUserPreference(-100)
                Toast.makeText(this, "Preferences will be reduce", Toast.LENGTH_SHORT).show()
                finish()}
            else{
                Log.d("TourismActivity", "Cannot find the document ID or user ID")
            }
        }

        binding.viewmore.setOnClickListener {
            if (isExpanded) {
                binding.textView20.maxLines = 3
                binding.viewmore.text = getString(R.string.view_more)
            } else {
                binding.textView20.maxLines = Integer.MAX_VALUE
                binding.viewmore.text = getString(R.string.view_less)
            }
            isExpanded = !isExpanded
        }


    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        geoPoint?.let {
            val location = LatLng(it.latitude, it.longitude)
            googleMap?.addMarker(MarkerOptions().position(location).title("Attraction Location"))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }




    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    private fun updateReviewsUI(reviews: List<Review>) {
        reviewsAdapter = ReviewAdapter(this, reviews)
        binding.listReview.adapter = reviewsAdapter
        reviewsAdapter.notifyDataSetChanged()
        binding.listReview.post {
            setListViewHeightBasedOnChildren(binding.listReview)
        }
    }
    private fun fetchReviewsWithUserDetails(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").document(documentId).collection("Review")
            .orderBy("RateDate", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { reviewsSnapshot ->
                val reviews = mutableListOf<Review>()
                val userDetailsFetches = mutableListOf<Task<DocumentSnapshot>>()

                for (reviewDoc in reviewsSnapshot) {
                    val userId = reviewDoc.getString("UserID") ?: continue
                    val rateDesc = reviewDoc.getString("RateDesc") ?: ""
                    val rateNo = reviewDoc.getString("RateNo")?: ""
                    val rateDate = reviewDoc.getTimestamp("RateDate")?.toDate()?.let { date ->
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                    } ?: ""


                    val userDetailFetch = db.collection("users").document(userId).get()
                    userDetailsFetches.add(userDetailFetch)

                    userDetailFetch.addOnSuccessListener { userDoc ->
                        val username = userDoc.getString("Name") ?: "Anonymous"
                        val profileImage = userDoc.getString("ProfileImage") ?: ""
                        synchronized(reviews) {
                            reviews.add(Review(username, profileImage, rateNo, rateDate, rateDesc))
                        }
                    }
                }
                Tasks.whenAllComplete(userDetailsFetches).addOnCompleteListener {
                    Log.d("TourismActivity", "Total reviews fetched: ${reviews.size}")
                    updateReviewsUI(reviews)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TourismActivity", "Error fetching reviews with user details", e)
            }
    }

    private fun checkUserLike(documentId: String,userId:String?) {

        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").document(documentId)
            .collection("like").document(userId!!).get()
            .addOnSuccessListener { document ->
                isFavorite = document.exists()
                updateFavoriteIcon()
            }
    }

    private fun toggleLike(documentId: String, userId: String) {
        val db = FirebaseFirestore.getInstance()
        val likeRef = db.collection("Tourism Attractions").document(documentId)
            .collection("like").document(userId)

        if (isFavorite) {
            // Remove like
            likeRef.delete().addOnSuccessListener {
                isFavorite = false
                updateFavoriteIcon()
                Log.d("toggleLike", "Like removed successfully")
                // Optionally, you could subtract 100 from the user's preference here
            }.addOnFailureListener { e ->
                Log.e("toggleLike", "Error removing like", e)
            }
        } else {

            val likeData = hashMapOf(
                "number" to 1,
                "timestamp" to Timestamp.now(),
            )
            likeRef.set(likeData).addOnSuccessListener {
                isFavorite = true
                updateFavoriteIcon()
                Log.d("toggleLike", "Like added successfully")
                updateUserPreference(100)
            }.addOnFailureListener { e ->
                Log.e("toggleLike", "Error adding like", e)
            }
        }
    }

    private fun fetchAndCalculateReviews(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").document(documentId).collection("Review")
            .get()
            .addOnSuccessListener { result ->
                val starCounts = IntArray(5)
                var reviewCount = 0

                for (document in result) {
                    val ratingString = document.getString("RateNo")
                    val rating = ratingString?.toDoubleOrNull()?.let { Math.round(it).toInt() }
                    if (rating != null && rating in 1..5) {
                        starCounts[rating - 1]++ // Increment the count for the rounded rating
                        reviewCount++
                    }
                }

                if (reviewCount > 0) {
                    val percentages = starCounts.map { it * 100 / reviewCount.toDouble() }
                    updateUIWithRatingAndPercentages(percentages, reviewCount)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TourismActivity", "Error fetching reviews", e)
            }
    }

    private fun updateUIWithRatingAndPercentages(percentages: List<Double>, reviewCount: Int) {
        runOnUiThread {

            binding.rate11.progress = percentages[0].toInt()
            binding.rate21.progress = percentages[1].toInt()
            binding.rate31.progress = percentages[2].toInt()
            binding.rate41.progress = percentages[3].toInt()
            binding.rate51.progress = percentages[4].toInt()

            val totalRating = percentages.indices.sumOf { (it + 1) * percentages[it] }
            val averageRating = totalRating / 100.0
            val formattedAverageRating = String.format("%.1f", averageRating)

            binding.ratetext.text = formattedAverageRating
            binding.ratingBar1.rating = averageRating.toFloat()
            binding.totalrate.text = "$reviewCount"
        }
    }

    fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return

        var totalHeight = listView.paddingTop + listView.paddingBottom
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.AT_MOST)

        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            if (listItem != null) {
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                totalHeight += listItem.measuredHeight
            }
        }

        totalHeight += listView.dividerHeight * (listAdapter.count - 1)


        val correctionFactor = 1300 * listAdapter.count
        totalHeight -= correctionFactor
        totalHeight = Math.max(totalHeight, 0)

        val params = listView.layoutParams
        params.height = totalHeight
        listView.layoutParams = params
        listView.requestLayout()

        Log.d("ListViewHeight", "Adjusted total height: $totalHeight")
    }
    private fun fetchTourismAttraction(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        val tourismAttractionRef = db.collection("Tourism Attractions").document(documentId)

        tourismAttractionRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val name = documentSnapshot.getString("TourismName")
                val description = documentSnapshot.getString("TourismDesc")
                val imageUrl = documentSnapshot.getString("TourismImage")
                tourismCategory = documentSnapshot.getString("TourismCategory")
                geoPoint = documentSnapshot.getGeoPoint("point")

                runOnUiThread {
                    binding.textView14.text = name
                    binding.textView20.text = description
                    binding.textView15.text = documentSnapshot.getString("TourismState")
                    Glide.with(this).load(imageUrl).into(binding.imageView2)
                    geoPoint?.let {
                        val location = LatLng(it.latitude, it.longitude)
                        googleMap?.addMarker(MarkerOptions().position(location).title(name))
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))
                    }
                }
                updateTourismPreference()
            } else {
                Log.d("TourismActivity", "Document is null")
            }
        }.addOnFailureListener { e ->
            Log.e("TourismActivity", "Error fetching tourism attraction details", e)
        }
    }

    private fun updateUserPreference(amount: Int) {
        if (userId == null || tourismCategory == null) return

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPreferenceValue = when (tourismCategory) {
                "Shopping" -> snapshot.getDouble("PreferShopping") ?: 0.0
                "Tourism Attraction" -> snapshot.getDouble("PreferAttraction") ?: 0.0
                "Restaurant" -> snapshot.getDouble("PreferRestaurant") ?: 0.0
                "Unknown" -> snapshot.getDouble("PreferOther") ?: 0.0
                else -> 0.0
            }
            val updatedValue = (currentPreferenceValue + amount).coerceIn(1.0, 1000.0)

            when (tourismCategory) {
                "Shopping" -> transaction.update(userRef, "PreferShopping", updatedValue)
                "Tourism Attraction" -> transaction.update(userRef, "PreferAttraction", updatedValue)
                "Restaurant" -> transaction.update(userRef, "PreferRestaurant", updatedValue)
                "Unknown" -> transaction.update(userRef, "PreferOther", updatedValue)
                else -> {}
            }
        }.addOnSuccessListener {
            Log.d("TourismActivity", "User preference updated successfully")
        }.addOnFailureListener { e ->
            Log.e("TourismActivity", "Error updating user preference", e)
        }
    }


    private fun updateTourismPreference() {
        if (userId == null) return

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val newValue = when (tourismCategory) {
                "Shopping" -> (snapshot.getDouble("PreferShopping") ?: 0.0) + 10
                "Tourism Attraction" -> (snapshot.getDouble("PreferAttraction") ?: 0.0) + 10
                "Restaurant" -> (snapshot.getDouble("PreferRestaurant") ?: 0.0) + 10
                "Unknown" -> (snapshot.getDouble("PreferOther") ?: 0.0) + 10
                else -> return@runTransaction
            }.coerceIn(1.0, 1000.0)

            when (tourismCategory) {
                "Shopping" -> transaction.update(userRef, "PreferShopping", newValue)
                "Tourism Attraction" -> transaction.update(userRef, "PreferAttraction", newValue)
                "Restaurant" -> transaction.update(userRef, "PreferRestaurant", newValue)
                "Unknown" -> transaction.update(userRef, "PreferOther", newValue)
            }
        }.addOnSuccessListener {
            Log.d("TourismActivity", "User preference updated successfully")
        }.addOnFailureListener { e ->
            Log.e("TourismActivity", "Error updating user preference", e)
        }
    }


    private fun toggleTextExpansion() {
        with(binding) {
            if (textView20.maxLines == 3) {
                textView20.maxLines = Integer.MAX_VALUE
                viewmore.text = getString(R.string.view_less)
            } else {
                textView20.maxLines = 3
                viewmore.text = getString(R.string.view_more)
            }
        }
    }

    private fun isTextViewTruncated(textView: TextView): Boolean {
        val layout = textView.layout ?: return false
        val lines = layout.lineCount
        if (lines > 0) {
            val ellipsisCount = layout.getEllipsisCount(lines - 1)
            if (ellipsisCount > 0) {
                return true
            }
        }
        return false
    }

    private fun setUtilitiesSelected() {
        binding.textView16.setTextColor(resources.getColor(R.color.black, null))
        binding.textView17.setTextColor(resources.getColor(R.color.grayText, null))

        binding.utilitiesContainer.visibility = View.VISIBLE
        binding.reviewContainer.visibility = View.GONE
    }

    private fun setReviewSelected() {
        binding.textView16.setTextColor(resources.getColor(R.color.grayText, null))
        binding.textView17.setTextColor(resources.getColor(R.color.black, null))

        binding.utilitiesContainer.visibility = View.GONE
        binding.reviewContainer.visibility = View.VISIBLE
    }
    private fun updateFavoriteIcon() {
        binding.imageButtonSearch.setImageResource(
            if (isFavorite) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )
    }


}


