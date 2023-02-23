package com.eco.beenlovememory.utils

import android.annotation.SuppressLint
import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.eco.beenlovememory.BuildConfig
import com.eco.beenlovememory.R
import com.eco.beenlovememory.base.BaseActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class FileUtils {

    fun getMediaDuration(file: File, context: Context): Long {
        if (!file.exists()) return 0
        val inputStream = FileInputStream(file.absolutePath)
        val retriever = MediaMetadataRetriever()
        var duration = 0L
        var mediaPlayer: MediaPlayer? = null
        try {
            retriever.setDataSource(inputStream.fd)
            duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0
        } catch (e: Exception) {
            try {
                val uri =
                    FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                mediaPlayer = MediaPlayer.create(context, uri);
                if (mediaPlayer == null)
                    mediaPlayer = MediaPlayer.create(context, Uri.parse(file.path))
                duration = if (mediaPlayer == null) {
                    0
                } else {
                    val time = mediaPlayer.duration
                    if (time == -1) {
                        0
                    } else {
                        time.toLong()
                    }
                }
            } catch (_: Exception) {
            }
        }
        mediaPlayer?.release()
        retriever.release()
        return duration
    }

    fun scanFile(videoRenderPath: String, context: Context?, onComplete: ((Uri?) -> Unit)? = null) {
        context?.let {
            MediaScannerConnection.scanFile(
                context, arrayOf(
                    videoRenderPath
                ),
                null
            ) { _: String?, uri: Uri? -> onComplete?.invoke(uri) }
        } ?: run {
            onComplete?.invoke(null)
        }
    }

    fun addPermissionForPrivateFile(file: File) {
        if (!file.canExecute()) file.setExecutable(true)
        if (!file.canRead()) file.setReadable(true)
        if (!file.canWrite()) file.setWritable(true)
    }

    private fun convertTimeToDate(): String? {
        val formatter = SimpleDateFormat(
            "HH_mm_ss",
            Locale.getDefault()
        )
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate)
    }

    fun getAvailableExternalMemorySize(): Float {
        val ERROR = 0f
        return if (externalMemoryAvailable()) {
            val path: File = Environment.getExternalStorageDirectory();
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            blockSize * availableBlocks / (1024 * 1024 * 1024f)
        } else {
            ERROR
        }
    }

    fun externalMemoryAvailable(): Boolean {
        return Environment.MEDIA_MOUNTED ==
                Environment.getExternalStorageState()
    }

    fun getTotalExternalMemorySize(): Float {
        val ERROR = 0f
        return if (externalMemoryAvailable()) {
            val path: File = Environment.getExternalStorageDirectory();
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            blockSize * totalBlocks / (1024 * 1024 * 1024f)
        } else {
            ERROR
        }
    }

    fun formatSizeToGB(sizeGb: Double): String {
        return DecimalFormat("##.#").format(sizeGb)
    }


    fun deleteFile(uri: Uri, activity: AppCompatActivity, complete: (Boolean) -> Unit) {
        try {
            if (activity.contentResolver.delete(uri, null, null) != -1) {
                complete.invoke(true)
            } else {
                complete.invoke(false)
            }
        } catch (exception: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (exception is RecoverableSecurityException) {
                    val editPendingIntent =
                        MediaStore.createDeleteRequest(activity.contentResolver,
                            arrayOf(uri).map { it })
                    activity.startIntentSenderForResult(
                        editPendingIntent.intentSender, Constants.REQUEST_CODE_DELETE_VIDEO,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                    complete.invoke(true)
                } else {
                    complete.invoke(false)
                }
            } else {
                complete.invoke(false)
            }
        }
    }


    private fun copyFile(inputStream: InputStream, out: OutputStream) {
        try {
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
        } catch (e: IOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun getAudioDuration(context: Context, musicPath: String): Int {
        return try {
            val length: Int
            val mp = MediaPlayer.create(context, Uri.parse(musicPath))
            length = mp.duration
            mp.release()
            length
        } catch (e: Exception) {
            try {
                val musicDuration: Int
                val retriever = MediaMetadataRetriever()
                try {
                    val uri: Uri
                    val file = File(musicPath)
                    uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            context,
                            BuildConfig.APPLICATION_ID + ".provider",
                            file
                        )
                    } else {
                        Uri.fromFile(file.absoluteFile)
                    }
                    retriever.setDataSource(context, uri)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    return 0
                }
                val duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                musicDuration = duration!!.toInt()
                retriever.release()
                musicDuration
            } catch (ex: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                0
            }
        }
    }


    fun getLengthFile(paramDouble: Double): String {
        val d1 = paramDouble / 1024.0
        val d2 = d1 / 1024.0
        val d3 = d2 / 1024.0
        if (paramDouble < 1024.0) {
            return "$paramDouble bytes"
        }
        if (d1 < 1024.0) {
            return BigDecimal(d1).setScale(2, 4).toString() + " Kb"
        }
        return if (d2 < 1024.0) {
            BigDecimal(d2).setScale(2, 4).toString() + " Mb"
        } else BigDecimal(d3).setScale(2, 4).toString() + " Gb"
    }

    @SuppressLint("SimpleDateFormat")
    fun convertDuration(duration: Long): String {
        return if (duration >= 3600000)
            SimpleDateFormat("hh:mm:ss").format(duration)
        else
            SimpleDateFormat("mm:ss").format(duration)
    }

    fun convertDurationFfmpeg(duration: Long): String {
        return if (duration >= 3600000)
            SimpleDateFormat("hh:mm:ss").format(duration)
        else
            SimpleDateFormat("hh:mm:ss").format(duration)
    }

    @SuppressLint("SimpleDateFormat")
    fun convertDurationMusic(duration: Long): String {
        return if (duration >= 3600000)
            SimpleDateFormat("hh:mm:ss").format(duration)
        else
            SimpleDateFormat("mm:ss").format(duration)
    }


    fun share(activity: BaseActivity<*>, path: String) {
        val f = File(path)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                activity, BuildConfig.APPLICATION_ID + ".provider", f
            )
        } else {
            Uri.fromFile(f.absoluteFile)
        }
        ShareCompat.IntentBuilder.from(activity)
            .setStream(uri)
            .setType("video/*")
            .setChooserTitle(activity.getString(R.string.share_to))
            .startChooser()
    }

    fun rename(
        activity: BaseActivity<*>,
        file: File, uri: Uri?,
        newName: String,
        listener: ((String) -> Unit
        ),
        pathNewListener: ((String) -> Unit)
    ) {
        val name = "${getNameWithoutExtension(file.path)}"
        val toFile = File(file.path.replace(name, newName))
        if (toFile.exists()) {
            listener.invoke(Constants.STATUS_EXISTS)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (uri != null) {
                    try {
                        val contentValues = ContentValues()
                        contentValues.put(
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            "${newName}.mp4"
                        )
                        activity.contentResolver.update(uri, contentValues, null)
                        scanFile(toFile.path, activity) {
                            pathNewListener.invoke(toFile.path)
                            listener.invoke(Constants.STATUS_SUCCESS)
                        }
                    } catch (e: java.lang.Exception) {
                        val contentResolver = activity.contentResolver
                        val collection = ArrayList<Uri>()
                        collection.add(uri)
                        val pendingIntent =
                            MediaStore.createWriteRequest(contentResolver, collection)
                        val sender = pendingIntent.intentSender
                        val request = IntentSenderRequest.Builder(sender)
                            .build()
                        activity.launcherRename.launch(request)
                    }
                }
            } else {
                val success = file.renameTo(toFile)
                if (success) {
                    scanFile(file.path, activity) {
                        scanFile(toFile.path, activity) {
                            pathNewListener.invoke(toFile.path)
                            listener.invoke(Constants.STATUS_SUCCESS)
                        }
                    }
                } else {
                    listener.invoke(Constants.STATUS_FAIL)
                }
            }
        }
    }


    fun getNameWithoutExtension(path: String): String {
        return try {
            val index = path.lastIndexOf("/")
            val fileName = if (index == -1) path else path.substring(index + 1)
            val dotIndex = fileName.lastIndexOf('.')
            if (dotIndex == -1) fileName else fileName.substring(0, dotIndex)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            File(path).name
        }
    }

}
