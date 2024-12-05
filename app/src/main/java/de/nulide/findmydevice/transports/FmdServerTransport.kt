package de.nulide.findmydevice.transports

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.nulide.findmydevice.R
import de.nulide.findmydevice.net.FMDServerApiRepoSpec
import de.nulide.findmydevice.net.FMDServerApiRepository
import de.nulide.findmydevice.permissions.Permission
import de.nulide.findmydevice.ui.settings.AddAccountActivity
import de.nulide.findmydevice.utils.Utils


class FmdServerTransport(
    context: Context,
    private val destination: String,
) : Transport<Unit>(Unit) {

    constructor(context: Context) : this(context, "FMD Server")

    private val repo = FMDServerApiRepository.getInstance(FMDServerApiRepoSpec(context))

    @get:DrawableRes
    override val icon = R.drawable.ic_cloud

    @get:StringRes
    override val title = R.string.transport_fmd_server_title

    @get:StringRes
    override val description = R.string.transport_fmd_server_description

    @get:StringRes
    override val descriptionAuth = R.string.transport_fmd_server_description_auth

    @get:StringRes
    override val descriptionNote = R.string.transport_fmd_server_description_note

    override val requiredPermissions = emptyList<Permission>()

    override val actions = listOf(TransportAction(R.string.Settings_Settings) { activity ->
        activity.startActivity(Intent(context, AddAccountActivity::class.java))
    })

    override fun getDestinationString() = destination

    @SuppressLint("MissingSuperCall")
    override fun send(context: Context, msg: String) {
        //super.send(context, msg, destination)
        // not implemented for FMD Server
    }

    override fun sendNewLocation(
        context: Context,
        provider: String,
        lat: String,
        lon: String,
        timeMillis: Long,
    ) {
        // no call to super(), we need to completely replace this for FMD Server

        val batteryLevel = Utils.getBatteryLevel(context)
        repo.sendLocation(provider, lat, lon, batteryLevel, timeMillis)
    }
}
