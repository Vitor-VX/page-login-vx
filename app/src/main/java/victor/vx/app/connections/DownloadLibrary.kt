package victor.vx.app.connections

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.*
import victor.vx.app.home.RootHelper
import java.io.File

interface DownloadManagerProvider {
    fun enqueue(request: DownloadManager.Request): Long
    fun getExternalFilesDir(type: String): File?
    fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter)
    fun unregisterReceiver(receiver: BroadcastReceiver)
    fun getContext(): Context
}

class DownloadManagerProviderImpl(private val context: Context) : DownloadManagerProvider {
    override fun enqueue(request: DownloadManager.Request): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }

    override fun getExternalFilesDir(type: String): File? {
        return context.getExternalFilesDir(type)
    }

    override fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        context.registerReceiver(receiver, filter)
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver) {
        context.unregisterReceiver(receiver)
    }

    override fun getContext(): Context {
        return context
    }
}

interface FileMover {
    fun moveFile(sourcePath: String, destPath: String)
}

class RootFileMover(private val rootHelper: RootHelper) : FileMover {
    override fun moveFile(sourcePath: String, destPath: String) {
        rootHelper.exec("mv $sourcePath $destPath")
    }
}

class DownloadLibrary(
    private val downloadManagerProvider: DownloadManagerProvider,
    private val fileMover: FileMover
) {

    fun downloadRequest(urlLibrary: String, destPath: String, abiSubDir: String, callback: (Boolean?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = DownloadManager.Request(Uri.parse(urlLibrary)).apply {
                setTitle("Downloading Library")
                setDescription("Downloading Library file")
                setDestinationInExternalFilesDir(downloadManagerProvider.getContext(),
                    Environment.DIRECTORY_DOWNLOADS, "libmain.so")
            }

            val downloadId = downloadManagerProvider.enqueue(request)

            withContext(Dispatchers.Main) {
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (downloadId == id) {
                            val destinationPath = "$destPath/lib/$abiSubDir/libmain.so"
                            val directoryPath = "${downloadManagerProvider.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath}/libmain.so"
                            fileMover.moveFile(directoryPath, destinationPath)
                            downloadManagerProvider.unregisterReceiver(this)

                            callback(true)
                        }
                    }
                }

                downloadManagerProvider.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }
        }
    }
}