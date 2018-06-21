package pw.lunzi.cloudstorage

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.net.ConnectException
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.content.Intent
import android.app.Activity
import android.graphics.Color
import android.support.design.internal.BottomNavigationItemView
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import java.nio.file.attribute.PosixFileAttributeView


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    //ViewPager 实例，实现底部导航栏切换页面
    private var pager: ViewPager? = null

    //common页面的当前路径缓存
    private var commonPath = "root/"

    //mySpace页面的当前路径
    private var myPath = ""

    //Api工具类实例化
    private val utils = ApiUtils.get()

    //根据pager编号处理不同页面的Path
    fun getNowPath() = if (pager!!.currentItem == 0) commonPath else myPath

    fun setNowPath(path: String) {
        if (pager!!.currentItem == 0) {
            commonPath = path
            findViewById<TextView>(R.id.textView_commonPath).text = path
        } else {
            myPath = path
            findViewById<TextView>(R.id.textView_myPath).text = path
        }
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_common -> {
                pager!!.currentItem = 0
                showCommonList(commonPath)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myspace -> {
                if (!ApiUtils.isLogin) {
                    UiUtils.showNeedLoginAlert(this, this)
                    findViewById<BottomNavigationItemView>(R.id.navigation_common).performClick()
                } else {
                    pager!!.currentItem = 1
                    if (myPath == "" || myPath.split("/")[0] != ApiUtils.userInfo!!.username) setNowPath(ApiUtils.userInfo!!.username + "/")
                    showMyList(myPath)
                }
                return@OnNavigationItemSelectedListener ApiUtils.isLogin
            }
            R.id.navigation_me -> {
                if (!ApiUtils.isLogin) {
                    UiUtils.showNeedLoginAlert(this, this)
                    findViewById<BottomNavigationItemView>(R.id.navigation_common).performClick()
                } else {
                    pager!!.currentItem = 2
                    Thread(Runnable {
                        utils.updateMyInfo()
                        runOnUiThread{
                            val seekBar = findViewById<ProgressBar>(R.id.progress_usage)
                            findViewById<TextView>(R.id.lable_username).text = ApiUtils.userInfo!!.username
                            findViewById<TextView>(R.id.lable_userAuth).text = if (ApiUtils.userInfo!!.username == "root") "Admin" else "Member"
                            findViewById<TextView>(R.id.lable_usage).text = "${Utils.getSizeString(ApiUtils.userInfo!!.usage)}/${Utils.getSizeString(ApiUtils.userInfo!!.space)}"
                            seekBar.max = ApiUtils.userInfo!!.space
                            seekBar.progress = ApiUtils.userInfo!!.usage
                            seekBar.isClickable = false
                        }
                    }).start()
                }
                return@OnNavigationItemSelectedListener ApiUtils.isLogin
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
        when (requestCode) {
            1 -> {
                val path: String
                if (resultCode === Activity.RESULT_OK) {
                    val uri = data!!.data
                    Log.v("uri", uri.toString())
                    if ("file".equals(uri.scheme, ignoreCase = true)) {//使用第三方应用打开
                        path = uri.path
                        Thread(Runnable {
                            utils.upload(getNowPath(), path, this)
                            runOnUiThread {
                                showMyList(getNowPath())
                            }
                        }).start()
                    } else {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                            path = UriToPath.getPath(this, uri)!!
                            Thread(Runnable {
                                utils.upload(getNowPath(), path, this)
                                runOnUiThread {
                                    showMyList(getNowPath())
                                }
                            }).start()
                        }
                    }
                }
            }
            2 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val username = data!!.extras.getString("username")
                    setNowPath("$username/")
                    showCommonList("$username/")
                }
            }
        }
    }

    fun mkdirOnClick(view: View) {
        if (ApiUtils.isLogin) {
            UiUtils.showEditAlert(this, getNowPath(), this)
        } else {
            UiUtils.showNeedLoginAlert(this, this)
        }
    }

    fun changePwdOnClick(view: View) {
        startActivity(Intent("ChangePassword"))
    }

    fun logoutOnClick(view: View) {
        Thread(Runnable {
            if (utils.logout()) {
                runOnUiThread {
                    UiUtils.showError(this, "Logout success")
                    findViewById<BottomNavigationItemView>(R.id.navigation_common).performClick()
                }
            } else {
                runOnUiThread {
                    UiUtils.showError(this, "Logout failed. Please try again.")
                }
            }
        }).start()
    }

    fun userListOnClick(view: View) {
        val intent = Intent("UserList")
        startActivityForResult(intent, 2)
    }

    fun mkdir(name: String, path: String) {
        Toast.makeText(this, "${ApiUtils.itemUrl}$path$name", Toast.LENGTH_SHORT).show()
        Thread(Runnable {
            if (utils.mkdir(name, path)) {
                showMyList(getNowPath())
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


        //初始化ViewPager
        pager = findViewById(R.id.view_pager)
        pager!!.adapter = MyPagerAdapter(supportFragmentManager)
        pager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> findViewById<BottomNavigationItemView>(R.id.navigation_common).performClick()
                    1 -> findViewById<BottomNavigationItemView>(R.id.navigation_myspace).performClick()
                    2 -> findViewById<BottomNavigationItemView>(R.id.navigation_me).performClick()
                }
            }
        })

        //询问文件读写权限
        if (Build.VERSION.SDK_INT >= 23) {
            val REQUEST_CODE_CONTACT = 101
            val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            //验证是否许可权限
            for (str in permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT)
                }
            }
        }
        //更新按钮状态
//        UiUtils.canMkdirAndUpload(false, this)
        showCommonList(commonPath)
        //初始化导航栏
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun showCommonList(path: String) {
        Thread(Runnable {
            try {
                Log.v("path:", path)
                val tempList = mutableListOf<Map<String, Any>>()
                FileItem.commonFileItemList = utils.getItemsByPath(path, false)
                FileItem.commonFileItemList.forEach {
                    tempList.add(mapOf(
                            "item_image" to if (it.isDictionary) R.drawable.folder_32 else R.drawable.documents_32,
                            "item_title" to it.itemName,
                            "item_size" to Utils.getSizeString(it.size)
                    ))
                }
                FileItem.commonItemList = tempList
                runOnUiThread {
                    findViewById<ListView>(R.id.commonItemList).adapter = object : BaseAdapter() {
                        @SuppressLint("ViewHolder", "InflateParams")
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.list_items, null)
                            val imgView = view.findViewById<ImageView>(R.id.item_image)
                            val titleView = view.findViewById<TextView>(R.id.item_title)
                            val sizeView = view.findViewById<TextView>(R.id.item_size)

                            val transferBtn = view.findViewById<Button>(R.id.btn_transfer)
                            val downloadBtn = view.findViewById<Button>(R.id.btn_download)

                            titleView.setOnClickListener {
                                if (FileItem.commonFileItemList[position].isDictionary) {
                                    var temp = getNowPath()
                                    temp += "${FileItem.commonFileItemList[position].itemName}/"
                                    showCommonList(temp)
                                    setNowPath(temp)
                                }
                            }

                            downloadBtn.setOnClickListener {
                                if (!FileItem.commonFileItemList[position].isDictionary) {
                                    Thread(Runnable {
                                        ApiUtils.get().download(getNowPath(), FileItem.commonFileItemList[position].itemName)
                                    }).start()
                                    Toast.makeText(view.context, "成功开始下载 ${FileItem.commonFileItemList[position].itemName} 至/cloudStorage", Toast.LENGTH_SHORT).show()

                                } else {
                                    Toast.makeText(view.context, "暂时不支持下载文件夹", Toast.LENGTH_SHORT).show()
                                }
                            }

                            transferBtn.setOnClickListener {
                                if (ApiUtils.isLogin) {
                                    val ownerName = FileItem.commonFileItemList[position].path.split("/")[1]
                                    if (FileItem.commonFileItemList[position].path.split("/")[1] == ApiUtils.userInfo!!.username) {
                                        Toast.makeText(view.context, "您无法转存属于自己的文件", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Thread(Runnable {
                                            UiUtils.showTransferAlert(this@MainActivity, this@MainActivity, FileItem.commonFileItemList[position].path, FileItem.commonFileItemList[position].itemName)
                                        }).start()
                                    }
                                } else {
                                    Toast.makeText(view.context, "您还未登录，无法转存", Toast.LENGTH_SHORT).show()
                                }
                            }

                            imgView.setImageResource(FileItem.commonItemList[position]["item_image"] as Int)
                            titleView.text = FileItem.commonItemList[position]["item_title"] as String
                            sizeView.text = FileItem.commonItemList[position]["item_size"] as String


                            return view
                        }

                        override fun getItem(position: Int): Any {
                            return position
                        }

                        override fun getItemId(position: Int): Long {
                            return position.toLong()
                        }

                        override fun getCount(): Int {
                            return FileItem.commonItemList.size
                        }
                    }
                }
            } catch (e: ConnectException) {
                runOnUiThread { UiUtils.showNetworkError(this) }
                Log.e("showListError", e.message)
            }
        }).start()
    }

    fun transferItem(targetPath: String, itemPath: String, itemName: String) {
        Thread(Runnable {
            if (utils.getItemsByPath("$targetPath", true).filter { it.itemName == itemName }.count() > 0) {
                runOnUiThread {
                    UiUtils.showError(this, "Failed. Target path have same name item.")
                }
            } else {
                if(utils.transferItem(targetPath,itemPath,itemName)){
                    runOnUiThread{
                        UiUtils.showError(this, "Transfer success")
                    }
                }
            }
        }).start()
    }


    private fun showMyList(path: String) {
        Thread(Runnable {
            try {
                Log.v("path:", path)
                val tempList = mutableListOf<Map<String, Any>>()
                FileItem.myFileItemList = utils.getItemsByPath(path, true)
                FileItem.myFileItemList.forEach {
                    tempList.add(mapOf(
                            "item_image" to if (it.isDictionary) R.drawable.folder_32 else R.drawable.documents_32,
                            "item_title" to it.itemName,
                            "item_size" to Utils.getSizeString(it.size)
                    ))
                }
                FileItem.myItemList = tempList

                runOnUiThread {
                    findViewById<ListView>(R.id.myItemList).adapter = object : BaseAdapter() {
                        @SuppressLint("ViewHolder", "InflateParams")
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.list_items_editable, null)
                            val imgView = view.findViewById<ImageView>(R.id.item_image)
                            val titleView = view.findViewById<TextView>(R.id.item_title)
                            val sizeView = view.findViewById<TextView>(R.id.item_size)

                            val downloadBtn = view.findViewById<Button>(R.id.btn_download)
                            val deleteBtn = view.findViewById<Button>(R.id.btn_delete)
                            val renameBtn = view.findViewById<Button>(R.id.btn_rename)
                            val changeAccessBtn = view.findViewById<Button>(R.id.btn_change_access)

                            titleView.setOnClickListener {
                                if (FileItem.myFileItemList[position].isDictionary) {
                                    var temp = getNowPath()
                                    temp += "${FileItem.myFileItemList[position].itemName}/"
                                    showMyList(temp)
                                    setNowPath(temp)
                                }
                            }

                            view.setOnClickListener {
                                if (FileItem.myFileItemList[position].isDictionary) {
                                    var temp = getNowPath()
                                    temp += "${FileItem.myFileItemList[position].itemName}/"
                                    showMyList(temp)
                                    setNowPath(temp)
                                }
                            }

                            downloadBtn.setOnClickListener {
                                if (!FileItem.myFileItemList[position].isDictionary) {
                                    Thread(Runnable {
                                        utils.download(getNowPath(), FileItem.myFileItemList[position].itemName)
                                    }).start()
                                    Toast.makeText(view.context, "成功开始下载 ${FileItem.myFileItemList[position].itemName} 至/cloudStorage", Toast.LENGTH_SHORT).show()

                                } else {
                                    Toast.makeText(view.context, "暂时不支持下载文件夹", Toast.LENGTH_SHORT).show()
                                }
                            }

                            deleteBtn.setOnClickListener {
                                Thread(Runnable {
                                    utils.deleteItem(getNowPath(), FileItem.myFileItemList[position].itemName)
                                    showMyList(getNowPath())
                                }).start()
                            }

                            renameBtn.setOnClickListener {
                                UiUtils.showRenameAlert(this@MainActivity, getNowPath(), FileItem.myFileItemList[position].itemName, this@MainActivity)
                            }

                            changeAccessBtn.setOnClickListener {
                                if (FileItem.myFileItemList[position].isDictionary) {
                                    UiUtils.showChangeAccessAlert(this@MainActivity, this@MainActivity, getNowPath(), FileItem.myFileItemList[position].itemName, !FileItem.myFileItemList[position].isPublic)
                                } else {
                                    changeItemAccess(getNowPath(), FileItem.myFileItemList[position].itemName, false, !FileItem.myFileItemList[position].isPublic)
                                }
                            }

                            if (FileItem.myFileItemList[position].isPublic) {
                                titleView.setTextColor(Color.rgb(63, 119, 4))
                                titleView.paint.isFakeBoldText = true
                            }

                            imgView.setImageResource(FileItem.myItemList[position]["item_image"] as Int)
                            titleView.text = FileItem.myItemList[position]["item_title"] as String
                            sizeView.text = FileItem.myItemList[position]["item_size"] as String
                            return view
                        }

                        override fun getItem(position: Int): Any {
                            return position
                        }

                        override fun getItemId(position: Int): Long {
                            return position.toLong()
                        }

                        override fun getCount(): Int {
                            return FileItem.myItemList.size
                        }
                    }
                }
            } catch (e: ConnectException) {
                runOnUiThread { UiUtils.showNetworkError(this) }
                Log.e("showListError", e.message)
            }
        }).start()
    }

    fun renameItem(newName: String, path: String, oldName: String) {
        Thread(Runnable {
            if (utils.renameItem(newName, path, oldName)) {
                runOnUiThread {
                    showMyList(getNowPath())
                }
            }
        }).start()
    }

    fun changeItemAccess(path: String, name: String, allRecursion: Boolean, isPublic: Boolean) {
        Thread(Runnable {
            if (utils.changeItemAccess(path, name, allRecursion, isPublic)) {
                runOnUiThread {
                    showMyList(getNowPath())
                }
            }
        }).start()
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            var temp = getNowPath()
            temp = utils.getSuperPath(temp)
            return if (temp == "") super.onKeyUp(keyCode, event)
            else {
                when (pager!!.currentItem) {
                    0 -> showCommonList(temp)
                    1 -> showMyList(temp)
                    2 -> return false
                }
                setNowPath(temp)
                true
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}
