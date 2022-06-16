package me.ssttkkl.sharenote.ui.home

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.data.entity.NoteInviteQrCodeContent
import me.ssttkkl.sharenote.data.entity.QrCodeContent
import me.ssttkkl.sharenote.databinding.FragmentHomeBinding
import me.ssttkkl.sharenote.ui.consumeinvite.ConsumeInviteFragment
import me.ssttkkl.sharenote.ui.utils.onErrorShowSnackBar


@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel>()

    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // toolbar and menu
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            addMenuProvider(this@HomeFragment, viewLifecycleOwner)
        }

        // rec view
        prepareNoteRecyclerView()

        // avatar
        prepareAvatar()

        // fab
        binding.fab.setOnClickListener { showNewNoteView() }

        // login button
        binding.login.setOnClickListener { showLoginView() }

        // handle qr content
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.qrCodeContent.collectLatest { e ->
                handleQrCodeContent(e)
            }
        }

        // handle error message
        viewModel.onErrorShowSnackBar(binding.root, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.home, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> findNavController().navigateUp()
            R.id.scan_qr_code -> showScanQrCodeView()
            else -> return false
        }
        return true
    }

    private fun prepareNoteRecyclerView() {
        noteAdapter = NoteAdapter(
            requireContext(),
            viewLifecycleOwner,
            object : NoteAdapter.Callback {
                override fun onClick(item: NoteItemModel) =
                    showNoteDetail(item.id)

                override fun onClickCreateInvite(item: NoteItemModel) =
                    showCreateInviteView(item.id, false)

                override fun onClickCreateReadonlyInvite(item: NoteItemModel) =
                    showCreateInviteView(item.id, true)

                override fun onClickEdit(item: NoteItemModel) =
                    showEditView(item.id)

                override fun onClickPermissionManage(item: NoteItemModel) =
                    showPermissionView(item.id)

                override fun onClickRemove(item: NoteItemModel) =
                    showRemoveWarning(item)
            }
        )

        binding.recView.apply {
            this.adapter = noteAdapter
            this.layoutManager = LinearLayoutManager(context)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.noteModels.collectLatest { pagingData ->
                noteAdapter.submitData(pagingData)
                binding.swipeRefresh.isRefreshing = false  // cancel refresh
            }
        }

        // swipe refresh
        binding.swipeRefresh.setOnRefreshListener { noteAdapter.refresh() }
    }

    private fun prepareAvatar() {
        binding.avatar.setOnClickListener {
            if (viewModel.userState.value.isLoggedIn) {
                PopupMenu(requireContext(), binding.avatar).apply {
                    inflate(R.menu.user_options)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.logout -> {
                                viewModel.logout()
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            } else {
                showLoginView()
            }
        }
    }

    private fun showLoginView() {
        findNavController().navigate(
            R.id.action_nav_home_to_nav_login
        )
    }

    private fun showNewNoteView() {
        findNavController().navigate(
            R.id.action_nav_home_to_nav_note_edit
        )
    }

    private fun showNoteDetail(noteID: Int) {
        findNavController().navigate(
            R.id.action_nav_home_to_nav_note_detail,
            bundleOf("noteID" to noteID)
        )
    }

    private fun showEditView(noteID: Int) {
        findNavController().navigate(
            R.id.action_nav_home_to_nav_note_edit,
            bundleOf("noteID" to noteID)
        )
    }

    private fun showRemoveWarning(note: NoteItemModel) {
        val dialog = if (note.isOwner) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_remove)
                .setMessage(R.string.message_confirm_remove_own_note)
                .setPositiveButton(R.string.yes) { _, _ -> viewModel.removeOwnNote(note.id) }
                .setNegativeButton(R.string.no) { _, _ -> ; }
                .create()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_remove)
                .setMessage(R.string.message_confirm_remove_note)
                .setPositiveButton(R.string.yes) { _, _ -> viewModel.removeNote(note.id) }
                .setNegativeButton(R.string.no) { _, _ -> ; }
                .create()
        }
        dialog.show()
    }

    private fun showCreateInviteView(noteID: Int, readonly: Boolean) {
        findNavController().navigate(
            R.id.action_nav_home_to_nav_create_invite,
            bundleOf("noteID" to noteID, "readonly" to readonly)
        )
    }

    private fun showPermissionView(noteID: Int) {
        findNavController().navigate(
            R.id.action_nav_home_to_nav_note_permission,
            bundleOf("noteID" to noteID)
        )
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        viewLifecycleOwner.lifecycleScope.launch {
            result.contents?.let {
                viewModel.parseQrCode(it)
            }
        }
    }

    private fun showScanQrCodeView() {
        if (viewModel.userState.value.isLoggedIn)
            barcodeLauncher.launch(ScanOptions())
        else
            Snackbar.make(binding.root, R.string.message_not_logged_in, Snackbar.LENGTH_SHORT)
    }

    private fun handleQrCodeContent(content: QrCodeContent) {
        when (content) {
            is NoteInviteQrCodeContent -> showConsumeInviteView(content.inviteID)
        }
    }

    private fun showConsumeInviteView(inviteID: String) {
        ConsumeInviteFragment().apply {
            arguments = bundleOf("inviteID" to inviteID)
        }.show(childFragmentManager, ConsumeInviteFragment::class.qualifiedName)
    }
}