package com.typeboot.dataformat.types

import java.io.File
import java.util.regex.Pattern

data class FileScript(val serial: Int, val name: String, val filePath: String)

data class SpecConfig(val provider: String,
                      val mode: String,
                      val generate: String,
                      val source: String) {
    fun getRenderers(): List<String> {
        return if (generate == "all") {
            "mutations,audit"
        } else {
            generate
        }.split(",")
    }

    private val reg = Pattern.compile("([0-9]+).*\\.yaml")
    fun getSources(): List<FileScript> {

        val dataFiles = File(source).walk().filter { f ->
            f.isFile && reg.matcher(f.name).matches()
        }.toList()
        val fileCache = mutableMapOf<String, File>()
        val scriptCache = mutableListOf<Int>()
        return dataFiles.map { f ->
            if (fileCache.containsKey(f.name)) {
                throw RuntimeException("duplicate script name ${f.name}")
            } else {
                fileCache[f.name] = f
            }
            val matcher = reg.matcher(f.name)
            matcher.matches()
            val scriptNumber = matcher.toMatchResult().group(1).toInt()
            if (scriptCache.contains(scriptNumber)) {
                throw RuntimeException("duplicate script number $scriptNumber in ${f.name} as it already exists.")
            } else {
                scriptCache.add(scriptNumber)
            }
            FileScript(scriptNumber, f.name, f.absolutePath)
        }.sortedBy { fs -> fs.serial }
    }
}