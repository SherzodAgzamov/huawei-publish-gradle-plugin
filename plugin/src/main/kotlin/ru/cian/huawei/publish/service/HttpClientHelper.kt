package ru.cian.huawei.publish.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import ru.cian.huawei.publish.utils.Logger

internal class HttpClientHelper {

    private val gson by lazy { Gson() }

    inline fun <reified T> get(url: String, headers: Map<String, String>? = null): T =
        execute(Request.Builder().get(), url, headers)

    inline fun <reified T> post(url: String, body: RequestBody, headers: Map<String, String>? = null): T =
        execute(Request.Builder().post(body), url, headers)

    inline fun <reified T> put(url: String, body: RequestBody, headers: Map<String, String>? = null): T =
        execute(Request.Builder().put(body), url, headers)

    @Suppress("ThrowsCount")
    inline fun <reified T> execute(requestBuilder: Request.Builder, url: String, headers: Map<String, String>?): T {
        try {
            val client = OkHttpClient()
            val request = requestBuilder
                .url(url)
                .apply { headers?.forEach { header(it.key, it.value) } }
                .build()

            return client.newCall(request).execute().use { httpResponse ->
                val statusCode = httpResponse.code

                if (!httpResponse.isSuccessful) {
                    throw IllegalStateException("Request failed. statusCode=$statusCode, httpResponse=$httpResponse")
                }

                gson.fromJson(httpResponse.body?.charStream(), T::class.java)
                    ?: throw IllegalStateException("http request result must not be null")
            }
        } catch (e: JsonSyntaxException) {
            Logger.e(e)
        }
        throw IllegalStateException("Request is failed. Something went wrong, please check request!")
    }

    companion object {
        val MEDIA_TYPE_JSON = "application/json;charset=utf-8".toMediaType()
        val MEDIA_TYPE_AAB = "application/octet-stream".toMediaType()
    }
}
