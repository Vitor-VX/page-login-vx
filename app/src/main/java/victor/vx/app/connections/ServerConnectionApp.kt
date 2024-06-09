package victor.vx.app.connections

import com.google.gson.JsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import victor.vx.app.LogsApp
import victor.vx.app.fragments.AccountFragment
import victor.vx.app.fragments.LoginFragment
import victor.vx.app.home.HomeActivity
import victor.vx.app.utils.UtilsFullApp
import java.io.IOException

class ServerConnectionApp {

    companion object {
        init {
            System.loadLibrary("app")
        }
    }

    private external fun nativeUrlApi(): String

    private val client = OkHttpClient()
    private val urlApi = nativeUrlApi()

    fun LoginPost(username: String, password: String, deviceId: String, deviceBuildID: String, architectureDevice: String, callback: LoginFragment) {
        val jsonData = JsonObject().apply {
            addProperty("username", username)
            addProperty("password", password)
            addProperty("deviceId", deviceId)
            addProperty("deviceBuildID", deviceBuildID)
            addProperty("architectureDevice", architectureDevice)
        }
        makePostRequest("${urlApi}/login", jsonData, callback)
    }

    fun CreateAccountPost(token: String, callback: AccountFragment) {
        val jsonData = JsonObject().apply {
            addProperty("token", token)
        }
        makePostRequest("${urlApi}/create-user", jsonData, callback)
    }

    fun DownloadGet(v: String, deviceId: String, callback: (String?, String?) -> Unit) {
        val urlWithParams = "$urlApi/library?v=$v&deviceId=$deviceId"

        val request = Request.Builder()
            .url(urlWithParams)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, null)
                LogsApp().Logs("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                        callback(null, null)
                        return
                    }
                    val jsonObject = JSONObject(responseBody)
                    val url = jsonObject.getString("link")
                    val message = jsonObject.getString("message")

                    callback(url, message)
                } catch (error: Exception) {
                    callback(null, null)
                    LogsApp().Logs("Error: ${error.message}")
                }
            }
        })
    }


    private fun makePostRequest(url: String, jsonData: JsonObject, callback: Any) {
        val requestBody = jsonData.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handleFailure(e, callback)
            }

            override fun onResponse(call: Call, response: Response) {
                handleResponse(response, callback)
            }
        })
    }

    private fun handleFailure(e: IOException, callback: Any) {
        when (callback) {
            is LoginFragment -> callback.onServerError("error", e.message.toString())
            is AccountFragment -> callback.onServerError("error", e.message.toString())
        }
        LogsApp().Logs("Error: ${e.message}")
    }

    private fun handleResponse(response: Response, callback: Any) {
        try {
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            val jsonObject = JSONObject(responseBody)

            if (!response.isSuccessful) {
                for (errorFieldName in jsonObject.keys()) {
                    val valueError = jsonObject.optString(errorFieldName, null.toString())
                    when (callback) {
                        is LoginFragment -> callback.onServerError(errorFieldName, valueError)
                        is AccountFragment -> callback.onServerError(errorFieldName, valueError)
                    }
                }
                return
            }

            for (fieldName in jsonObject.keys()) {
                val value = jsonObject.optString(fieldName, null.toString())
                when (callback) {
                    is LoginFragment -> callback.onServerResponse(fieldName, value)
                    is AccountFragment -> callback.onServerResponse(fieldName, value)
                }
            }
        } catch (error: Exception) {
            LogsApp().Logs("Error: ${error.message}")
        }
    }
}
