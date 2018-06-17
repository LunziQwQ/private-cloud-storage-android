package pw.lunzi.cloudstorage

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File
import java.util.*

class FileItem(
        @JsonProperty("itemName") val itemName: String,
        @JsonProperty("path") val path: String,
        @JsonProperty("size") val size: Int,
        @JsonProperty("lastModified") val lastModified: Date,
        @JsonProperty("public") val isPublic: Boolean,
        @JsonProperty("dictionary") val isDictionary: Boolean){
    companion object {
        var commonFileItemList = listOf<FileItem>()
        var myFileItemList = listOf<FileItem>()

        var commonItemList = listOf<Map<String, Any>>()
        var myItemList = listOf<Map<String, Any>>()
    }
}