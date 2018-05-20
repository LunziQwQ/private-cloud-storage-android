package pw.lunzi.cloudstorage

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginOnClick(view: View) {
        val myPasswordEditText = findViewById<MyPasswordEditText>(R.id.editText_password)
        val myUsernameEditText = findViewById<MyEditText>(R.id.editText_username)
        val usernameLayout = findViewById<TextInputLayout>(R.id.usernameLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)

        usernameLayout.isErrorEnabled = false
        passwordLayout.isErrorEnabled = false

        if (myUsernameEditText.text.isEmpty()) {
            myUsernameEditText.startShakeAnimation()
            usernameLayout.error = getString(R.string.tips_usernameempty)
            return
        }
        if (myPasswordEditText.text.isEmpty()) {
            myPasswordEditText.startShakeAnimation()
            passwordLayout.error = getString(R.string.tips_passwordempty)
            return
        }

        Thread(Runnable {
            if (ApiUtils.get().getUser(myUsernameEditText.text.toString()) == null) {
                runOnUiThread {
                    myUsernameEditText.startShakeAnimation()
                    usernameLayout.error = getString(R.string.tips_username)
                }
                return@Runnable
            }

            if (!ApiUtils.get().login(myUsernameEditText.text.toString(), myPasswordEditText.text.toString())) {
                runOnUiThread {
                    myPasswordEditText.startShakeAnimation()
                    passwordLayout.error = getString(R.string.tips_password)
                }
            } else {
                runOnUiThread {
                    UiUtils.showLoginSuccess(this)
                }
            }
        }).start()
    }

    fun registerOnClick(view: View){

    }

}