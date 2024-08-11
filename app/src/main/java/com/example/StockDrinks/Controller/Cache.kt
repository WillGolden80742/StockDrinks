package com.example.StockDrinks.Controller

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Cache {

    companion object {
        private const val CACHE_DIRECTORY = "Cache/"
    }

    private var file: File? = null

    private fun getHashMd5(value: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }

        val hash = BigInteger(1, md.digest(value.toByteArray()))
        return hash.toString(16)
    }

    private fun getHashSha1(value: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-1")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
        val hash = BigInteger(1, md.digest(value.toByteArray()))
        return hash.toString(16)
    }

    private fun fileHashed(fileName: String, hashType: String = "SHA-1"): String {
        return if (hashType == "SHA-1") {
            "${getHashSha1(fileName)}${fileName.length}.json"
        } else {
            "${getHashMd5(fileName)}.json"
        }
    }

    fun setCache(context: Context, fileName: String, text: String) {
        val hashedFileName = fileHashed(fileName, "SHA-1")
        file = File(context.filesDir, CACHE_DIRECTORY + hashedFileName)

        if (file!!.exists()) {
            try {
                val writer = FileOutputStream(file)
                writer.write(text.toByteArray())
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            writeToFile(context, hashedFileName, text.toByteArray())
        }
    }

    fun getCache(context: Context, fileName: String): String {
        var hashedFileName = fileHashed(fileName, "SHA-1")
        file = File(context.filesDir, CACHE_DIRECTORY + hashedFileName)

        if (!file!!.exists()) {
            hashedFileName = fileHashed(fileName, "MD5")
            file = File(context.filesDir, CACHE_DIRECTORY + hashedFileName)
        }

        return if (file!!.exists() && file!!.length() > 0) {
            file!!.readText()
        } else {
            "NOT_FOUND"
        }
    }

    fun hasCache(context: Context, fileName: String): Boolean {
        var hashedFileName = fileHashed(fileName, "SHA-1")
        file = File(context.filesDir, CACHE_DIRECTORY + hashedFileName)

        if (!file!!.exists()) {
            hashedFileName = fileHashed(fileName, "MD5")
            file = File(context.filesDir, CACHE_DIRECTORY + hashedFileName)
        }

        return file!!.exists() && file!!.length() > 0
    }

    private fun writeToFile(context: Context, fileName: String, content: ByteArray) {
        val path = File(context.filesDir, CACHE_DIRECTORY)
        val newDir = File(path.toString())
        try {
            if (!newDir.exists()) {
                newDir.mkdirs()
            }
            val writer = FileOutputStream(File(path, fileName))
            writer.write(content)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
