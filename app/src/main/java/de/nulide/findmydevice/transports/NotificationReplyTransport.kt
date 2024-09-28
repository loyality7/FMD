package de.nulide.findmydevice.transports

import android.app.PendingIntent.CanceledException
import android.content.Context
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.robj.notificationhelperlibrary.utils.NotificationUtils
import de.nulide.findmydevice.R
import de.nulide.findmydevice.permissions.NotificationAccessPermission


class NotificationReplyTransport(
    // should only be null for the availableTransports list
    private val destination: StatusBarNotification?
) : Transport<StatusBarNotification?>(destination) {

    companion object {
        private val TAG = NotificationReplyTransport::class.simpleName
    }

    @get:DrawableRes
    override val icon = R.drawable.ic_notifications

    @get:StringRes
    override val title = R.string.transport_notification_reply_title

    @get:StringRes
    override val description = R.string.transport_notification_reply_description

    @get:StringRes
    override val descriptionAuth = R.string.transport_notification_reply_description_auth

    override val requiredPermissions = listOf(NotificationAccessPermission())

    override fun getDestinationString() = destination?.packageName ?: "Notification Response"

    override fun send(context: Context, msg: String) {
        super.send(context, msg)
        if (destination == null) {
            Log.w(TAG, "Cannot reply, destination is null!")
            return
        }

        val action = NotificationUtils.getQuickReplyAction(
            destination.notification, context.packageName
        )
        if (action == null) {
            Log.i(TAG, "Cannot send message: quick reply action was null")
            return
        }
        try {
            action.sendReply(context, msg)
        } catch (e: CanceledException) {
            Log.e(TAG, "Failed to send message via notification reply")
            e.printStackTrace()
        }
    }
}
