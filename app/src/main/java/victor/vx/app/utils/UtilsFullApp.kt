package victor.vx.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.ybq.android.spinkit.SpinKitView
import eu.chainfire.libsuperuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import victor.vx.app.LogsApp
import victor.vx.app.MainActivity.Companion.mainActivityView
import victor.vx.app.R
import java.util.jar.Manifest


class UtilsFullApp : AppCompatActivity() {

    fun isVisibleIconLoading(isVisible: Boolean) {
        runOnUiThread {
            try {
                val spinKitView : SpinKitView? = mainActivityView?.findViewById(R.id.spin_kit)
                val viewLoading : View? = mainActivityView?.findViewById(R.id.background_view)
                val viewMainActivity : View? = mainActivityView?.findViewById(R.id.bottom_navigation)

                viewLoading?.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
                spinKitView?.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
                viewLoading?.isClickable = isVisible
                viewMainActivity?.isClickable = isVisible
            } catch (error: Exception) {
                LogsApp().Logs("Error isVisibleIconLoading: ${error.message.toString()}")
            }
        }
    }

    fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        val androidId: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId
    }

    fun getBuildID(): String {
        return Build.ID
    }

    suspend fun checkRootAccess(): Boolean {
        return withContext(Dispatchers.IO) { Shell.SU.available() }
    }

}