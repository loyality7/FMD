package de.nulide.findmydevice.permissions

import android.content.Context

// Order matters for the permissions screen
fun globalAppPermissions() = listOf(
    PostNotificationsPermission(),
    BatteryOptimizationsPermission(),
    UnusedAppRestrictionsPermission(),
)

fun isMissingGlobalAppPermission(context: Context): Boolean {
    return globalAppPermissions().any { perm -> !perm.isGranted(context) }
}
