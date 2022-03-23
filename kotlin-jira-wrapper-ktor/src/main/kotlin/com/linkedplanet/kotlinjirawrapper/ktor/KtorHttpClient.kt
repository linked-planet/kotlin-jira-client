package com.linkedplanet.kotlinjirawrapper.ktor

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.gson.JsonParser
import com.linkedplanet.kotlinjirawrapper.api.http.BaseHttpClient
import com.linkedplanet.kotlinjirawrapper.api.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.http.JiraConfig
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.auth.basic.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

fun httpClient(username: String, password: String) =
    HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(BasicAuth) {
            this.username = username
            this.password = password
        }
    }

class KtorHttpClient(
    private val baseUrl: String,
    username: String,
    password: String
) : BaseHttpClient() {

    private var httpClient: HttpClient = httpClient(username, password)

    private fun prepareRequest(requestBuilder: HttpRequestBuilder, path: String, params: Map<String, String>, bodyIn: String?, contentType: String?) {
        val parsedContentType = contentType
            ?.let { ContentType.parse(it) }
            ?:ContentType.Application.Json
        val parameterString = params
            .takeIf { it.isNotEmpty() }
            ?.let { "?${encodeParams(params)}" }
            ?: ""
        requestBuilder.url("$baseUrl/$path$parameterString")
        requestBuilder.contentType(parsedContentType)
        if(bodyIn != null){
            requestBuilder.body = JsonParser().parse(bodyIn)
        }
    }

    override suspend fun executeRestCall(
        method: String,
        path: String,
        params: Map<String, String>,
        bodyIn: String?,
        contentType: String?,
        headers: Map<String, String>
    ): Either<DomainError, String> {
        return try {
            when (method) {
                "GET" -> {
                    httpClient.get<String> {
                        prepareRequest(this, path, params, bodyIn, contentType)
                    }.right()
                }
                "POST" -> {
                    httpClient.post<String> {
                        prepareRequest(this, path, params, bodyIn, contentType)
                    }.right()
                }
                "PUT" -> {
                    httpClient.put<String> {
                        prepareRequest(this, path, params, bodyIn, contentType)
                    }.right()
                }
                "DELETE" -> {
                    httpClient.delete<String> {
                        prepareRequest(this, path, params, bodyIn, contentType)
                    }.right()
                }
                else -> {
                    DomainError("HTTP-ERROR", "Method '$method' not available").left()
                }
            }
        } catch (ex: Exception) {
            val message = "$method: $path\n${params.map { "${it.key}::${it.value}" }.joinToString { "\n\t" }}\n\n${ex.message}"
            DomainError("Schnittstellen-Fehler", message).left()
        }
    }

    override suspend fun executeDownload(
        method: String,
        url: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?
    ): Either<DomainError, ByteArray> {
        return httpClient.get<ByteArray> {
            url(url)
        }.right()
    }

    override suspend fun executeUpload(
        method: String,
        url: String,
        params: Map<String, String>,
        mimeType: String,
        filename: String,
        byteArray: ByteArray
    ): Either<DomainError, ByteArray> =
        httpClient.post<ByteArray> {
            url("${JiraConfig.baseUrl}$url")
            header("Connection", "keep-alive")
            header("Cache-Control", "no-cache")
            body = MultiPartFormDataContent(
                formData {
                    this.append(
                        "file",
                        byteArray,
                        Headers.build {
                            append(HttpHeaders.ContentType, mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=$filename")
                        })
                    //this.append(FormPart("encodedComment", comment))
                }
            )
        }.right()


}