package com.example.travel_itinerary_planner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.travel_itinerary_planner.databinding.FragmentDrawerBinding
import com.example.travel_itinerary_planner.login_register_reset.LoginActivity
import com.example.travel_itinerary_planner.social.BookmarksActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class DrawerFragment : Fragment() {
    private var _binding: FragmentDrawerBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bottomNavigationView = view?.findViewById<BottomNavigationView>(R.id.nav_view)

        val menu = bottomNavigationView?.menu
        if (menu != null) {
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                if (menuItem.itemId == R.id.navigation_profile) {
                    menuItem.isChecked = true
                    break
                }
            }
        }
        _binding = FragmentDrawerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        binding.toolbarSettings.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.navigationPasswordSecurity.setOnClickListener {
            val intent = Intent(requireContext(), PasswordSecurityActivity::class.java)
            startActivity(intent)
        }
        binding.navigationPersonalDetails.setOnClickListener {
            val intent = Intent(requireContext(), PersonalDetailsActivity::class.java)
            startActivity(intent)
        }

        binding.navigationBookmarks.setOnClickListener {
            val intent = Intent(requireContext(), BookmarksActivity::class.java)
            startActivity(intent)
        }
        binding.navigationNotifications.setOnClickListener {
            val intent = Intent(requireContext(), NotificationsActivity::class.java)
            startActivity(intent)
        }
        binding.navigationHelpCenter.setOnClickListener {
            val intent = Intent(requireContext(), HelpCenterActivity::class.java)
            startActivity(intent)
        }
        binding.navigationLogout.setOnClickListener {
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
