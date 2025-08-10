package com.example.mahalapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mahalapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Fragment showing the info categories menu
class InfoCategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout for the fragment
        return inflater.inflate(R.layout.fragment_info_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        // Reference to the Welcome TextView
        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)

        // Get user's name from Firebase Firestore
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("fullName") ?: "User"
                        welcomeText.text = "Welcome, $name"
                    } else {
                        welcomeText.text = "Welcome, User"
                    }
                }
                .addOnFailureListener {
                    welcomeText.text = "Welcome, User"
                }
        }

        // Button click listeners for navigating to each category
        view.findViewById<MaterialCardView>(R.id.btnHealth).setOnClickListener {
            navController.navigate(
                InfoCategoriesFragmentDirections.actionInfoCategoriesToInfoContent("health")
            )
        }

        view.findViewById<MaterialCardView>(R.id.btnHousing).setOnClickListener {
            navController.navigate(
                InfoCategoriesFragmentDirections.actionInfoCategoriesToInfoContent("housing")
            )
        }

        view.findViewById<MaterialCardView>(R.id.btnRights).setOnClickListener {
            navController.navigate(
                InfoCategoriesFragmentDirections.actionInfoCategoriesToInfoContent("rights")
            )
        }

        view.findViewById<MaterialCardView>(R.id.btnAliyah).setOnClickListener {
            navController.navigate(
                InfoCategoriesFragmentDirections.actionInfoCategoriesToInfoContent("aliyah")
            )
        }
    }
}
