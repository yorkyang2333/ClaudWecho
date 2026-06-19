import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.wear.input.RemoteInputIntentHelper
import android.view.inputmethod.EditorInfo

fun test() {
    val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
    val remoteInputs = listOf(
        RemoteInput.Builder("search_query")
            .setLabel("搜索")
            .setAllowFreeFormInput(true)
            // .wearableExtender(RemoteInput.WearableExtender().setInputActionType(EditorInfo.IME_ACTION_SEARCH))
            .build()
    )
    // RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs) // THIS expects android.app.RemoteInput
}
