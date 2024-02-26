package com.example.travel_itinerary_planner.social

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentOthersProfileBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.profile.SocialMediaPost
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OthersProfileFragment : LoggedInFragment(), OthersProfileAdapter.OnItemClickListener {

    private val binding get() = _binding!!
    private var _binding: FragmentOthersProfileBinding? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var othersProfileAdapter: OthersProfileAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOthersProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)


        val menu = bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            if (menuItem.itemId == R.id.navigation_social) {
                menuItem.isChecked = true
                break
            }
        }
        firestore = FirebaseFirestore.getInstance()

        othersProfileAdapter = OthersProfileAdapter(this)
        binding.recycleViewOthersProfile.apply {
            val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            binding.recycleViewOthersProfile.layoutManager = layoutManager
            binding.recycleViewOthersProfile.adapter = othersProfileAdapter


        }
        binding.toolbarOthersProfile.setNavigationOnClickListener {
            val intent = Intent(requireContext(), BottomNavigationActivity::class.java)
            intent.putExtra("returnToSocialFragment", true)
            startActivity(intent)
            requireActivity().finish()
        }
        fetchSocialMediaPosts()

        fetchProfilePictureFromFirestore()

        val user = arguments?.getString("userId")

        val userId= user ?: requireActivity().intent.getStringExtra("userId")
        userId?.let { fetchUserData(it) }
    }

    private fun fetchSocialMediaPosts() {
        val user = arguments?.getString("userId")

        val userId= user ?: requireActivity().intent.getStringExtra("userId")
        if (userId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("SocialMedia")
                .whereEqualTo("UserID", userId)
                .whereEqualTo("SocialSharingOptions", "Public")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val socialMediaPosts = mutableListOf<SocialMediaPost>()
                    for (document in querySnapshot.documents) {
                        val post = document.toObject(SocialMediaPost::class.java)
                        post?.let {
                            socialMediaPosts.add(0,it)
                        }
                    }
                    othersProfileAdapter.submitList(socialMediaPosts)
                }
                .addOnFailureListener { e ->

                }
        } else {

        }
    }
    private fun fetchProfilePictureFromFirestore() {
        val user = arguments?.getString("userId")

        val userId= user ?: requireActivity().intent.getStringExtra("userId")
        userId?.let { uid ->
            val firestore = FirebaseFirestore.getInstance()
            val userRef = firestore.collection("users").document(uid)

            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val profilePictureUri = documentSnapshot.getString("ProfileImage")
                        profilePictureUri?.let { uri ->
                            Glide.with(this)
                                .load(uri)
                                .override(300, 300)
                                .error(R.drawable.travel_main)
                                .centerCrop()
                                .into(binding.userOthersProfile)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to fetch profile picture", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchUserData(userId: String) {
        val userRef = firestore.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userData = document.data
                    userData?.let { populateEditTextFields(it) }
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun populateEditTextFields(userData: Map<String, Any>) {
        binding.apply {
            val username = userData["Username"].toString()
            othersUsername.text = username
        }
    }

    override fun onItemClick(socialMediaPost: SocialMediaPost, userId: String) {
        val position = othersProfileAdapter.getSocialMediaPosts().indexOf(socialMediaPost)

        val user = arguments?.getString("userId")

        val userId= user ?: requireActivity().intent.getStringExtra("userId")
        val bundle = Bundle().apply {
            putInt("position", position)
            putString("userId", userId)

        }

        val othersPostDetailsFragment = OthersPostDetailsFragment()
        othersPostDetailsFragment.arguments = bundle

        val intent = Intent(view?.context, BottomNavigationActivity::class.java)
        intent.putExtra("navigateToOthersPostDetailsFragment", true)
        intent.putExtra("userId", userId)
        intent.putExtra("position", position)
        view?.context?.startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
