package pw.lunzi.cloudstorage

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_common -> {
//                message.setText(R.string.title_common)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myspace -> {
                if(!LoginActivity.isLogin) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.alert_login))
                    builder.setPositiveButton(getString(R.string.word_login), { _, _ ->
                        startActivity(Intent("Login"))
                    })
                    builder.setNegativeButton(getString(R.string.word_cancle), { _, _ -> })
                    builder.create().show()
                }
                return@OnNavigationItemSelectedListener LoginActivity.isLogin
            }
            R.id.navigation_notifications -> {
//                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
