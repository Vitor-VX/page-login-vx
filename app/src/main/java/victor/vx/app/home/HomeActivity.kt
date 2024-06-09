package victor.vx.app.home

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import eu.chainfire.libsuperuser.Shell
import kotlinx.coroutines.*
import victor.vx.app.LogsApp
import victor.vx.app.R
import victor.vx.app.connections.DownloadLibrary
import victor.vx.app.connections.DownloadManagerProviderImpl
import victor.vx.app.connections.RootFileMover
import victor.vx.app.connections.ServerConnectionApp
import victor.vx.app.prefs.PrefsApp
import victor.vx.app.utils.UtilsFullApp
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import android.Manifest


class HomeActivity : AppCompatActivity() {

    private val appPackage = "com.dts.freefireth"
    private var handler = Handler(Looper.getMainLooper())
    private var rootHelper: RootHelper? = null
    private var utilsFullApp = UtilsFullApp()
    private var prefsApp: PrefsApp? = null
    private val connectionApp = ServerConnectionApp()

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        prefsApp = PrefsApp(this)
        rootHelper = RootHelper(this)

        rootHelper?.exec("setenforce 0")

        val btnInject = findViewById<Button>(R.id.btn_inject)
        val txtDevice = findViewById<TextView>(R.id.txtDevice)
        val txtGameName = findViewById<TextView>(R.id.txt_game_name)

        txtGameName.text = "Game package: $appPackage"
        txtDevice.text = "Device: ${prefsApp?.getString("user_choice")}"

        btnInject.setOnClickListener {
            btnInject.isClickable = false
            btnInject.setBackgroundResource(R.drawable.background_button2)

            launchInjectionProcess()
        }
    }

    private fun launchInjectionProcess() {
        lifecycleScope.launch {
            if (!utilsFullApp.checkRootAccess()) {
                utilsFullApp.showToast("Root not found", this@HomeActivity)
                return@launch
            }

            if (prefsApp?.getString("user_choice") == "Emulador") {
                injectInEmulator()
            } else {
              injectInMobile()
            }
        }
    }

    // FakeLib-Inject - https://github.com/Vitor-VX/FakeLib-Inject
    private fun injectInEmulator() {
        val v = prefsApp?.getString("k")
        val deviceId = utilsFullApp.getDeviceId(this@HomeActivity)

        if (v != null) {
            try {
                connectionApp.DownloadGet(v, deviceId) { url, message ->
                    if (url != null && message != null) {
                        ConfigLibMain(url, "x86_64")

                        runOnUiThread { utilsFullApp.showToast(message, this@HomeActivity) }
                    } else {
                        utilsFullApp.showToast("Failed to get download URL", this@HomeActivity)
                    }
                }
            } catch (error: Exception) {
                LogsApp().Logs("Failed injectInEmulator: ${error.message}")
            }
        }
    }

    // FakeLib-Inject - https://github.com/Vitor-VX/FakeLib-Inject
    private fun injectInMobile() {
        val v = prefsApp?.getString("k")
        val deviceId = utilsFullApp.getDeviceId(this@HomeActivity)

        if (v != null) {
            try {
                connectionApp.DownloadGet(v, deviceId) { url, message ->
                    if (url != null && message != null) {
                        ConfigLibMain(url, "arm64")

                        runOnUiThread { utilsFullApp.showToast(message, this@HomeActivity) }
                    } else {
                        utilsFullApp.showToast("Failed to get download URL", this@HomeActivity)
                    }
                }
            } catch (error: Exception) {
                LogsApp().Logs("Failed injectInMobile: ${error.message}")
            }
        }
    }

    // FakeLib-Inject - https://github.com/Vitor-VX/FakeLib-Inject
    private fun ConfigLibMain(url: String, abiSurDir: String) {
        val appPath: String? = rootHelper?.getAppPath(this@HomeActivity, appPackage)

        if (appPath != null) {
            rootHelper?.renameLib(appPath)
            val delaySetPermission: Long = 2000
            val delayOpenApp: Long = 4000

            val downloadManagerProvider = DownloadManagerProviderImpl(this@HomeActivity)
            val fileMover = RootFileMover(rootHelper!!)
            val downloadLibrary = DownloadLibrary(downloadManagerProvider, fileMover)

            downloadLibrary.downloadRequest(url, appPath, abiSurDir) {
                if (it == true) {
                    handler.postDelayed({ rootHelper?.setPermissions(appPath) }, delaySetPermission)
                    handler.postDelayed(this@HomeActivity::openApp, delayOpenApp)
                }
            }
        } else {
            utilsFullApp.showToast("App path not found", this@HomeActivity)
        }
    }

    private fun openApp() {
        val pm = baseContext.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(appPackage)
        if (launchIntent != null) {
            baseContext.startActivity(launchIntent)
        } else {
            Log.e("Error", "Failed to launch app: $appPackage")
        }
    }
}