package victor.vx.app.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import org.json.JSONObject
import victor.vx.app.LogsApp
import victor.vx.app.R
import victor.vx.app.callbacks.CallbackConnectionApp
import victor.vx.app.connections.ServerConnectionApp
import victor.vx.app.prefs.PrefsApp
import victor.vx.app.utils.UtilsAlertApp
import victor.vx.app.utils.UtilsFullApp

class AccountFragment(private val context: Context) : Fragment(), CallbackConnectionApp.CallbackRequest {

    companion object {
        init {
            System.loadLibrary("app")
        }
    }

    private external fun urlAuthorization(): String

    private var btnGenerateToken: Button? = null
    private val connectApp = ServerConnectionApp()
    private var utilsAlert = UtilsAlertApp(context)
    private var prefsApp = PrefsApp(context)
    private var utilsFullApp = UtilsFullApp()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_account, container, false)

        startElementID(view)

        val token = arguments?.getString("token")
        if (token != null) {
            utilsFullApp.isVisibleIconLoading(true)

            prefsApp.saveString("token-account", token)
            connectApp.CreateAccountPost(token, this)
        }

        btnGenerateToken?.setOnClickListener {
            var uri = urlAuthorization()
            val tokenDetect = prefsApp.getString("token-account")

            if (tokenDetect != null) uri += "?detect=${tokenDetect}"

            val intentUrl = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(uri)
            )

            startActivity(intentUrl)

            btnGenerateToken?.isClickable = false
            btnGenerateToken?.setBackgroundResource(R.drawable.background_button2)
        }

        return view
    }

    private fun resetButton() {
        btnGenerateToken?.isClickable = true
        btnGenerateToken?.setBackgroundResource(R.drawable.background_button)
    }

    private fun startElementID(view: View) {
        btnGenerateToken = view.findViewById(R.id.btn_generate_token)
    }

    override fun onServerResponse(fieldName: String?, value: String?) {
        activity?.runOnUiThread {
            utilsFullApp.isVisibleIconLoading(false)

            if ("message" == fieldName) value?.let {
                utilsAlert.ShowAlertSuccess("Sucesso!", it, "OK", true, null)
            }

            if ("data" == fieldName) {
                try {
                    val data = value?.let { JSONObject(it) }

                    data.let {
                        prefsApp.saveString("username", data!!.getString("user"))
                        prefsApp.saveString("password", data.getString("password"))

                        utilsFullApp.showToast("Authenticated success.", context)
                    }
                } catch (error: Exception) {
                    LogsApp().Logs("Error data.Value: ${error.message}")
                }
            }
        }
    }

    override fun onServerError(errorFieldName: String?, defaultValue: String?) {
        activity?.runOnUiThread {
            utilsFullApp.isVisibleIconLoading(false)

            if ("message" == errorFieldName) defaultValue?.let {
                utilsAlert.ShowAlertFailed("", it, "OK", true, null)
            }
        }
    }
}