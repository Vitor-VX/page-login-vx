package victor.vx.app.utils

import android.content.Context
import cn.pedant.SweetAlert.SweetAlertDialog

class UtilsAlertApp(private val context: Context) {

    private var pDialog : SweetAlertDialog? = null

    fun ShowAlertSuccess(setTitleText: String, setContentText: String, setConfirmText: String, setCancelable: Boolean, onClickListener: SweetAlertDialog.OnSweetClickListener?) {
        DismissSweetAlert()

        pDialog = getSweetAlert(SweetAlertDialog.SUCCESS_TYPE)
        pDialog?.setTitleText(setTitleText)
        pDialog?.setContentText(setContentText)
        pDialog?.setConfirmText(setConfirmText)
        pDialog?.setCancelable(setCancelable)
        onClickListener.let {
            pDialog?.setConfirmClickListener(onClickListener)
        }

        pDialog?.show()
    }

    fun ShowAlertFailed(setTitleText: String, setContentText: String, setConfirmText: String, setCancelable: Boolean, onClickListener: SweetAlertDialog.OnSweetClickListener?) {
        DismissSweetAlert()

        pDialog = getSweetAlert(SweetAlertDialog.ERROR_TYPE)
        pDialog?.setTitleText(setTitleText)
        pDialog?.setContentText(setContentText)
        pDialog?.setConfirmText(setConfirmText)
        pDialog?.setCancelable(setCancelable)
        onClickListener.let {
            pDialog?.setConfirmClickListener(onClickListener)
        }

        pDialog?.show()
    }

    private fun DismissSweetAlert() {
        pDialog.let {
            pDialog?.dismiss()
            pDialog = null
        }
    }

    private fun getSweetAlert(typeAlert: Int) : SweetAlertDialog {
        return SweetAlertDialog(context, typeAlert)
    }

}