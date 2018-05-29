package pw.lunzi.cloudstorage

import android.os.Environment
import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class ApiUtils private constructor() {
    //单例模式
    companion object {
        var isLogin = false
        var session = ""
        var userInfo: UserInfo? = null

        const val DOMAIN_ADDRESS = "http:/192.168.0.114:8080"
        const val apiRootUrl = "$DOMAIN_ADDRESS/api"
        const val loginUrl = "$apiRootUrl/session"
        const val getUserUrl = "$apiRootUrl/user/"
        const val itemUrl = "$apiRootUrl/item/"
        const val downloadUrl = "$apiRootUrl/file/"
        fun get(): ApiUtils {
            return Inner.instance
        }
    }

    private object Inner {
        val instance = ApiUtils()
    }

    val mapper = ObjectMapper()

    private fun getFileItemListByJson(jsonStr: String): List<FileItem> {
        val javaType = mapper.typeFactory.constructCollectionType(List::class.java, FileItem::class.java)
        return mapper.readValue<ArrayList<FileItem>>(jsonStr, javaType)
    }

    private fun inputStreamToString(inputStream: InputStream): String {
        val br = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        while (true) {
            sb.append(br.readLine() ?: break).append("\n")
        }
        return sb.toString()
    }


    fun getItemsByPath(path: String): List<FileItem> {
        val path = getItemsURL(path)
        val connection = URL(path).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        if (isLogin) {
            connection.setRequestProperty("Cookie", session.split(";")[0])
        }
        return getFileItemListByJson(inputStreamToString(connection.inputStream))
    }

    fun login(username: String, password: String): Boolean {
        val connection = URL(loginUrl).openConnection() as HttpURLConnection
        val data = "username=$username&password=$password"
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.doOutput = true
        val outputStream = connection.outputStream
        outputStream.write(data.toByteArray())
        val responseCode = connection.responseCode
        if (responseCode == 200) {
            session = connection.getHeaderField("Set-Cookie")
            isLogin = true
            userInfo = getUser(username)
        }
        return responseCode == 200
    }

    data class UserInfo(
            @JsonProperty("isExist") val isExist: Boolean,
            @JsonProperty("username") val username: String,
            @JsonProperty("space") val space: Int,
            @JsonProperty("index") val index: String)


    fun getUser(username: String): UserInfo? {
        val url = "$getUserUrl$username"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        return if (connection.responseCode == 404) null else {
            val br = BufferedReader(InputStreamReader(connection.inputStream))
            val sb = StringBuilder()
            while (true) {
                sb.append(br.readLine() ?: break).append("\n")
            }
            mapper.readValue<UserInfo>(sb.toString(), UserInfo::class.java)
        }
    }

    fun getItemsURL(path: String): String = "$apiRootUrl/items/$path"

    fun getSuperPath(path: String): String {
        val temp = path.substring(0, path.lastIndex - 1)
        return temp.substring(0, temp.lastIndexOf("/") + 1)
    }

    fun mkdir(name: String, path: String): Boolean {
        val url = "$itemUrl$path$name"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Cookie", session.split(";")[0])
        connection.connect()
        val responseCode = connection.responseCode
        Log.i("code:", responseCode.toString())
        return responseCode == 200
    }

    fun download(path: String, name: String) {
        val url = "$downloadUrl$path$name"
        Log.i("download URL : ", url)
        val savePath = Environment.getDownloadCacheDirectory().absolutePath
        val connection = URL(url).openConnection() as HttpURLConnection
        File(savePath).mkdir()
        val saveFile = File("$savePath/$name")
        Log.i("savePath : ", "$savePath/$name")
        saveFile.mkdirs()
        saveFile.createNewFile()
        val inputStream = connection.inputStream
        val outputStream = FileOutputStream(saveFile)
        val buffer = ByteArray(4 * 1024)
        while(inputStream.read(buffer)!=-1){
            outputStream.write(buffer)
        }
        outputStream.flush()
        outputStream.close()
    }
}