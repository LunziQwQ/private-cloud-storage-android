package pw.lunzi.cloudstorage

import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow

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

        fun showLoginSuccess(context: Context, activity: AppCompatActivity) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.word_loginsuccess)
            builder.setPositiveButton(context.getString(R.string.word_gotit), { _, _ -> activity.finish() })
            builder.create().show()
        }

        fun showEditAlert(context: Context, nowPath: String, activity: MainActivity) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.tips_inputName)
            val editText = EditText(context)
            builder.setView(editText)
            builder.setPositiveButton(context.getString(R.string.word_gotit), { _, _ ->
                activity.mkdir(editText.text.toString(), nowPath)
            })
            builder.setNegativeButton(context.getString(R.string.word_cancle), { _, _ -> })
            builder.create().show()
        }

        fun canMkdirAndUpload(isEnable: Boolean, activity: MainActivity) {
            activity.findViewById<Button>(R.id.btn_mkdir).isEnabled = isEnable
            activity.findViewById<Button>(R.id.btn_mkdir).alpha = if (isEnable) 1F else 0.3F
            activity.findViewById<Button>(R.id.btn_upload).isEnabled = isEnable
            activity.findViewById<Button>(R.id.btn_upload).alpha = if (isEnable) 1F else 0.3F


        }
    }
}