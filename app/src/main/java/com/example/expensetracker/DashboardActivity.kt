package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expensetracker.databinding.ActivityDashboardBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

/**
 * DashboardActivity is the main screen of the app.
 *
 * Responsibilities:
 * - Acts as the launcher activity
 * - Verifies user authentication state
 * - Redirects unauthenticated users to LoginActivity
 * - Allows users to logout safely
 */
class DashboardActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        firebaseAuth = FirebaseAuth.getInstance()

        // Authentication check
        if (firebaseAuth.currentUser == null) {
            navigateToLogin()
            return
        }

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Attach click listener
        binding.btnLogout.setOnClickListener(this)
    }

    /**
     * Handles all click events for this activity.
     */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnLogout -> signOut()
        }
    }

    /**
     * Signs the user out from Firebase and Google Sign-In,
     * then redirects to LoginActivity.
     */
    private fun signOut() {
        firebaseAuth.signOut()

        GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut()

        navigateToLogin()
    }

    /**
     * Navigates the user to LoginActivity and clears DashboardActivity.
     */
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}