package me.ssttkkl.sharenote.ui.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

open class MyViewModel : ViewModel() {
    private val _errorMessage = Channel<Int>(Channel.BUFFERED)
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _finishSignal = Channel<Unit>(Channel.BUFFERED)
    val finishSignal = _finishSignal.receiveAsFlow()

    suspend fun sendErrorMessage(@StringRes id: Int) {
        _errorMessage.send(id)
    }

    suspend fun sendFinishSignal() {
        _finishSignal.send(Unit)
    }
}

suspend inline fun MyViewModel.onFinishSignal(crossinline action: suspend () -> Unit) {
    finishSignal.first()
    action()
}

suspend inline fun MyViewModel.onErrorMessage(crossinline action: suspend (Int) -> Unit) {
    errorMessage.collectLatest {
        action(it)
    }
}

fun MyViewModel.onErrorShowToast(context: Context, lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycleScope.launch {
        onErrorMessage { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

fun MyViewModel.onErrorShowSnackBar(view: View, lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycleScope.launch {
        onErrorMessage { msg ->
            Snackbar.make(view, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

fun MyViewModel.onFinishSignalNavigateUp(fragment: Fragment) {
    fragment.lifecycleScope.launch {
        onFinishSignal {
            fragment.findNavController().navigateUp()
        }
    }
}

fun MyViewModel.onFinishSignalShowToastAndNavigateUp(fragment: Fragment, @StringRes message: Int) {
    fragment.lifecycleScope.launch {
        onFinishSignal {
            Toast.makeText(fragment.requireContext(), message, Toast.LENGTH_SHORT).show()
            fragment.findNavController().navigateUp()
        }
    }
}