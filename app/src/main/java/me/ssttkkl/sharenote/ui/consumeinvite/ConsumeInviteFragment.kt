package me.ssttkkl.sharenote.ui.consumeinvite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.databinding.FragmentConsumeInviteBinding

@AndroidEntryPoint
class ConsumeInviteFragment : DialogFragment() {
    private var _binding: FragmentConsumeInviteBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ConsumeInviteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsumeInviteBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel.apply {
            inviteID.value = requireArguments().getString("inviteID")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.confirmButton.setOnClickListener { viewModel.consume() }
        binding.cancelButton.setOnClickListener { dismiss() }

        viewLifecycleOwner.lifecycleScope.launch {
            val message = viewModel.finishMessage.first()
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
                .show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}