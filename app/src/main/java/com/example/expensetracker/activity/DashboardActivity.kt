package com.example.expensetracker.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.databinding.ActivityDashboardBinding
import com.example.expensetracker.fragment.CategoryFragment
import com.example.expensetracker.fragment.HomeFragment
import com.example.expensetracker.fragment.SettingsFragment
import com.example.expensetracker.fragment.TransactionFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        db = AppDatabase.getInstance(this)
        AppDatabase.getInstance(this).let { db ->
            insertDefaultCategoriesIfNeeded(db)
        }
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

    /**
     * Ensures that default categories exist in the database.
     *
     * Default categories are inserted only on the first app launch.
     * A SharedPreferences flag prevents duplicate insertion on subsequent launches.
     *
     * Database operations are executed on the IO dispatcher.
     *
     * @param db App database instance
     */
    private fun insertDefaultCategoriesIfNeeded(db: AppDatabase) {
        val prefs = getSharedPreferences(getString(R.string.app_prefs), MODE_PRIVATE)
        val isInserted = prefs.getBoolean(getString(R.string.default_categories_inserted), false)

        if (isInserted) return

        lifecycleScope.launch(Dispatchers.IO) {

            val defaultCategories = listOf(
                CategoryEntity(
                    name = getString(R.string.salary),
                    iconRes = R.drawable.ic_money,
                    color = Color.GREEN
                ),
                CategoryEntity(
                    name = getString(R.string.gift),
                    iconRes = R.drawable.ic_gift,
                    color = Color.MAGENTA
                ),
                CategoryEntity(
                    name = getString(R.string.refund),
                    iconRes = R.drawable.ic_refund,
                    color = Color.CYAN
                ),
                CategoryEntity(
                    name = getString(R.string.investment),
                    iconRes = R.drawable.ic_investment,
                    color = Color.YELLOW
                ),
                CategoryEntity(
                    name = getString(R.string.other),
                    iconRes = R.drawable.ic_other,
                    color = Color.DKGRAY
                ),
            )

            db.categoryDao().insertAll(defaultCategories)

            prefs.edit().putBoolean(getString(R.string.default_categories_inserted), true).apply()
        }
    }
}