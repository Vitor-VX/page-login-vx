package victor.vx.app.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.Fragment
import org.json.JSONObject
import victor.vx.app.home.HomeActivity
import victor.vx.app.LogsApp
import victor.vx.app.R
import victor.vx.app.callbacks.CallbackConnectionApp
import victor.vx.app.connections.ServerConnectionApp
import victor.vx.app.home.RootHelper
import victor.vx.app.prefs.PrefsApp
import victor.vx.app.utils.UtilsAlertApp
import victor.vx.app.utils.UtilsFullApp

class LoginFragment(private val context: Context) : Fragment(),
    CallbackConnectionApp.CallbackRequest {

    private val connectApp = ServerConnectionApp()
    private var txtUsername: EditText? = null
    private var txtPassword: EditText? = null
    private var chkSaveLogin: CheckBox? = null
    private var btnMobile: AppCompatCheckBox? = null
    private var btnEmulator: AppCompatCheckBox? = null
    private var btnLogin: Button? = null
    private var utilsApp = UtilsFullApp()
    private var utilsAlertApp = UtilsAlertApp(context)
    private var rootHelper = RootHelper(context)
    private val prefsApp = PrefsApp(context)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        startElementID(view)

        val usernameExist = prefsApp.getString("username")
        val passwordExist = prefsApp.getString("password")

        if (!usernameExist.isNullOrEmpty() && !passwordExist.isNullOrEmpty()) {
            txtUsername?.setText(usernameExist)
            txtPassword?.setText(passwordExist)
            chkSaveLogin?.isChecked = true
        }

        val savedChoice = prefsApp.getString("user_choice")
        savedChoice?.let {
            if (it == "Mobile") {
                selectButton(btnMobile!!, btnEmulator!!)
            } else if (it == "Emulador") {
                selectButton(btnEmulator!!, btnMobile!!)
            }
        }

        btnMobile?.setOnClickListener {
            selectButton(btnMobile!!, btnEmulator!!)
            checkLoginButtonEnabled()
        }

        btnEmulator?.setOnClickListener {
            selectButton(btnEmulator!!, btnMobile!!)
            checkLoginButtonEnabled()
        }

        btnLogin?.setOnClickListener {
            if (!checkLoginButtonEnabled()) {
                utilsApp.showToast("Selecione sua arquitetura: Mobile/Emulador.", context)

                return@setOnClickListener
            }

            utilsApp.isVisibleIconLoading(true)

            val username = txtUsername?.text.toString()
            val password = txtPassword?.text.toString()
            val deviceId = utilsApp.getDeviceId(context)
            val deviceBuildID = utilsApp.getBuildID()
            val architectureDevice = prefsApp.getString("user_choice")

            if (chkSaveLogin?.isChecked!!) {
                prefsApp.saveString("username", username)
                prefsApp.saveString("password", password)
            }

            connectApp.LoginPost(username, password, deviceId, deviceBuildID, architectureDevice!!, this)
        }

        return view
    }

    override fun onServerResponse(fieldName: String?, value: String?) {
        activity?.runOnUiThread {
            if ("data" == fieldName) {
                try {
                    val data = value?.let { JSONObject(it) }
                    data.let {
                        utilsApp.isVisibleIconLoading(false)

                        utilsAlertApp.ShowAlertSuccess(
                            "Sucesso!",
                            "Seja bem vindo ${data!!.getString("username")}! Validade login: ${data.getString("data")}.",
                            "OK",
                            false,
                            onClickListener = {
                                val k = data.getString("key")
                                val intentHomeActivity = Intent(context, HomeActivity::class.java)
                                startActivity(intentHomeActivity)
                                createCheck(k)

                                prefsApp.saveString("k", k)

                                activity?.finish()
                            })

                    }
                } catch (error: Exception) {
                    LogsApp().Logs("Error data.Value: ${error.message}")
                }
            }
        }
    }

    override fun onServerError(errorFieldName: String?, defaultValue: String?) {
        activity?.runOnUiThread {
            utilsApp.isVisibleIconLoading(false)

            if ("message" == errorFieldName) defaultValue?.let {
                utilsAlertApp.ShowAlertFailed("Oops! Algo deu errado.", it, "OK", true, null)
            }
        }
    }

    private fun startElementID(view: View) {
        txtUsername = view.findViewById(R.id.txt_username)
        txtPassword = view.findViewById(R.id.txt_password)
        chkSaveLogin = view.findViewById(R.id.chk_save_login)
        btnMobile = view.findViewById(R.id.btn_mobile)
        btnEmulator = view.findViewById(R.id.btn_emulator)
        btnLogin = view.findViewById(R.id.btn_login)
    }

    private fun createCheck(value: String) {
        rootHelper.exec("echo $value > /data/v.txt")
        rootHelper.exec("chmod 777 /data/v.txt")
    }

    private fun checkLoginButtonEnabled(): Boolean {
        if (btnMobile?.isChecked == true || btnEmulator?.isChecked == true) {
            btnLogin?.isEnabled = true

            return true
        }

        return false
    }

    private fun selectButton(selectedButton: AppCompatCheckBox, otherButton: AppCompatCheckBox) {
        selectedButton.isChecked = true
        otherButton.isChecked = false

        val choice = if (selectedButton == btnMobile) "Mobile" else "Emulador"
        prefsApp.saveString("user_choice", choice)

        checkLoginButtonEnabled()
    }
}