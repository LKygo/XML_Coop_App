package com.kygoinc.xmlcoopapp.ApiCall

import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class OkHttpHelper {

    private val client = OkHttpClient()

    fun sendRequestWithCredentials(
        url: String,
        username: String,
        password: String,
        method: String = "POST", // GET, POST, PUT, DELETE, etc.
        body: RequestBody? = null
    ): Response? {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", Credentials.basic(username, password))
            .method(method, body)
            .build()

        return try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}