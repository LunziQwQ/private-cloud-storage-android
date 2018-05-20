package pw.lunzi.cloudstorage

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*
import java.net.ConnectException

class MainActivity : AppCompatActivity() {

    private var path = ApiUtils.rootPathUrl

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_common -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myspace -> {
                if (!ApiUtils.isLogin) {
                    UiUtils.showNeedLoginAlert(this)
                }
                return@OnNavigationItemSelectedListener ApiUtils.isLogin
            }
            R.id.navigation_notifications -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun mkdirOnClick(view: View) {


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        showList(path)
    }

    private fun showList(path: String) {
        Thread(Runnable {
            try {
                val itemList = ApiUtils.get().getItemsWithoutLogin(path)
                val nameList = itemList.map { it.itemName }
                runOnUiThread { findViewById<ListView>(R.id.itemList).adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nameList) }
            } catch (e: ConnectException) {
                runOnUiThread{ UiUtils.showNetworkError(this) }
            }
        }).start()
    }


}
