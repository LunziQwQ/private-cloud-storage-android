package pw.lunzi.cloudstorage

import android.content.Context
import android.content.Intent
import android.support.design.internal.BottomNavigationItemView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.PopupWindow
import java.security.cert.CertPath

class UiUtils {
    companion object {

        fun showError(context: Context, title: String) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setNegativeButton(context.getString(R.string.word_gotit), { _, _ -> })
            builder.create().show()
        }

        fun showNetworkError(context: Context) {
            showError(context, context.getString(R.string.alert_nointernet))
        }

        fun showNeedLoginAlert(context: Context, activity: MainActivity) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.alert_login))
            builder.setPositiveButton(context.getString(R.string.word_login)) { _, _ ->
                context.startActivity(Intent("Login"))
            }
            builder.setNegativeButton(context.getString(R.string.word_cancle)) { _, _ ->
                activity.findViewById<BottomNavigationItemView>(R.id.navigation_common).performClick()
            }
            builder.create().show()
        }

        fun showLoginSuccess(context: Context, activity: AppCompatActivity) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.word_loginsuccess)
            builder.setPositiveButton(context.getString(R.string.word_gotit)) { _, _ -> activity.finish() }
            builder.create().show()
        }

        fun showChangePasswordSuccess(context: Context, activity: AppCompatActivity) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Change password success")
            builder.setPositiveButton(context.getString(R.string.word_gotit)) { _, _ -> activity.finish() }
            builder.create().show()
        }

        fun showRegisterSuccess(context: Context, activity: AppCompatActivity) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.word_register_success)
            builder.setPositiveButton(context.getString(R.string.word_gotit), { _, _ -> activity.finish() })
            builder.create().show()
        }

        fun showEditAlert(context: Context, nowPath: String, activity: MainActivity) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.tips_inputName)
            val editText = EditText(context)
            builder.setView(editText)
            builder.setPositiveButton(context.getString(R.string.word_gotit)) { _, _ ->
                activity.mkdir(editText.text.toString(), nowPath)
            }
            builder.setNegativeButton(context.getString(R.string.word_cancle), { _, _ -> })
            builder.create().show()
        }

        fun showChangeAccessAlert(context: Context, activity: MainActivity, path: String, name: String, ispublic: Boolean) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("更改文件夹权限")
            val checkBox = CheckBox(context)
            checkBox.text= "递归更改所有子文件"
            builder.setView(checkBox)
            builder.setPositiveButton(context.getString(R.string.word_gotit)) { _, _ ->
                activity.changeItemAccess(path, name, checkBox.isChecked, ispublic)
            }
            builder.setNegativeButton(context.getString(R.string.word_cancle), { _, _ -> })
            builder.create().show()
        }

        fun showRenameAlert(context: Context, nowPath: String, oldName: String, activity: MainActivity) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Rename item")
            val editText = EditText(context)
            builder.setView(editText)
            builder.setPositiveButton(context.getString(R.string.word_gotit)) { _, _ ->
                if(FileItem.myFileItemList.filter { it.itemName == editText.text.toString() }.count() > 0){
                    showError(context, "目录下已存在该名称，重命名失败")
                }else{
                    activity.renameItem(editText.text.toString(), nowPath, oldName)
                }
            }
            builder.setNegativeButton(context.getString(R.string.word_cancle), { _, _ -> })
            builder.create().show()
        }
    }
}