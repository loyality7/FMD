package de.nulide.findmydevice.transports

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.findmydevice.R
import de.nulide.findmydevice.commands.CommandHandler
import de.nulide.findmydevice.data.Settings
import de.nulide.findmydevice.data.SettingsRepository
import de.nulide.findmydevice.permissions.PostNotificationsPermission
import de.nulide.findmydevice.receiver.CopyInAppTextReceiver
import de.nulide.findmydevice.receiver.EXTRA_TEXT_TO_COPY
import de.nulide.findmydevice.utils.Notifications


class InAppTransport(
    private val context: Context,
) : Transport<Unit>(Unit) {

    @get:DrawableRes
    override val icon = R.drawable.ic_in_app

    @get:StringRes
    override val title = R.string.transport_inapp_title

    @get:StringRes
    override val description = R.string.transport_inapp_description

    override val requiredPermissions = listOf(PostNotificationsPermission())

    override val actions =
        listOf(TransportAction(R.string.transport_inapp_send_command_title) { activity ->
            onTestCommandClicked(activity)
        })

    override fun getDestinationString(): String = context.getString(R.string.transport_inapp_title)

    override fun send(context: Context, msg: String) {
        super.send(context, msg)

        val title = context.getString(R.string.transport_inapp_title)

        Notifications.notify(context, title, msg, Notifications.CHANNEL_IN_APP) { builder ->
            val copyIntent = Intent(context, CopyInAppTextReceiver::class.java)
            copyIntent.putExtra(EXTRA_TEXT_TO_COPY, msg)

            val copyPendingIntent = PendingIntent.getBroadcast(
                context,
                57568,
                copyIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(
                R.drawable.ic_content_copy,
                context.getString(R.string.copy),
                copyPendingIntent
            )
        }
    }
}

@SuppressLint("SetTextI18n")
fun onTestCommandClicked(activity: AppCompatActivity) {
    val context = activity
    val dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_in_app_command, null)
    val editTextCommand = dialogLayout.findViewById<EditText>(R.id.editTextCommand)

    val settings = SettingsRepository.getInstance(context)
    val fmdTriggerWord = settings.get(Settings.SET_FMD_COMMAND) as String
    editTextCommand.setText("$fmdTriggerWord ")

    MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.transport_inapp_send_command_title))
        .setView(dialogLayout)
        .setPositiveButton(
            context.getString(R.string.transport_inapp_send_command_button_send)
        ) { _, _ ->
            val transport = InAppTransport(context)
            val commandHandler = CommandHandler(transport, activity.lifecycleScope, null, false)
            val command = editTextCommand.text.toString()
            commandHandler.execute(context, command)
        }
        .setNegativeButton(context.getString(R.string.cancel), null)
        .show()
}
