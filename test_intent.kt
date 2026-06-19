import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.wear.input.RemoteInputIntentHelper
import android.view.inputmethod.EditorInfo

fun main() {
    val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
    val remoteInputs = listOf(
        RemoteInput.Builder("search_query")
            .setLabel("搜索")
            .setAllowFreeFormInput(true)
            .build()
    )
    RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs.map { it as android.app.RemoteInput })
}
