package ffc.airsync.utils

import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import java.io.FileReader
import java.io.FileWriter

inline fun saveResource(strData: String, fileName: String) {
    val fileWriter = FileWriter(fileName)
    fileWriter.write(strData)
    fileWriter.close()
}

inline fun <reified T> loadResource(fileName: String): List<T> {
    val fileJson = FileReader(fileName).readText().parseTo<List<Any>>()

    return fileJson.map {
        val ii = it.toJson().parseTo<T>()
        ii
    }
}

inline fun <reified T> loadResourceHashMap(fileName: String): HashMap<String, T> {
    return FileReader(fileName).readText().parseTo()
}

inline fun <reified T> List<T>.save(filename: String = "${getClassNameInList(this)}.json") {
    saveResource(this.toJson(), filename)
}

inline fun <reified T> HashMap<String, T>.save(filename: String) {
    saveResource(this.toJson(), filename)
}

inline fun <reified T> List<T>.load(filename: String = "${getClassNameInList(this)}.json"): List<T> {
    return try {
        loadResource(filename)
    } catch (ex: java.io.FileNotFoundException) {
        arrayListOf()
    }
}

inline fun <reified T> HashMap<String, T>.load(filename: String): HashMap<String, T> {
    return try {
        loadResourceHashMap(filename)
    } catch (ex: java.io.FileNotFoundException) {
        hashMapOf()
    }
}

inline fun <reified T> List<T>.cleanFile() {
    saveResource("[]", "${getClassNameInList(this)}.json")
}

inline fun <reified T> getClassNameInList(list: List<T>): String {
    return T::class.java.simpleName
}
