package com.example.travel_itinerary_planner

import android.content.Intent
import android.os.Bundle
import com.example.travel_itinerary_planner.databinding.ActivityMainBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.login_register_reset.LoginActivity
import com.example.travel_itinerary_planner.notification.NotificationDetailActivity

class MainActivity : LoggedInActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkIntent(intent)
        binding.startBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
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

}