package com.example.travel_itinerary_planner.logged_in

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.travel_itinerary_planner.StartActivity
import com.example.travel_itinerary_planner.login_register_reset.LoginActivity
import com.google.firebase.auth.FirebaseAuth

open class LoggedInFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToStart()
        }
    }

    protected open fun redirectToStart() {
        val intent = Intent(requireContext(), StartActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }


}
