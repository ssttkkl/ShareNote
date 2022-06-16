package me.ssttkkl.sharenote.ui.noteedit.draft

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ssttkkl.sharenote.data.persist.entity.Draft
import me.ssttkkl.sharenote.data.repo.DraftRepository
import me.ssttkkl.sharenote.ui.utils.MyViewModel
import javax.inject.Inject

@HiltViewModel
class DraftViewModel @Inject constructor(
    private val draftRepo: DraftRepository
) : MyViewModel() {

    val drafts = draftRepo.getDrafts().map { page ->
        page.map {
            DraftItemModel(it)
        }
    }.cachedIn(viewModelScope)

    fun insert(draft: Draft) {
        viewModelScope.launch {
            draftRepo.insertDraft(draft)
            sendFinishSignal()
        }
    }

    fun delete(draftID: Int) {
        viewModelScope.launch {
            draftRepo.deleteDraft(draftID)
        }
    }

    companion object {
        val TAG
            get() = DraftViewModel::class.simpleName
    }
}