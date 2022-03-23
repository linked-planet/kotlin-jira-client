package com.linkedplanet.kotlinjirawrapper.atlas

import arrow.core.Either
import com.atlassian.applinks.api.ApplicationLink
import com.atlassian.applinks.api.ApplicationLinkResponseHandler
import com.atlassian.sal.api.net.Request
import com.atlassian.sal.api.net.RequestFilePart
import com.atlassian.sal.api.net.Response
import com.atlassian.sal.api.net.ResponseException
import com.linkedplanet.kotlinjirawrapper.api.http.BaseHttpClient
import com.linkedplanet.kotlinjirawrapper.api.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.error.ResponseError
import org.apache.http.HttpHeaders
import org.jetbrains.kotlin.library.impl.javaFile

class AtlasJiraHttpClient(private val appLink: ApplicationLink) : BaseHttpClient() {

    override suspend fun executeRestCall(
        method: String,
        path: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?,
        headers: Map<String, String>
    ): Either<DomainError, String> =
        try {
            //val pathWithParams = if(params != null) "$path?${URLEncoder.encode(params)}" else path
            val atlasMethod = Request.MethodType.valueOf(method)
            val parameters = encodeParams(params)
            val pathWithParams = if (params.isNotEmpty()) "$path?${parameters}" else path

            val requestFactory = appLink.createAuthenticatedRequestFactory()
            val requestWithoutBody = requestFactory.createRequest(atlasMethod, pathWithParams)
            val request = if (body == null) {
                requestWithoutBody
            } else {
                requestWithoutBody.setRequestBody(body).setHeader(HttpHeaders.CONTENT_TYPE, contentType)
            }
            request.execute(object : ApplicationLinkResponseHandler<Either<DomainError, String>> {
                override fun credentialsRequired(response: Response): Either<DomainError, String>? {
                    return null
                }

                override fun handle(response: Response): Either<DomainError, String> {
                    return when {
                        response.isSuccessful -> Either.Right(response.responseBodyAsString)
                        else -> {
                            val errorWithStatusCode = """Call to $path failed with 
                            status [${response.statusCode}]
                            statusText [${response.statusText}]
                            body [${response.responseBodyAsString}]"""
                            return Either.Left(ResponseError(errorWithStatusCode))
                        }
                    }
                }
            })
        } catch (e: ResponseException) {
            Either.Left(DomainError("Jira/Insight hat ein internes Problem festgestellt", ""))
        }

    override suspend fun executeDownload(
        method: String,
        path: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?
    ): Either<DomainError, ByteArray> =
        try {
            val atlasMethod = Request.MethodType.valueOf(method)
            val parameters = encodeParams(params)
            val pathWithParams = if (params.isNotEmpty()) "$path?${parameters}" else path

            val requestFactory = appLink.createAuthenticatedRequestFactory()
            val requestWithoutBody = requestFactory.createRequest(atlasMethod, pathWithParams)
            val request = if (body == null) {
                requestWithoutBody
            } else {
                requestWithoutBody.setRequestBody(body).setHeader(HttpHeaders.CONTENT_TYPE, contentType)
            }
            request.execute(object : ApplicationLinkResponseHandler<Either<DomainError, ByteArray>> {
                override fun credentialsRequired(response: Response): Either<DomainError, ByteArray>? {
                    return null
                }

                override fun handle(response: Response): Either<DomainError, ByteArray> {
                    return when {
                        response.isSuccessful -> Either.Right(response.responseBodyAsStream.readBytes())
                        else -> {
                            val errorWithStatusCode = """Call to $path failed with 
                            status [${response.statusCode}]
                            statusText [${response.statusText}]
                            body [${response.responseBodyAsString}]"""
                            return Either.Left(ResponseError(errorWithStatusCode))
                        }
                    }
                }
            })
        } catch (e: ResponseException) {
            Either.Left(DomainError("Jira/Insight hat ein internes Problem festgestellt", ""))
        }

    override suspend fun executeUpload(
        method: String,
        url: String,
        params: Map<String, String>,
        mimeType: String,
        filename: String,
        byteArray: ByteArray
    ): Either<DomainError, ByteArray> =
        try {
            val atlasMethod = Request.MethodType.valueOf(method)
            val parameters = encodeParams(params)
            val pathWithParams = if (params.isNotEmpty()) "$url?${parameters}" else url

            val requestFactory = appLink.createAuthenticatedRequestFactory()
            val requestWithoutBody = requestFactory.createRequest(atlasMethod, pathWithParams)

            val file: java.io.File = org.jetbrains.kotlin.konan.file.createTempFile(filename).javaFile()
            file.writeBytes(byteArray)
            val filePart = RequestFilePart(file, "file")

            val request = requestWithoutBody.setFiles(listOf(filePart))
            request.execute(object : ApplicationLinkResponseHandler<Either<DomainError, ByteArray>> {
                override fun credentialsRequired(response: Response): Either<DomainError, ByteArray>? {
                    return null
                }

                override fun handle(response: Response): Either<DomainError, ByteArray> {
                    return when {
                        response.isSuccessful -> Either.Right(byteArray)
                        else -> {
                            val errorWithStatusCode = """Call to $url failed with 
                            status [${response.statusCode}]
                            statusText [${response.statusText}]
                            body [${response.responseBodyAsString}]"""
                            return Either.Left(ResponseError(errorWithStatusCode))
                        }
                    }
                }
            })
        } catch (e: ResponseException) {
            Either.Left(DomainError("Jira/Insight hat ein internes Problem festgestellt", ""))
        }
}