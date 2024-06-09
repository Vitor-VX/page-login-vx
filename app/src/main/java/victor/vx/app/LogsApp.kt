package victor.vx.app

import android.util.Log

class LogsApp {

    private var TAG = "VxApp"

    fun Logs(message: String) {
        Log.v(TAG, message)
    }

}