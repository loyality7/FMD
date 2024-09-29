package de.nulide.findmydevice.commands

import android.content.Context
import android.util.Log
import de.nulide.findmydevice.R
import de.nulide.findmydevice.data.Settings
import de.nulide.findmydevice.data.io.IO
import de.nulide.findmydevice.data.io.JSONFactory
import de.nulide.findmydevice.data.io.json.JSONMap
import de.nulide.findmydevice.services.FmdJobService
import de.nulide.findmydevice.transports.Transport
import de.nulide.findmydevice.utils.CypherUtils
import de.nulide.findmydevice.utils.Logger
import de.nulide.findmydevice.utils.Notifications
import kotlinx.coroutines.CoroutineScope


// Order matters for the home screen
fun availableCommands(context: Context): List<Command> {
    val commands = mutableListOf(
        CameraCommand(context),
        DeleteCommand(context),
        GpsCommand(context),
        // HelpCommand(context),
        LocateCommand(context),
        LockCommand(context),
        NoDisturbCommand(context),
        RingCommand(context),
        StatsCommand(context),
    )
    // FIXME: The HelpCommand does not know about itself
    commands.add(HelpCommand(commands, context))
    return commands
}

/**
 * CommandHandler is the entry point for taking a string,
 * mapping it to a Command, and executing the command.
 *
 * @param job
 * An optional FmdJobService that is running this command, and its JobParameters.
 * If this is non-null, the Command should call job.jobFinished() when it is done.
 * (This is like a callback.)
 */
class CommandHandler<T>
@JvmOverloads constructor(
    private val transport: Transport<T>,
    private val coroutineScope: CoroutineScope,
    private val job: FmdJobService?,
    private val showUsageNotification: Boolean = true,
) {

    /**
     * Executes commands of the form "triggerWord command options", e.g. "fmd locate cell"
     */
    fun execute(context: Context, rawCommand: String) {
        Logger.logSession(TAG, "Handling command: $rawCommand")
        Log.d(TAG, "Handling command: $rawCommand")

        val args = rawCommand.split(" ").filter { it.isNotBlank() }.toMutableList()
        val settings: Settings =
            JSONFactory.convertJSONSettings(IO.read(JSONMap::class.java, IO.settingsFileName))
        val fmdTriggerWord = settings.get(Settings.SET_FMD_COMMAND) as String

        if (args.isEmpty()) {
            Log.w(TAG, "Cannot handle: args is empty.")
            return
        }
        if (args[0].lowercase() != fmdTriggerWord.lowercase()) {
            Log.w(TAG, "Not handling: '${args[0]}' does not match trigger word '${fmdTriggerWord}'")
            return
        }

        if (showUsageNotification) {
            showUsageNotification(context, rawCommand)
        }

        if (args.size == 1) {
            // no argument ==> show help
            args.add("help")
        }

        // run the command
        for (cmd in availableCommands(context)) {
            if (args[1].lowercase() == cmd.keyword.lowercase()) {
                Log.d(TAG, "Executing command: ${cmd.keyword}")
                cmd.execute(args, transport, coroutineScope, job)
                return
            }
        }
        Log.w(TAG, "No command found that matches '${args[1]}'")
    }

    private fun showUsageNotification(context: Context, rawCommand: String) {
        val source = transport.getDestinationString()
        Notifications.notify(
            context,
            context.getString(R.string.usage_notification_title),
            context.getString(R.string.usage_notification_text_source, rawCommand, source),
            Notifications.CHANNEL_USAGE
        )
    }

    companion object {
        val TAG = CommandHandler::class.simpleName

        // fmd <pin> locate
        @JvmStatic
        fun checkAndRemovePin(settings: Settings, msg: String): String? {
            val expectedHash = settings.get(Settings.SET_PIN) as String
            val parts = msg.split(" ").filter { it.isNotBlank() }
            if (parts.size >= 2) {
                val pin = parts[1]
                if (CypherUtils.checkPasswordForFmdPin(expectedHash, pin)) {
                    return msg.replace(pin, "")
                }
            }
            return null
        }
    }
}
