package com.example.travel_itinerary_planner

import android.content.Intent
import android.os.Bundle
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.login_register_reset.LoginActivity
import com.example.travel_itinerary_planner.notification.NotificationDetailActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : LoggedInActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkIntent(intent)

    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }
    private fun checkIntent(intent: Intent) {
        intent.getStringExtra("notification_action")?.let { action ->
            if (action == "OPEN_DETAIL_ACTIVITY") {
                val detailIntent = Intent(this, NotificationDetailActivity::class.java).apply {
                    putExtra("notification_title", intent.getStringExtra("notification_title"))
                    putExtra("notification_body", intent.getStringExtra("notification_body"))
                }
                startActivity(detailIntent)
                finish()
            }
        }
    }
    private fun handleIntent(intent: Intent) {
        intent.extras?.let {
            val notificationAction = it.getString("notification_action")
            if ("OPEN_DETAIL_ACTIVITY" == notificationAction) {
                val detailIntent = Intent(this, NotificationDetailActivity::class.java).apply {
                    putExtras(it)
                }
                startActivity(detailIntent)
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser != null) {
            setupHomeFragment()
        }


    }
    private fun setupHomeFragment() {
        val intent = Intent(this, BottomNavigationActivity::class.java)
        intent.putExtra("returnToHomeFragment", true)
        startActivity(intent)
    }
}