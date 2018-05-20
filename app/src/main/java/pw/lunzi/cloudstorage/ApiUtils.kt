package pw.lunzi.cloudstorage

import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class ApiUtils private constructor() {
    //单例模式
    companion object {
        var isLogin = false
        var session = ""
        var userInfo: UserInfo? = null

        const val DOMAIN_ADDRESS = "http:/10.0.2.2:8080"
        const val rootPathUrl = "$DOMAIN_ADDRESS/api/items/root/"
        const val loginUrl = "$DOMAIN_ADDRESS/api/session"
        const val getUserUrl = "$DOMAIN_ADDRESS/api/user/"
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


    fun getItems(path: String): List<FileItem> {
        val connection = URL(path).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        if (isLogin) {
            connection.setRequestProperty("Cookie",session.split(";")[0])
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
}