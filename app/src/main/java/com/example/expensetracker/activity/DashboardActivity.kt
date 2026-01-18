package com.example.expensetracker.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.expensetracker.fragment.CategoryFragment
import com.example.expensetracker.fragment.HomeFragment
import com.example.expensetracker.fragment.SettingsFragment
import com.example.expensetracker.fragment.TransactionFragment
import com.example.expensetracker.R
import com.example.expensetracker.databinding.ActivityDashboardBinding
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
class DashboardActivity : AppCompatActivity() {

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
        setupBottomNavigation()
    }

    /**
     * Navigates the user to LoginActivity and clears DashboardActivity.
     */
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /**
     * Initializes and handles BottomNavigationView item selection.
     *
     * - Listens for bottom navigation item clicks
     * - Loads the corresponding fragment into the fragment container
     * - Displays a toast message for Home tab selection
     * - Sets the default selected tab as Home
     */
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_transactions -> {
                    loadFragment(TransactionFragment())
                    true
                }

                R.id.nav_categories -> {
                    loadFragment(CategoryFragment())
                    true
                }

                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }

                else -> false
            }
        }

        // Sets Home as the default selected tab
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    /**
     * Replaces the current fragment inside the fragment container.
     *
     * @param fragment The fragment to be displayed
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }
}