package victor.vx.app.home

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import eu.chainfire.libsuperuser.Shell
import eu.chainfire.libsuperuser.Shell.ShellDiedException
import victor.vx.app.prefs.PrefsApp
import victor.vx.app.utils.UtilsFullApp
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStreamReader


class RootHelper(private val context: Context) : AppCompatActivity() {
    private val prefsApp = PrefsApp(context)
    private val utilsApp = UtilsFullApp()
    private val abiSubDir: String = if (prefsApp.getString("user_choice") == "Emulador") "x86_64" else "arm64"

    fun getAppPath(context: Context, packageName: String?): String? {
        try {
            val applicationInfo = context.packageManager.getApplicationInfo(
                packageName!!, 0
            )
            return File(applicationInfo.sourceDir).parent
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun copyLibToDest(context: Context, destPath: String) {
        try {
            val libName = "libmain.so"
            checkAbiSubDir()

            val sourcePath = context.applicationInfo.nativeLibraryDir + "/" + libName
            val destinationPath = "$destPath/lib/$abiSubDir/"
            val cmd = "cp $sourcePath $destinationPath"
            exec(cmd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPermissions(filePath: String) {
        checkAbiSubDir()

        exec("chmod 755 $filePath/lib/$abiSubDir/libmain.so")
    }

    fun renameLib(appPath: String) {
        try {
            checkAbiSubDir()

            val libDir = "$appPath/lib/$abiSubDir/"
            val realMainLib = File(libDir, "librealmain.so")

            if (!realMainLib.exists()) {
                val lib = "libmain.so"

                val command = "mv " + libDir + lib + " " + libDir + "librealmain.so"
                exec(command)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkAbiSubDir() {
        if (prefsApp.getString("user_choice").toString().isEmpty()) {
            utilsApp.showToast("abiSubDir not found.", this)

            return
        }
    }

    fun exec(cmd: String) {
        Thread {
            var process: Process? = null
            var os: DataOutputStream? = null
            var `is`: BufferedReader? = null
            val result = StringBuilder()
            try {
                process = Runtime.getRuntime().exec("su")
                os = DataOutputStream(process.outputStream)
                `is` = BufferedReader(InputStreamReader(process.inputStream))
                os.writeBytes(cmd + "\n")
                os.writeBytes("exit\n")
                os.flush()
                var line: String?
                while ((`is`.readLine().also { line = it }) != null) {
                    result.append(line).append("\n")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    os?.close()
                    process?.destroy()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            val commandOutput = result.toString()
        }.start()
    }

    @Throws(ShellDiedException::class)
    fun getPid(target: String): Int {
        val stdout = ArrayList<String>()
        Shell.Pool.SU.run(
            "(toolbox ps; toolbox ps -A; toybox ps; toybox ps -A) | grep \" $target$\"",
            stdout,
            null,
            false
        )

        for (line in stdout) {
            var trimmedLine = line.trim()
            while (trimmedLine.contains("  ")) {
                trimmedLine = trimmedLine.replace("  ", " ")
            }
            val parts = trimmedLine.split(" ")
            if (parts.size >= 2) {
                try {
                    return parts[1].toInt()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        }
        return -1
    }
}