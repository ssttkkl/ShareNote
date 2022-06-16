package me.ssttkkl.sharenote.ui.login

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.databinding.FragmentLoginBinding
import me.ssttkkl.sharenote.ui.utils.onErrorShowToast
import me.ssttkkl.sharenote.ui.utils.onFinishSignalShowToastAndNavigateUp

@AndroidEntryPoint
class LoginFragment : Fragment(), MenuProvider {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            addMenuProvider(this@LoginFragment, viewLifecycleOwner)
        }

        binding.login.setOnClickListener { viewModel.login() }
        binding.register.setOnClickListener { showRegisterView() }

        viewModel.onErrorShowToast(requireContext(), this)
        viewModel.onFinishSignalShowToastAndNavigateUp(this, R.string.message_login_succeed)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().navigateUp()
        }
        return true
    }

    private fun showRegisterView() {
        findNavController().navigate(R.id.action_nav_login_to_nav_register)
    }
}