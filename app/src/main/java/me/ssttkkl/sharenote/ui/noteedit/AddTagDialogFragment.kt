package me.ssttkkl.sharenote.ui.noteedit

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.databinding.FragmentAddTagBinding

@AndroidEntryPoint
class AddTagDialogFragment : DialogFragment() {

    private var _binding: FragmentAddTagBinding? = null
    private val binding get() = _binding!!

    private val viewModel by hiltNavGraphViewModels<NoteEditViewModel>(R.id.nav_graph_note_edit)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentAddTagBinding.inflate(requireActivity().layoutInflater, null, false)

        return AlertDialog.Builder(context)
            .setTitle(R.string.add_tag)
            .setView(binding.root)
            .setPositiveButton(R.string.confirm) { _, _ -> save() }
            .setNegativeButton(R.string.cancel) { _, _ -> dismiss() }
            .create()
    }

    private fun save() {
        val text = binding.text.text.toString()
        if (text.isBlank()) {
            Toast.makeText(context, R.string.tag_cannot_be_blank, Toast.LENGTH_SHORT).show()
        } else {
            val ok = viewModel.saveTag(text)
            if (ok) {
                dismiss()
            } else {
                Toast.makeText(context, R.string.tag_already_exists, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}