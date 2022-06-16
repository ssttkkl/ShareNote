package me.ssttkkl.sharenote.ui.notedetail

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.databinding.FragmentNoteDetailBinding
import me.ssttkkl.sharenote.ui.utils.onErrorShowSnackBar
import me.ssttkkl.sharenote.ui.utils.onFinishSignalNavigateUp

@AndroidEntryPoint
class NoteDetailFragment : Fragment(), MenuProvider {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<NoteDetailViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        binding.vm = viewModel.apply {
            viewModel.noteID.value = requireArguments().getInt("noteID")
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            addMenuProvider(this@NoteDetailFragment, viewLifecycleOwner)
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.onFinishSignalNavigateUp(this)
        viewModel.onErrorShowSnackBar(binding.root, viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.note_detail, menu)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.noteState.collectLatest {
                menu.findItem(R.id.edit).isVisible = it.isReadonly == false
                menu.findItem(R.id.create_invite).isVisible = it.isOwner == true
                menu.findItem(R.id.create_invite_readonly).isVisible = it.isOwner == true
                menu.findItem(R.id.permission_manage).isVisible = it.isOwner == true
            }
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().navigateUp()
            R.id.edit -> showEditView()
            R.id.note_detail -> showNoteInfoView()
            R.id.create_invite -> showCreateInviteView(false)
            R.id.create_invite_readonly -> showCreateInviteView(true)
            R.id.permission_manage -> showPermissionView()
            R.id.remove -> showRemoveWarning()
            else -> return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showEditView() {
        findNavController().navigate(
            R.id.action_nav_note_detail_to_nav_note_edit,
            bundleOf("noteID" to viewModel.noteID.value)
        )
    }

    private fun showRemoveWarning() {
        val state = viewModel.noteState.value
        val dialog = if (state.isOwner) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_remove)
                .setMessage(R.string.message_confirm_remove_own_note)
                .setPositiveButton(R.string.yes) { _, _ -> viewModel.removeOwnNote() }
                .setNegativeButton(R.string.no) { _, _ -> ; }
                .create()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_remove)
                .setMessage(R.string.message_confirm_remove_note)
                .setPositiveButton(R.string.yes) { _, _ -> viewModel.removeNote() }
                .setNegativeButton(R.string.no) { _, _ -> ; }
                .create()
        }
        dialog.show()
    }

    private fun showCreateInviteView(readonly: Boolean) {
        findNavController().navigate(
            R.id.action_nav_note_detail_to_nav_create_invite,
            bundleOf(
                "noteID" to viewModel.noteID.value,
                "readonly" to readonly
            )
        )
    }

    private fun showPermissionView() {
        findNavController().navigate(
            R.id.action_nav_note_detail_to_nav_note_permission,
            bundleOf("noteID" to viewModel.noteID.value)
        )
    }

    private fun showNoteInfoView() {
        NoteInfoDialogFragment().show(
            childFragmentManager,
            NoteInfoDialogFragment::class.qualifiedName
        )
    }
}