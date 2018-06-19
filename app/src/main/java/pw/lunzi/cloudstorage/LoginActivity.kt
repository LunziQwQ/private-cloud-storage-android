package pw.lunzi.cloudstorage

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        findViewById<TextInputLayout>(R.id.repeatLayout).visibility = View.INVISIBLE
    }

    fun loginOnClick(view: View) {
        val myPasswordEditText = findViewById<MyPasswordEditText>(R.id.editText_password)
        val myUsernameEditText = findViewById<MyEditText>(R.id.editText_username)
        val usernameLayout = findViewById<TextInputLayout>(R.id.usernameLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)

        val repeatLayout = findViewById<TextInputLayout>(R.id.repeatLayout)
        if (repeatLayout.visibility == View.VISIBLE) {
            repeatLayout.visibility = View.INVISIBLE
            return
        }

        repeatLayout.isErrorEnabled = false
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
                    UiUtils.showLoginSuccess(this,this)
                }
            }
        }).start()
    }

    fun registerOnClick(view: View){
        val myRepeatEditText = findViewById<MyPasswordEditText>(R.id.editText_repeat)
        val myPasswordEditText = findViewById<MyPasswordEditText>(R.id.editText_password)
        val myUsernameEditText = findViewById<MyEditText>(R.id.editText_username)
        val usernameLayout = findViewById<TextInputLayout>(R.id.usernameLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val repeatLayout = findViewById<TextInputLayout>(R.id.repeatLayout)

        if (repeatLayout.visibility == View.INVISIBLE) {
            repeatLayout.visibility = View.VISIBLE
            return
        }

        repeatLayout.isErrorEnabled = false
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

        if (myPasswordEditText.text.toString() != myRepeatEditText.text.toString()) {
            myRepeatEditText.startShakeAnimation()
            repeatLayout.error = "The password you entered twice is inconsistent"
            return
        }

        Thread(Runnable {
            if (ApiUtils.get().getUser(myUsernameEditText.text.toString()) != null) {
                runOnUiThread {
                    myUsernameEditText.startShakeAnimation()
                    usernameLayout.error = "Username already exist"
                }
                return@Runnable
            }

            if (!ApiUtils.get().register(myUsernameEditText.text.toString(), myPasswordEditText.text.toString())) {
                runOnUiThread {
                    myPasswordEditText.startShakeAnimation()
                    passwordLayout.error = getString(R.string.tips_password)
                }
            } else {
                ApiUtils.get().login(myUsernameEditText.text.toString(), myPasswordEditText.text.toString())
                runOnUiThread {
                    UiUtils.showRegisterSuccess(this,this)
                }
            }
        }).start()
    }

}