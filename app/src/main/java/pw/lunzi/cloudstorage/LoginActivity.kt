package pw.lunzi.cloudstorage

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.view.View

class LoginActivity : AppCompatActivity() {
    companion object {
        var isLogin = false
        var session = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginOnClick(view: View) {
        val myPasswordEditText = findViewById<MyPasswordEditText>(R.id.editText_password)
        myPasswordEditText.startShakeAnimation()
        findViewById<TextInputLayout>(R.id.layout).error = getString(R.string.tips_password)
    }

    fun registerOnClick(view: View){

    }

}