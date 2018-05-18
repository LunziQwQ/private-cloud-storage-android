package pw.lunzi.cloudstorage

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class FileItem(
        @JsonProperty("itemName") val itemName: String,
        @JsonProperty("path") val path: String,
        @JsonProperty("size") val size: Int,
        @JsonProperty("lastModified") val lastModified: Date,
        @JsonProperty("public") val isPublic: Boolean,
        @JsonProperty("dictionary") val isDictionary: Boolean){
}