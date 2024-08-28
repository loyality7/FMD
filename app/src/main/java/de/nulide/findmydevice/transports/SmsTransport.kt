package de.nulide.findmydevice.transports

import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.nulide.findmydevice.R
import de.nulide.findmydevice.permissions.SmsPermission
import de.nulide.findmydevice.ui.settings.AllowlistActivity


class SmsTransport(
    private val context: Context,
    private val destination: String,
) : Transport<String>(destination) {

    @get:DrawableRes
    override val icon = R.drawable.ic_sms

    @get:StringRes
    override val title = R.string.transport_sms_title

    @get:StringRes
    override val description = R.string.transport_sms_description

    @get:StringRes
    override val descriptionAuth = R.string.transport_sms_description_auth

    @get:StringRes
    override val descriptionNote = R.string.transport_sms_description_note

    override val requiredPermissions = listOf(SmsPermission())

    override val actions = listOf(TransportAction(R.string.Settings_WhiteList) { activity ->
        activity.startActivity(Intent(context, AllowlistActivity::class.java))
    })

    override fun getDestinationString() = destination

    override fun send(context: Context, msg: String) {
        super.send(context, msg)

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        if (msg.length <= 160) {
            smsManager.sendTextMessage(destination, null, msg, null, null)
        } else {
            val parts = smsManager.divideMessage(msg)
            smsManager.sendMultipartTextMessage(destination, null, parts, null, null)
        }
    }
}