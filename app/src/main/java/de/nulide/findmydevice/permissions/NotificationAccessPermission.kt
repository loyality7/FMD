package de.nulide.findmydevice.permissions

import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import de.nulide.findmydevice.R
import de.nulide.findmydevice.services.ThirdPartyAccessService


class NotificationAccessPermission : Permission() {
    @get:StringRes
    override val name = R.string.perm_do_not_disturb_access_name

    override fun isGranted(context: Context): Boolean {
        val cn = ComponentName(context, ThirdPartyAccessService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return nm.isNotificationListenerAccessGranted(cn)
        } else {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners" // Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
            )
            return flat != null && flat.contains(cn.flattenToString())
        }
    }

    override fun request(activity: Activity) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        activity.startActivity(intent)
    }
}
