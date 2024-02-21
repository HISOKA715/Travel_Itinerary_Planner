package com.example.travel_itinerary_planner

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.travel_itinerary_planner.databinding.FragmentDrawerBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.login_register_reset.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class DrawerFragment : LoggedInFragment() {
    private var _binding: FragmentDrawerBinding? = null
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrawerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)


        val menu = bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            if (menuItem.itemId == R.id.navigation_profile) {
                menuItem.isChecked = true
                break
            }
        }
        binding.toolbarSettings.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.navigationPasswordSecurity.setOnClickListener {
            val intent = Intent(context, PasswordSecurityActivity::class.java)
            startActivity(intent)
        }
        binding.navigationPersonalDetails.setOnClickListener {
            val intent = Intent(context, PersonalDetailsActivity::class.java)
            startActivity(intent)
        }

        binding.navigationBookmarks.setOnClickListener {
            val intent = Intent(context, BookmarksActivity::class.java)
            startActivity(intent)
        }
        binding.navigationNotifications.setOnClickListener {
            val intent = Intent(context, NotificationsActivity::class.java)
            startActivity(intent)
        }
        binding.navigationHelpCenter.setOnClickListener {
            val intent = Intent(context, HelpCenterActivity::class.java)
            startActivity(intent)
        }
        binding.navigationLogout.setOnClickListener{
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }




        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

