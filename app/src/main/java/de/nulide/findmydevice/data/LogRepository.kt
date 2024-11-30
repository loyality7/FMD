package de.nulide.findmydevice.data

import android.content.Context
import android.net.Uri
import android.os.Build
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import de.nulide.findmydevice.data.UncaughtExceptionHandler.Companion.CRASH_MSG_HEADER
import de.nulide.findmydevice.utils.SingletonHolder
import de.nulide.findmydevice.utils.writeToUri
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max


const val LOG_FILENAME = "logs.json"

data class LogEntry(
    val level: String,
    val timeMillis: Long,
    val tag: String,
    val msg: String,
)

class LogModel : LinkedList<LogEntry>()


class LogRepository private constructor(private val context: Context) {

    companion object : SingletonHolder<LogRepository, Context>(::LogRepository) {
        fun filenameForExport(): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                return "fmd-logs-$date.json"
            } else {
                return "fmd-logs.json"
            }
        }
    }

    private val gson = Gson()

    val list: LogModel

    // All functions that iterate over the list or modify it should use the lock.
    // It can happen that a first thread is already saving the list (gson.toJson internally uses an iterator)
    // while a second thread is calling list.sublist().clear().
    // This causes a ConcurrentModificationException.
    // See https://gitlab.com/Nulide/findmydevice/-/issues/262.
    private val lock = ReentrantLock()

    init {
        val file = File(context.filesDir, LOG_FILENAME)
        if (!file.exists()) {
            file.createNewFile()
        }
        val reader = JsonReader(FileReader(file))
        list = gson.fromJson(reader, LogModel::class.java) ?: LogModel()
    }

    private fun saveList() = lock.withLock {
        val raw = gson.toJson(list)

        val file = File(context.filesDir, LOG_FILENAME)
        file.writeText(raw)
    }

    fun add(new: LogEntry) = lock.withLock {
        list.add(new)
        pruneLog()
        // no need to save, pruneLog() saves
    }

    fun pruneLog() = lock.withLock {
        // Prune the log when it becomes too large.
        // When we prune, prune a bit more than the pruning threshold.
        // This avoids pruning the log with every new entry.
        if (list.size < 1200) {
            return
        }
        val maxLength = 1000
        val newStart = max(0, list.size - maxLength)

        // Prune the old logs (from the beginning until the new start)
        // subList returns a view, thus we can use it to remove from list
        list.subList(0, newStart).clear()
        saveList()
    }

    fun clearLog() = lock.withLock {
        list.clear()
        saveList()
    }

    fun getLastCrashLog(): LogEntry? = lock.withLock {
        for (e in list.reversed()) {
            if (e.msg.startsWith(CRASH_MSG_HEADER)) {
                return e
            }
        }
        return null
    }

    fun writeToUri(context: Context, uri: Uri) {
        writeToUri(context, uri, list)
    }
}
