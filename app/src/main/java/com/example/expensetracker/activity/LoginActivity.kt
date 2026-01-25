package com.example.expensetracker.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expensetracker.R
import com.example.expensetracker.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * LoginActivity handles Google Sign-In authentication.
 *
 * Flow:
 * - If user is already logged in → redirect to DashboardActivity
 * - If not logged in → show Google login button
 * - On successful login → authenticate with Firebase → DashboardActivity
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        // Auto-login check
        if (auth.currentUser != null) {
            navigateToDashboard()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupGoogleSignIn()

        // Attach click listener
        binding.btnGoogleLogin.setOnClickListener(this)
    }

    /**
     * Handles all click events for this activity.
     */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnGoogleLogin -> signInWithGoogle()
        }
    }

    /**
     * Configures Google Sign-In options and client.
     */
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    /**
     * Launches Google Sign-In intent.
     */
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /**
     * Receives result from Google Sign-In intent.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.e(
                    getString(R.string.google_login),
                    getString(R.string.google_sign_in_failed),
                    e
                )
            }
        }
    }

    /**
     * Authenticates Google account with Firebase.
     *
     * @param account GoogleSignInAccount returned from Google Sign-In
     */
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(getString(R.string.google_login), "Login success: ${auth.currentUser?.email}")
                navigateToDashboard()
            } else {
                Log.e(
                    getString(R.string.google_login),
                    getString(R.string.firebase_auth_failed),
                    task.exception
                )
            }
        }
    }

    /**
     * Navigates user to DashboardActivity and clears LoginActivity.
     */
    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}