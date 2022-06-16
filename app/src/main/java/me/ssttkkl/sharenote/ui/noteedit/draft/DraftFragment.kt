package me.ssttkkl.sharenote.ui.noteedit.draft

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.data.persist.entity.Draft
import me.ssttkkl.sharenote.databinding.FragmentNoteDraftBinding
import me.ssttkkl.sharenote.ui.noteedit.NoteEditViewModel
import me.ssttkkl.sharenote.ui.utils.onErrorShowSnackBar
import me.ssttkkl.sharenote.ui.utils.onFinishSignalShowToastAndNavigateUp
import java.time.OffsetDateTime

@AndroidEntryPoint
class DraftFragment : Fragment(), MenuProvider {

    private var _binding: FragmentNoteDraftBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<DraftViewModel>()
    private val noteEditViewModel by hiltNavGraphViewModels<NoteEditViewModel>(R.id.nav_graph_note_edit)

    private lateinit var draftAdapter: DraftAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDraftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // toolbar and menu
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            addMenuProvider(this@DraftFragment, viewLifecycleOwner)
        }

        prepareRecyclerView()

        viewModel.onErrorShowSnackBar(binding.root, viewLifecycleOwner)
        viewModel.onFinishSignalShowToastAndNavigateUp(this, R.string.message_save_draft_succeed)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.draft, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> findNavController().navigateUp()
            R.id.save -> save()
            else -> return false
        }
        return true
    }

    private fun prepareRecyclerView() {
        draftAdapter = DraftAdapter(
            requireContext(),
            viewLifecycleOwner,
            object : DraftAdapter.Callback {
                override fun onClick(item: DraftItemModel) =
                    showApplyAlert(item)

                override fun onClickRemove(item: DraftItemModel) =
                    showRemoveAlert(item)
            }
        )

        binding.recView.apply {
            this.adapter = draftAdapter
            this.layoutManager = LinearLayoutManager(context)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.drafts.collectLatest { pagingData ->
                draftAdapter.submitData(pagingData)
            }
        }
    }

    private fun showApplyAlert(draft: DraftItemModel) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_confirm_apply_draft)
            .setMessage(R.string.message_confirm_apply_draft)
            .setPositiveButton(R.string.confirm) { _, _ ->
                noteEditViewModel.applyDraft(draft.draft)
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel) { _, _ -> ; }
            .create()
            .show()
    }

    private fun showRemoveAlert(draft: DraftItemModel) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_remove)
            .setMessage(R.string.message_confirm_remove_draft)
            .setPositiveButton(R.string.confirm) { _, _ -> viewModel.delete(draft.id) }
            .setNegativeButton(R.string.cancel) { _, _ -> ; }
            .create()
            .show()
    }

    private fun save() {
        val draft = Draft(
            id = 0,
            title = noteEditViewModel.title.value,
            content = noteEditViewModel.content.value,
            createdAt = OffsetDateTime.now(),
        )
        viewModel.insert(draft)
    }
}