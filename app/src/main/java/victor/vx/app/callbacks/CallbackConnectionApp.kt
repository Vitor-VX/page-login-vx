package victor.vx.app.callbacks

class CallbackConnectionApp {

    interface CallbackRequest {
        fun onServerResponse(fieldName: String?, value: String?)

        fun onServerError(errorFieldName: String?, defaultValue: String?)
    }
}