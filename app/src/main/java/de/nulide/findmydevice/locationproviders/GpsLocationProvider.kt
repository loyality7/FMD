package de.nulide.findmydevice.locationproviders

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import de.nulide.findmydevice.R
import de.nulide.findmydevice.data.Settings
import de.nulide.findmydevice.data.SettingsRepository
import de.nulide.findmydevice.permissions.LocationPermission
import de.nulide.findmydevice.permissions.WriteSecureSettingsPermission
import de.nulide.findmydevice.transports.Transport
import de.nulide.findmydevice.utils.SecureSettings
import de.nulide.findmydevice.utils.log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred


class GpsLocationProvider<T>(
    private val context: Context,
    private val transport: Transport<T>,
) : LocationProvider(), LocationListener {

    companion object {
        private val TAG = GpsLocationProvider::class.simpleName

        @JvmStatic
        fun isGpsOn(context: Context): Boolean {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lm.isLocationEnabled
            } else lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var deferred: CompletableDeferred<Unit>? = null
    private var isGpsTurnedOnByUs = false

    @SuppressLint("MissingPermission") // linter is not good enough to recognize the check
    override fun getAndSendLocation(): Deferred<Unit> {
        val def = CompletableDeferred<Unit>()
        deferred = def

        if (!LocationPermission().isGranted(context)) {
            context.log().i(TAG, "Missing location permission, cannot get location")
            def.complete(Unit)
            return def
        }

        if (!isGpsOn(context)) {
            if (WriteSecureSettingsPermission().isGranted(context)) {
                SecureSettings.turnGPS(context, true)
                isGpsTurnedOnByUs = true
            } else {
                context.log().w(
                    TAG,
                    "Cannot run fmd locate: GPS is off and missing permission WRITE_SECURE_SETTINGS"
                )
                transport.send(
                    context,
                    context.getString(R.string.cmd_locate_response_location_off)
                )
                def.complete(Unit)
                return def
            }
        }

        transport.send(context, context.getString(R.string.cmd_locate_response_gps_will_follow))
        context.log().d(TAG, "Requesting location update from GPS")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getAndSendLocationAndroid12()
            return def
        }

        for (provider in locationManager.allProviders) {
            // we may be in a background thread due to being in a coroutine,
            // but this needs to be called on the main thread
            ContextCompat.getMainExecutor(context).execute {
                locationManager.requestLocationUpdates(provider, 1000, 0f, this)
            }
        }
        return def
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    private fun getAndSendLocationAndroid12() {
        val FIVE_MINS_MILLIS = 300_000L
        val locationRequest = LocationRequest.Builder(2000L)
            .setQuality(LocationRequest.QUALITY_HIGH_ACCURACY)
            .setDurationMillis(FIVE_MINS_MILLIS) // timeout before it fails
            .build()
        val consumer = { location: Location? ->
            if (location != null) {
                onLocationChanged(location)
            } else {
                // Fall back to getting the last known location.
                // The last location known by the LocationManager may still be newer
                // than the last location known by FMD.
                getLastKnownLocation(true)
            }
        }
        locationManager.getCurrentLocation(
            LocationManager.FUSED_PROVIDER,
            locationRequest,
            null,
            context.mainExecutor,
            consumer
        )
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(asFallBackForCurrentLocation: Boolean = false) {
        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        val settings = SettingsRepository.getInstance(context)
        val cachedLat = settings.get(Settings.SET_LAST_KNOWN_LOCATION_LAT) as String
        val cachedLon = settings.get(Settings.SET_LAST_KNOWN_LOCATION_LON) as String
        val cachedTimeMillis = (settings.get(Settings.SET_LAST_KNOWN_LOCATION_TIME) as Number).toLong()
        val isCacheValid = cachedLat.isNotEmpty() && cachedLon.isNotEmpty()

        if (lastLocation == null) {
            if (asFallBackForCurrentLocation) {
                // If current location was requested originally, don't fall back to cached location.
                transport.send(context, context.getString(R.string.cmd_locate_response_gps_fail))
            } else if (isCacheValid) {
                // If last location was requested, fall back to cached location.
                val provider = context.getString(R.string.cmd_locate_last_known_location_text)
                transport.sendNewLocation(context, provider, cachedLat, cachedLon, cachedTimeMillis)
            } else {
                // No location and nothing to fall back to.
                transport.send(
                    context,
                    context.getString(R.string.cmd_locate_last_known_location_not_available)
                )
            }
        } else {
            // If the last location from the LocationManager is newer than our cached location,
            // update our cached location.
            if (lastLocation.time > cachedTimeMillis) {
                onLocationChanged(lastLocation)
            } else {
                val provider = context.getString(R.string.cmd_locate_last_known_location_text)
                transport.sendNewLocation(
                    context,
                    provider,
                    cachedLat,
                    cachedLon,
                    cachedTimeMillis
                )
            }
        }
        cleanup()
    }

    override fun onLocationChanged(location: Location) {
        val provider = location.provider ?: "GPS"
        val lat = location.latitude.toString()
        val lon = location.longitude.toString()
        context.log().d(TAG, "Location found by $provider")

        storeLastKnownLocation(context, lat, lon, location.time)
        transport.sendNewLocation(context, provider, lat, lon, location.time)

        cleanup()
    }

    private fun cleanup() {
        if (isGpsTurnedOnByUs) {
            SecureSettings.turnGPS(context, false)
        }
        locationManager.removeUpdates(this)
        deferred?.complete(Unit)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
