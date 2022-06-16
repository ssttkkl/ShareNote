package me.ssttkkl.sharenote.ui.noteedit

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.databinding.FragmentNoteEditBinding
import me.ssttkkl.sharenote.ui.utils.onErrorShowSnackBar
import me.ssttkkl.sharenote.ui.utils.onFinishSignalNavigateUp

@AndroidEntryPoint
class NoteEditFragment : Fragment(), MenuProvider, TagAdapter.Callback {

    private var _binding: FragmentNoteEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel by hiltNavGraphViewModels<NoteEditViewModel>(R.id.nav_graph_note_edit)

    private lateinit var tagAdapter: TagAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteEditBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel.apply {
            setNoteID(arguments?.getInt("noteID") ?: 0)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            addMenuProvider(this@NoteEditFragment, viewLifecycleOwner)
        }

        prepareTagRecyclerView()

        viewModel.conflict.onEach { onConflict() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.onErrorShowSnackBar(binding.root, viewLifecycleOwner)
        viewModel.onFinishSignalNavigateUp(this)
    }

    private fun prepareTagRecyclerView() {
        tagAdapter = TagAdapter(this)
        binding.recViewTags.apply {
            adapter = tagAdapter
            layoutManager = FlexboxLayoutManager(context)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.tags.collectLatest { tagAdapter.submitList(it) }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.note_edit, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBack()
            R.id.save -> viewModel.save()
            R.id.draft -> showDraftView()
            else -> return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClickTag(tag: TagItemModel.Tag) {
        viewModel.handleClickTag(tag)
    }

    override fun onClickAddTag(addTag: TagItemModel.AddTag) {
        AddTagDialogFragment().show(childFragmentManager, AddTagDialogFragment::class.simpleName)
    }

    private fun onBack() {
        if (viewModel.modified.value) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_note_unsaved)
                .setMessage(R.string.message_note_unsaved)
                .setPositiveButton(R.string.confirm) { _, _ -> findNavController().navigateUp() }
                .setNegativeButton(R.string.cancel) { _, _ -> ; }
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun onConflict() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_note_conflict_on_save)
            .setMessage(R.string.message_note_conflict_on_save)
            .setNeutralButton(R.string.action_save_draft_on_conflict) { _, _ -> viewModel.saveAsDraft() }
            .setPositiveButton(R.string.confirm) { _, _ -> viewModel.save(true) }
            .setNegativeButton(R.string.cancel) { _, _ -> ; }
            .show()
    }

    private fun showDraftView() {
        findNavController().navigate(
            R.id.action_nav_note_edit_to_nav_draft
        )
    }
}