package pw.lunzi.cloudstorage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

class UserListActivity : AppCompatActivity() {

    private var page = 1
    private var userList: List<ApiUtils.UserListItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        showUserList(this)

        findViewById<ListView>(R.id.listView_userList).setOnItemClickListener { _, _, position, _ ->
            val intent = Intent()
            intent.putExtra("username", userList!![position].username)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    fun preOnClick(view: View) {
        if (page <= 1) {
            Toast.makeText(this, "已经是第一页啦", Toast.LENGTH_SHORT).show()
        } else {
            page--
            showUserList(this)
        }
    }

    fun nextOnClick(view: View) {
        if (userList!!.isEmpty()) {
            Toast.makeText(this, "没有更多内容啦", Toast.LENGTH_SHORT).show()
        } else {
            page++
            showUserList(this)
        }
    }

    fun showUserList(context: Context) {
        Thread(Runnable {
            userList = ApiUtils.get().getUserList(page)
            runOnUiThread{
                if (userList!!.isEmpty()) {
                    Toast.makeText(context, "没有更多内容啦", Toast.LENGTH_SHORT).show()
                }
                findViewById<ListView>(R.id.listView_userList).adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,userList!!.map { it.username })
            }
        }).start()
    }
}