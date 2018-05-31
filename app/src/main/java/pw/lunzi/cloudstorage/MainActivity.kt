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
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.content.Intent
import android.app.Activity
import android.net.Uri


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    private var commonPath = "root/"
    private var myPath = ""
    private val utils = ApiUtils.get()
    private var itemList = listOf<FileItem>()
    private var nowNavigation = 0 // 0 1 2 对应三个navigation

    private fun getNowPath() = if (nowNavigation == 0) commonPath else myPath
    private fun setNowPath(path: String) {
        if (nowNavigation == 0) commonPath = path else myPath = path
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_common -> {
                nowNavigation = 0
                showList(commonPath)
                UiUtils.canMkdirAndUpload(false, this)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myspace -> {
                nowNavigation = 1
                if (!ApiUtils.isLogin) {
                    UiUtils.showNeedLoginAlert(this)
                } else {
                    UiUtils.canMkdirAndUpload(true, this)
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

    fun uploadOnClick(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var path = ""
        if (resultCode === Activity.RESULT_OK) {
            val uri = data!!.data
            Log.v("uri", uri.toString())
            if ("file".equals(uri.scheme, ignoreCase = true)) {//使用第三方应用打开
                path = uri.path
                Toast.makeText(this, path + "11111", Toast.LENGTH_SHORT).show()
                return
            }
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    path = UriToPath.getPath(this, uri)!!
                    Thread(Runnable {
                        utils.upload(getNowPath(), path, this)
                    }).start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        //询问文件读写权限
        if (Build.VERSION.SDK_INT >= 23) {
            val REQUEST_CODE_CONTACT = 101
            val permissions = arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            //验证是否许可权限
            for (str in permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT)
                }
            }
        }
        //更新按钮状态
        UiUtils.canMkdirAndUpload(false, this)

        //列表子项点击事件
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
                Toast.makeText(this, "你单击的是第" + (position + 1) + "条数据,非文件夹", Toast.LENGTH_SHORT).show()
            }
        })

        showList(if (nowNavigation == 0) commonPath else myPath)
    }

    private fun showList(path: String) {
        Thread(Runnable {
            try {
                Log.v("path:", path)
                itemList = utils.getItemsByPath(path)
                val nameList = itemList.map { it.itemName }
                runOnUiThread { findViewById<ListView>(R.id.itemList).adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nameList) }
            } catch (e: ConnectException) {
                runOnUiThread { UiUtils.showNetworkError(this) }
                Log.e("showListError", e.message)
            }
        }).start()
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            var temp = getNowPath()
            temp = utils.getSuperPath(temp)
            return if (temp == "/") false
            else {
                showList(temp)
                setNowPath(temp)
                true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

}
