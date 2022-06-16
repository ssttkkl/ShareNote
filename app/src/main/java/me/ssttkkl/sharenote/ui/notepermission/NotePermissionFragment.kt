package me.ssttkkl.sharenote.ui.notepermission

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
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.databinding.FragmentNotePermissionBinding
import me.ssttkkl.sharenote.ui.utils.onErrorShowSnackBar
import me.ssttkkl.sharenote.ui.utils.onFinishSignalNavigateUp

@AndroidEntryPoint
class NotePermissionFragment : Fragment(), MenuProvider {

    private var _binding: FragmentNotePermissionBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<NotePermissionViewModel>()

    private lateinit var permissionAdapter: PermissionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotePermissionBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel.apply {
            noteID.value = arguments?.getInt("noteID") ?: 0
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            addMenuProvider(this@NotePermissionFragment, viewLifecycleOwner)
        }

        prepareRecyclerView()

        viewModel.onFinishSignalNavigateUp(this)
        viewModel.onErrorShowSnackBar(binding.root, viewLifecycleOwner)
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

    private fun prepareRecyclerView() {
        permissionAdapter = PermissionAdapter(
            requireContext(),
            viewLifecycleOwner,
            object : PermissionAdapter.Callback {
                override fun onClickSetReadonly(item: PermissionItemModel) =
                    viewModel.setReadonly(item.id, true)

                override fun onClickSetReadwrite(item: PermissionItemModel) =
                    viewModel.setReadonly(item.id, false)

                override fun onClickRemove(item: PermissionItemModel) =
                    viewModel.remove(item.id)

            }
        )

        binding.recView.apply {
            this.adapter = permissionAdapter
            this.layoutManager = LinearLayoutManager(context)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.permissions.collectLatest { pagingData ->
                permissionAdapter.submitData(pagingData)
                binding.swipeRefresh.isRefreshing = false  // cancel refresh
            }
        }

        lifecycleScope.launch {
            viewModel.refreshSignal.collectLatest {
                permissionAdapter.refresh()
            }
        }

        // swipe refresh
        binding.swipeRefresh.setOnRefreshListener { permissionAdapter.refresh() }
    }
}