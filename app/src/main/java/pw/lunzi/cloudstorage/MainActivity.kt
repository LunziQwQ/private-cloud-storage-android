package pw.lunzi.cloudstorage

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.net.ConnectException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var commonPath = "root/"
    private var myPath = ""
    private val utils = ApiUtils.get()
    private var itemList = listOf<FileItem>()
    private var nowNavigation = 0 // 0 1 2 对应三个navigation

    private fun getNowPath() = if(nowNavigation == 0) commonPath else myPath
    private fun setNowPath(path: String) {
        if(nowNavigation == 0) commonPath = path else myPath = path
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_common -> {
                nowNavigation = 0
                showList(commonPath)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myspace -> {
                nowNavigation = 1
                if (!ApiUtils.isLogin) {
                    UiUtils.showNeedLoginAlert(this)
                } else {
                    if (myPath == "") myPath = ApiUtils.userInfo!!.username + "/"
                    showList(myPath)
                }
                return@OnNavigationItemSelectedListener ApiUtils.isLogin
            }
            R.id.navigation_notifications -> {
                nowNavigation = 2
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun mkdirOnClick(view: View) {
        if (ApiUtils.isLogin) {
            UiUtils.showEditAlert(this, getNowPath(), this)
        } else {
            UiUtils.showNeedLoginAlert(this)
        }
    }

    fun mkdir(name: String, path: String) {
        Toast.makeText(this, "${ApiUtils.itemUrl}$path$name", Toast.LENGTH_SHORT).show()
        Thread(Runnable {
            if (utils.mkdir(name, path)) {
                showList(getNowPath())
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }).start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        findViewById<ListView>(R.id.itemList).setOnItemClickListener({ parent, view, position, id ->
            if (itemList.get(position).isDictionary) {
                var temp = getNowPath()
                temp += "${itemList.get(position).itemName}/"
                showList(temp)
                setNowPath(temp)
            } else {
                Thread(Runnable {
                    utils.download(getNowPath(), itemList.get(position).itemName)
                }).start()
                Toast.makeText(this,"你单击的是第"+(position+1)+"条数据,非文件夹",Toast.LENGTH_SHORT).show()
            }
        })
        showList(if(nowNavigation == 0) commonPath else myPath)
    }

    private fun showList(path: String) {
        Thread(Runnable {
            try {
                Log.v("path:", path)
                itemList = utils.getItemsByPath(path)
                val nameList = itemList.map { it.itemName }
                runOnUiThread { findViewById<ListView>(R.id.itemList).adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nameList) }
            } catch (e: ConnectException) {
                runOnUiThread{ UiUtils.showNetworkError(this) }
                Log.e("showListError", e.message)
            }
        }).start()
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            var temp = getNowPath()
            temp = utils.getSuperPath(temp)
            return if (temp== "/") false
            else {
                showList(temp)
                setNowPath(temp)
                true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

}
