package de.nulide.findmydevice.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.findmydevice.R
import de.nulide.findmydevice.utils.Logger
import de.nulide.findmydevice.utils.RootAccess.Companion.execCommand
import de.nulide.findmydevice.utils.RootAccess.Companion.isRooted
import de.nulide.findmydevice.utils.ShizukuUtil.Companion.isShizukuPermissionGranted
import de.nulide.findmydevice.utils.ShizukuUtil.Companion.isShizukuRunning
import de.nulide.findmydevice.utils.ShizukuUtil.Companion.requestShizukuPermission
import rikka.shizuku.Shizuku


class WriteSecureSettingsPermission : Permission() {

    @get:StringRes
    override val name = R.string.perm_write_secure_settings_name

    override fun isGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PERMISSION_GRANTED
    }

    override fun request(activity: Activity) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.grant_write_secure_settings_title)
            setMessage(R.string.grant_write_secure_settings_description)

            setNegativeButton(R.string.grant_via_root) { _, _ -> requestViaRoot(activity) }
            if (isShizukuRunning()) {
                setNeutralButton(R.string.grant_via_shizuku) { _, _ -> requestViaShizuku(activity) }
            }
            setPositiveButton(R.string.grant_via_adb) { _, _ -> requestManually(activity) }
        }.show()
    }

    private fun requestManually(activity: Activity) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://gitlab.com/Nulide/findmydevice/-/wikis/PERMISSION-WRITE_SECURE_SETTINGS")
        )
        activity.startActivity(intent)
    }

    private fun requestViaShizuku(context: Context) {
        if (!isShizukuPermissionGranted()) {
            requestShizukuPermission()
            return
        }

        val command = "pm grant ${context.packageName} ${Manifest.permission.WRITE_SECURE_SETTINGS}"
        val proc = Shizuku.newProcess(arrayOf("sh", "-c", command), null, "/")
        try {
            proc.waitFor()
        } catch (e: InterruptedException) {
            Toast.makeText(
                context,
                context.getString(R.string.perm_shizuku_failed),
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
            Logger.log("ShizukuError", e.toString())
        }
    }

    private fun requestViaRoot(context: Context) {
        if (isRooted()) {
            val command =
                "pm grant ${context.packageName} ${Manifest.permission.WRITE_SECURE_SETTINGS}"
            execCommand(context, command)
        } else {
            Toast.makeText(context, context.getString(R.string.perm_root_denied), Toast.LENGTH_LONG)
                .show()
        }
    }
}
