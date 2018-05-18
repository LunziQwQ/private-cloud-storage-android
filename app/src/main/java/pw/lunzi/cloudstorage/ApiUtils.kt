package pw.lunzi.cloudstorage

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiUtils private constructor(){
    //单例模式
    companion object {
        val rootPath = "http://127.0.0.1:8080/api/items/root"

        fun get():ApiUtils{
            return Inner.instance
        }
    }
    private object Inner{
        val instance = ApiUtils()
    }


    val mapper = ObjectMapper()

    private fun getFileItemListByJson(jsonStr: String): List<FileItem> {
        val javaType = mapper.typeFactory.constructCollectionType(List::class.java, FileItem::class.java)
        return mapper.readValue<ArrayList<FileItem>>(jsonStr, javaType)
    }

    private fun sendGetRequest(path: String): String {
        val connection = URL(path).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val br = BufferedReader(InputStreamReader(connection.inputStream))
        var sb = StringBuilder()
        while (true) {
            sb.append(br.readLine() ?: break).append("\n")
        }
        return sb.toString()
    }


    fun getItemsWithoutLogin(path: String): List<FileItem> {
        return getFileItemListByJson(sendGetRequest(path))
    }

}