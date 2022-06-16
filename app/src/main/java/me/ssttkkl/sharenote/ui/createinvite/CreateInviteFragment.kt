package me.ssttkkl.sharenote.ui.createinvite

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.databinding.FragmentCreateInviteBinding

@AndroidEntryPoint
class CreateInviteFragment : Fragment(), MenuProvider {

    private var _binding: FragmentCreateInviteBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<CreateInviteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateInviteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            addMenuProvider(this@CreateInviteFragment, viewLifecycleOwner)
        }

        viewModel.arguments.value = CreateInviteViewModel.Arguments(
            noteID = requireArguments().getInt("noteID"),
            readonly = requireArguments().getBoolean("readonly", false)
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.progressBar.visibility = if (state.loading) View.VISIBLE else View.GONE
                binding.qrCode.visibility = if (!state.loading) View.VISIBLE else View.GONE
                binding.expiresAt.visibility = if (!state.loading) View.VISIBLE else View.GONE

                Glide.with(requireContext())
                    .load(state.qr)
                    .into(binding.qrCode)

                binding.expiresAt.text = state.expiresAt
            }
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().navigateUp()
        }
        return true
    }
}