import android.content.Intent
import android.app.RemoteInput
import androidx.wear.input.RemoteInputIntentHelper

fun test() {
    val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
    val remoteInputs = listOf(
        RemoteInput.Builder("search_query")
            .setLabel("搜索")
            .setAllowFreeFormInput(true)
            .build()
    )
    RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
}
