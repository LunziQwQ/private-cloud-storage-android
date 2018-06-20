package pw.lunzi.cloudstorage

import android.content.Context
import android.os.Environment
import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*




class ApiUtils private constructor() {
    //单例模式
    companion object {
        var isLogin = false
        var session = ""
        var userInfo: UserInfo? = null

        const val DOMAIN_ADDRESS = "http://192.168.0.114:8080"
        const val apiRootUrl = "$DOMAIN_ADDRESS/api"
        const val sessionUrl = "$apiRootUrl/session"
        const val userUrl = "$apiRootUrl/user/"
        const val userListUrl = "$apiRootUrl/users/"
        const val itemUrl = "$apiRootUrl/item/"
        const val fileLoadUrl = "$apiRootUrl/file/"
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

    private fun getUserItemListByJson(jsonStr: String):List<UserListItem>{
        val javaType = mapper.typeFactory.constructCollectionType(List::class.java, UserListItem::class.java)
        return mapper.readValue<ArrayList<UserListItem>>(jsonStr, javaType)
    }

    private fun inputStreamToString(inputStream: InputStream): String {
        val br = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        while (true) {
            sb.append(br.readLine() ?: break).append("\n")
        }
        return sb.toString()
    }

    fun getUserList(page: Int): List<UserListItem> {
        val url = "$userListUrl$page"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        return getUserItemListByJson(inputStreamToString(connection.inputStream))
    }


    fun getItemsByPath(path: String, withLogin: Boolean): List<FileItem> {
        val path = getItemsURL(path)
        val connection = URL(path).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        if (withLogin && isLogin) {
            connection.setRequestProperty("Cookie", session.split(";")[0])
        }
        return getFileItemListByJson(inputStreamToString(connection.inputStream))
    }

    fun register(username: String, password: String): Boolean {
        val connection = URL("$userUrl$username").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        val outputStream = connection.outputStream
        outputStream.write("{\"password\":\"$password\"}".toByteArray())
        val responseCode = connection.responseCode
        outputStream.close()
        return responseCode == 200
    }

    fun login(username: String, password: String): Boolean {
        val connection = URL(sessionUrl).openConnection() as HttpURLConnection
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
        outputStream.close()
        return responseCode == 200
    }

    fun changeMyPassword(oldPassword: String, newPassword: String): Boolean {
        val url = "$userUrl${userInfo!!.username}/password"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Cookie", session.split(";")[0])
        connection.doOutput = true
        val outputStream = connection.outputStream
        outputStream.write("{\"oldPassword\":\"$oldPassword\",\"newPassword\":\"$newPassword\"}".toByteArray())
        outputStream.flush()
        outputStream.close()
        val responseCode = connection.responseCode
        return responseCode == 200
    }

    fun renameItem(newName: String, path: String, oldName: String): Boolean {
        val url = "$itemUrl$path$oldName/name"
        Log.v("RenameURL ---->",url)
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Cookie", session.split(";")[0])
        connection.doOutput = true
        val outputStream = connection.outputStream
        outputStream.write("{\"newName\":\"$newName\"}".toByteArray())
        outputStream.flush()
        outputStream.close()
        val responseCode = connection.responseCode
        Log.v("Rename ------>",responseCode.toString())
        return responseCode == 200
    }

    fun logout(): Boolean {
        val connection = URL(sessionUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Cookie", session.split(";")[0])
        val responseCode = connection.responseCode
        if (responseCode == 200) {
            isLogin = false
            session = ""
            userInfo = null
        }
        return responseCode == 200
    }


    fun getUser(username: String): UserInfo? {
        val url = "$userUrl$username"
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

    fun deleteItem(path: String, name: String): Boolean {
        val url = "$itemUrl$path$name"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Cookie", session.split(";")[0])
        connection.requestMethod = "DELETE"
        val responseCode = connection.responseCode
        Log.v("code:--------->", responseCode.toString())
        return responseCode == 200
    }

    fun changeItemAccess(path: String, name: String, allRecursion: Boolean, isPublic: Boolean): Boolean {
        val url = "$itemUrl$path$name/access"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Cookie", session.split(";")[0])
        connection.doOutput = true
        val outputStream = connection.outputStream
        outputStream.write("{\"isPublic\":$isPublic,\"allowRecursion\":$allRecursion}".toByteArray())
        outputStream.flush()
        outputStream.close()
        val responseCode = connection.responseCode
        Log.v("Change Acccess ------>", responseCode.toString())
        return responseCode == 200
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
        val responseCode = connection.responseCode
        Log.i("code:", responseCode.toString())
        return responseCode == 200
    }

    fun download(path: String, name: String) {
        val url = "$fileLoadUrl$path$name".replace(" ", "%20")
        Log.i("download URL : ", url)
        val savePath = Environment.getExternalStorageDirectory().absolutePath+"/cloudStorage"
        val connection = URL(url).openConnection() as HttpURLConnection
        if (isLogin) connection.setRequestProperty("Cookie", session.split(";")[0])
        File(savePath).mkdir()
        val saveFile = File("$savePath/$name")
        Log.i("savePath : ", "$savePath/$name")
        saveFile.createNewFile()
        val inputStream = connection.inputStream
        val outputStream = FileOutputStream(saveFile)
        val buffer = readInputStream(inputStream)
        outputStream.write(buffer)
        outputStream.flush()
        outputStream.close()
    }

    fun readInputStream(inputStream: InputStream): ByteArray {
        val buffer = ByteArray(1024)
        val bos = ByteArrayOutputStream()
        while (true) {
            val len = inputStream.read(buffer)
            if(len == -1) break
            bos.write(buffer, 0, len)
        }
        bos.close()
        return bos.toByteArray()
    }

    fun upload(vpath: String, path: String, context: Context) {
        val url = "$fileLoadUrl$vpath"
        Log.e("path",url)
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.doOutput = true
        connection.doInput = true
        connection.requestMethod = "POST"
        // create random boundary
        val PREFIX = "--"
        val LINE_END = "\r\n"
        val boundary = UUID.randomUUID().toString()
        val file = File(path)

        connection.setRequestProperty("Cookie", session.split(";")[0])
        connection.setRequestProperty("Connection", "keep-alive")
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
        connection.setRequestProperty("Cache-Control", "no-cache")
        connection.setRequestProperty("Charset","utf-8")
        val fis = FileInputStream(file)
        val os = connection.outputStream
        val sb = StringBuilder().append(PREFIX).append(boundary).append(LINE_END)
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""+file.name +"\""+LINE_END)
        sb.append("Content-Type: application/octet-stream$LINE_END")
        sb.append("Content-Transfer-Encoding:binary$LINE_END")
        sb.append(LINE_END)
        os.write(sb.toString().toByteArray())
        val bytes = ByteArray(1024)
        var len = 0
        while (true) {
            len = fis.read(bytes)
            if(len == -1) break
            os.write(bytes, 0, len)
        }
        fis.close()
        os.write(LINE_END.toByteArray())
        val endData = (PREFIX + boundary + PREFIX + LINE_END).toByteArray()
        os.write(endData)
        os.flush()
        os.close()

        val res = connection.responseCode
        Log.e("code", "response code:$res")
        if (res == 200) {
            val input = connection.inputStream
            val sb1 = StringBuffer()
            var ss: Int
            while (true) {
                ss = input.read()
                if(ss == -1) break
                sb1.append(ss.toChar())
            }
            val result = sb1.toString()
            Log.e("result", "result : $result")
        }
    }

    data class UserInfo(
            @JsonProperty("isExist") val isExist: Boolean,
            @JsonProperty("username") val username: String,
            @JsonProperty("space") val space: Int,
            @JsonProperty("usage") val usage: Int,
            @JsonProperty("index") val index: String)

    data class UserListItem(
            @JsonProperty("username") val username: String,
            @JsonProperty("userURL") val indexUrl: String,
            @JsonProperty("admin") val isAdmin: Boolean
    )
}