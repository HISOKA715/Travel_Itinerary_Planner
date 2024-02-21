package com.example.travel_itinerary_planner.social

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentPostDetailsBinding
import com.example.travel_itinerary_planner.profile.ProfileViewModel
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostDetailsFragment : Fragment() {
    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var socialPostAdapter: SocialPostAdapter
    private val viewModel by viewModels<ProfileViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
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
        binding.toolbarPostDetails.setNavigationOnClickListener {
            val intent = Intent(requireContext(), BottomNavigationActivity::class.java)
            intent.putExtra("returnToProfileFragment", true)
            startActivity(intent)
            requireActivity().finish()
        }

        socialPostAdapter = SocialPostAdapter()
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPosts.adapter = socialPostAdapter

        fetchSocialPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchSocialPosts() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            val postsRef = firestore.collection("SocialMedia")
                .whereEqualTo("UserID", currentUserUid)

            postsRef.get()
                .addOnSuccessListener { documents ->
                    val socialPostsList = mutableListOf<SocialMediaPost>()
                    val userDataMap = mutableMapOf<String, UserData?>()

                    for (document in documents) {
                        val socialPost = document.toObject(SocialMediaPost::class.java)
                        socialPostsList.add(socialPost)

                        fetchUserData(socialPost.UserID) { userData ->
                            userDataMap[socialPost.UserID] = userData
                            socialPostAdapter.submitList(socialPostsList, userDataMap)
                        }
                    }


                }
                .addOnFailureListener { exception ->
                }
        }
    }

    private fun fetchUserData(userId: String, callback: (UserData?) -> Unit) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(UserData::class.java)
                    callback(userData)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }
}




