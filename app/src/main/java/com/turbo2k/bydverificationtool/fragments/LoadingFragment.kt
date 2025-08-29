package com.turbo2k.bydverificationtool.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.turbo2k.bydverificationtool.R
import com.turbo2k.bydverificationtool.databinding.FragmentLoadingBinding
import com.turbo2k.bydverificationtool.interfaces.PlatformCompatibilityProvider

class LoadingFragment : Fragment() {

    companion object {
        const val LOADING_DELAY_MILLIS: Long = 1000
    }

    private var _binding: FragmentLoadingBinding? = null

    private val binding get() = _binding!!

    private var compatibilityProvider: PlatformCompatibilityProvider? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        showDisclaimerDialog()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is PlatformCompatibilityProvider) {
            compatibilityProvider = context
        } else {
            throw ClassCastException("$context must implement PlatformCompatibilityListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        compatibilityProvider = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDisclaimerDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.disclaimer_title))
            .setMessage(getString(R.string.disclaimer_message))
            .setPositiveButton(getString(R.string.agree_button)) { _, _ ->
                checkPlatformCompatibility()
            }
            .setNegativeButton(getString(R.string.exit_button)) { _, _ ->
                requireActivity().finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun checkPlatformCompatibility() {
        binding.progressBar.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            compatibilityProvider?.let {
                if (it.ensurePlatformCompatibility()) {
                    navigateToMenu()
                } else {
                    binding.progressBar.visibility = View.GONE
                    showExitDialog()
                }
            }
        }, LOADING_DELAY_MILLIS)
    }

    private fun showExitDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.unsupported_platform_message))
            .setPositiveButton(getString(R.string.exit_button)) { _, _ ->
                requireActivity().finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToMenu() {
        findNavController().navigate(R.id.action_LoadingFragment_to_DevelopmentToolsVerificationFragment)
    }
}