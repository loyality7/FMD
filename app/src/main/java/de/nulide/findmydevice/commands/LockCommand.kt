package de.nulide.findmydevice.commands

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.nulide.findmydevice.R
import de.nulide.findmydevice.permissions.DeviceAdminPermission
import de.nulide.findmydevice.permissions.OverlayPermission
import de.nulide.findmydevice.services.FmdJobService
import de.nulide.findmydevice.transports.Transport
import de.nulide.findmydevice.ui.LockScreenMessage


class LockCommand(context: Context) : Command(context) {

    override val keyword = "lock"
    override val usage = "lock [msg]"

    @get:DrawableRes
    override val icon = R.drawable.ic_phone_lock

    @get:StringRes
    override val shortDescription = R.string.cmd_lock_description_short

    override val longDescription = R.string.cmd_lock_description_long

    override val requiredPermissions = listOf(DeviceAdminPermission(), OverlayPermission())

    override fun <T> executeInternal(
        args: List<String>,
        transport: Transport<T>,
        job: FmdJobService?,
    ) {
        val devicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        devicePolicyManager.lockNow()

        val customText = args.getOrNull(3)

        val lockScreenMessage = Intent(context, LockScreenMessage::class.java)

        lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // TODO: bring back passing this data??
        //lockScreenMessage.putExtra(LockScreenMessage.SENDER, transport.getDestinationString())
        //lockScreenMessage.putExtra(LockScreenMessage.SENDER_TYPE, ch.getSender().SENDER_TYPE)

        if (customText != null) {
            val customMessage = args.subList(3, args.size).joinToString(" ")
            lockScreenMessage.putExtra(LockScreenMessage.CUSTOM_TEXT, customMessage)
        }
        context.startActivity(lockScreenMessage)

        transport.send(context, context.getString(R.string.cmd_lock_response))
        job?.jobFinished()
    }
}
