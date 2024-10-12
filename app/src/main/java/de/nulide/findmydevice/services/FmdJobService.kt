package de.nulide.findmydevice.services

import android.app.job.JobParameters
import android.app.job.JobService
import androidx.annotation.CallSuper
import de.nulide.findmydevice.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * This alternative JobService base class exists in order to share the
 * coroutineScope for Commands to use to run their asynchronous work in, and
 * to provide a central way to cancel it when the job finishes or is stopped.
 */
abstract class FmdJobService : JobService() {
    companion object {
        private val TAG = FmdJobService::class.simpleName
    }

    val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    private var params: JobParameters? = null

    @CallSuper
    override fun onStartJob(params: JobParameters?): Boolean {
        this.params = params
        return false
    }

    @CallSuper
    open fun jobFinished() {
        this.log().d(TAG, "Finishing job ${params?.jobId}")
        coroutineScope.cancel()
        params?.let { this.jobFinished(it, false) }
    }

    @CallSuper
    override fun onStopJob(params: JobParameters?): Boolean {
        coroutineScope.cancel()
        return false
    }
}
