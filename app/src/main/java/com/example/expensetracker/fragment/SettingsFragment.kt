package com.example.expensetracker.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.expensetracker.activity.LoginActivity
import com.example.expensetracker.R
import com.example.expensetracker.databinding.FragmentSettingsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

/**
 * Settings screen fragment
 * Handles app settings like logout
 */
class SettingsFragment : Fragment(R.layout.fragment_settings), View.OnClickListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    /**
     * Called after the fragment's view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSettingsBinding.bind(view)
        binding.btnLogout.setOnClickListener(this)
    }

    /**
     * Logs out the current user from Firebase and Google
     * and redirects to LoginActivity
     */
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        GoogleSignIn.getClient(
            requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN
        ).signOut()

        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }
    /**
     * Handles all click events for this fragment.
     */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnLogout -> logoutUser()
        }
    }
}
