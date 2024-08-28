package de.nulide.findmydevice.commands

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.nulide.findmydevice.R
import de.nulide.findmydevice.data.Settings
import de.nulide.findmydevice.locationproviders.CellLocationProvider
import de.nulide.findmydevice.locationproviders.GpsLocationProvider
import de.nulide.findmydevice.locationproviders.LocationProvider
import de.nulide.findmydevice.permissions.LocationPermission
import de.nulide.findmydevice.permissions.WriteSecureSettingsPermission
import de.nulide.findmydevice.services.FmdJobService
import de.nulide.findmydevice.transports.Transport
import de.nulide.findmydevice.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LocateCommand(context: Context) : Command(context) {
    companion object {
        private val TAG = this::class.simpleName
    }

    override val keyword = "locate"
    override val usage = "locate [last | all | cell | gps]"

    @get:DrawableRes
    override val icon = R.drawable.ic_location

    @get:StringRes
    override val shortDescription = R.string.cmd_locate_description_short

    @get:StringRes
    override val longDescription = R.string.cmd_locate_description_long

    override val requiredPermissions = listOf(LocationPermission())

    override val optionalPermissions = listOf(WriteSecureSettingsPermission())

    override fun <T> executeInternal(
        args: List<String>,
        transport: Transport<T>,
        coroutineScope: CoroutineScope,
        job: FmdJobService?,
    ) {
        // ignore everything except the first option (if it exists)
        val option = args.getOrElse(2) { "all" }

        // fmd locate last
        if (args.contains("last")) {
            handleLocationLastKnown(transport)
            // Even if last location is not available, return here.
            // Because requesting "last" explicitly asks not to refresh the location.
            job?.jobFinished()
            return
        }

        // build the location providers
        val providers = when (option) {
            "cell" -> listOf(CellLocationProvider(context, transport))
            "gps" -> listOf(GpsLocationProvider(context, transport))
            else ->
                listOf(
                    GpsLocationProvider(context, transport),
                    CellLocationProvider(context, transport)
                )
        }

        // run the providers and get the locations
        val lambda = suspend {
            providers
                // launch all providers in parallel
                .map { prov -> prov.getAndSendLocation() }
                // await all providers
                .forEach { deferred -> deferred.await() }

            // finish the job once all providers have finished
            job?.jobFinished()
        }
        coroutineScope.launch(Dispatchers.IO) { lambda() }
    }

    private fun <T> handleLocationLastKnown(transport: Transport<T>) {
        val lat = settings.get(Settings.SET_LAST_KNOWN_LOCATION_LAT) as String
        val lon = settings.get(Settings.SET_LAST_KNOWN_LOCATION_LON) as String
        val timeMillis = settings.get(Settings.SET_LAST_KNOWN_LOCATION_TIME) as Long

        val msg = if (lat.isNotEmpty() && lon.isNotEmpty()) {
            val batteryLevel = Utils.getBatteryLevel(context)
            LocationProvider.buildLocationString(
                context.getString(R.string.cmd_locate_last_known_location_text),
                lat,
                lon,
                batteryLevel,
                timeMillis,
            )
        } else {
            context.getString(R.string.cmd_locate_last_known_location_not_available)
        }
        transport.send(context, msg)
    }
}
