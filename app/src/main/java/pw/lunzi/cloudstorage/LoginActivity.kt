package pw.lunzi.cloudstorage

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.EditText

class LoginActivity : AppCompatActivity() {
    companion object {
        var isLogin = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginOnClick(){
        startActivity(Intent("android.intent.action.MAIN"))
        val username = findViewById<EditText>(R.id.editText_Username).text
        val password = findViewById<EditText>(R.id.editText_password).text
        if (username.isEmpty() || password.isEmpty()) {
        }
    }

    fun registerOnClick(){

    }



}