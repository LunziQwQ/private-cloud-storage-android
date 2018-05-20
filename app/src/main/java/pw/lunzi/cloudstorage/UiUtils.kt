package pw.lunzi.cloudstorage

import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog

class UiUtils {
    companion object {
        fun showNetworkError(context: Context){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.alert_nointernet))
            builder.setNegativeButton(context.getString(R.string.word_gotit), { _, _ -> })
            builder.create().show()
        }

        fun showNeedLoginAlert(context: Context){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.alert_login))
            builder.setPositiveButton(context.getString(R.string.word_login), { _, _ ->
                context.startActivity(Intent("Login"))
            })
            builder.setNegativeButton(context.getString(R.string.word_cancle), { _, _ -> })
            builder.create().show()
        }

        fun showLoginSuccess(context: Context){
            val builder = AlertDialog.Builder(context)
            builder.setTitle("LoginSuccess")
            builder.setNegativeButton(context.getString(R.string.word_gotit), { _, _ -> })
            builder.create().show()
        }
    }
}