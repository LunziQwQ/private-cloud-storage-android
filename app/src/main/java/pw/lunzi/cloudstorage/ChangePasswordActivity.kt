package pw.lunzi.cloudstorage

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.view.View

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
    }

    fun confirmOnClick(view: View) {
        val oldPasswordLayout = findViewById<TextInputLayout>(R.id.oldPasswordLayout)
        val newPasswordLayout = findViewById<TextInputLayout>(R.id.newPasswordLayout)
        val repeatLayout = findViewById<TextInputLayout>(R.id.repeatLayout)

        val oldPasswordEditText = findViewById<MyPasswordEditText>(R.id.editText_oldPassword)
        val newPasswordEditText = findViewById<MyPasswordEditText>(R.id.editText_newPassword)
        val repeatEditText = findViewById<MyPasswordEditText>(R.id.editText_repeat)

        repeatLayout.isErrorEnabled = false
        oldPasswordLayout.isErrorEnabled = false
        newPasswordLayout.isErrorEnabled = false

        if (oldPasswordEditText.text.isEmpty()) {
            oldPasswordEditText.startShakeAnimation()
            oldPasswordLayout.error = "Old password can't be empty"
            return
        }
        if (newPasswordEditText.text.isEmpty()) {
            newPasswordEditText.startShakeAnimation()
            newPasswordLayout.error = "New password can't be empty"
            return
        }

        if (newPasswordEditText.text.toString() != repeatEditText.text.toString()) {
            repeatEditText.startShakeAnimation()
            repeatLayout.error = "The password you entered twice is inconsistent"
            return
        }

        Thread(Runnable {
            if (ApiUtils.get().changeMyPassword(oldPasswordEditText.text.toString(), newPasswordEditText.text.toString())) {
                runOnUiThread {
                    UiUtils.showChangePasswordSuccess(this, this)
                }
            } else {
                runOnUiThread{
                    UiUtils.showError(this, "Change password failed. Try again.")
                }
            }

        }).start()

    }
}
