package com.linkedplanet.kotlinjirawrapper.core

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.left
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.JiraConfig
import com.linkedplanet.kotlinjirawrapper.api.interfaces.CommentOperatorInterface
import com.linkedplanet.kotlinjirawrapper.api.model.IssueComment

object CommentOperator : CommentOperatorInterface {

    override var RESULTS_PER_PAGE: Int = 25

    override suspend fun getComments(
        issueKey: String,
        startPage: Int,
        endPage: Int?,
        perPage: Int
    ): Either<DomainError, List<IssueComment>> = either {
        val ticketsToLoad = RESULTS_PER_PAGE
        var commentObjects = emptyList<IssueComment>()
        val max = endPage?.let { it*perPage }?:Int.MAX_VALUE
        var startAt = startPage
        var size: Int
        do {
            val json = JiraConfig.httpClient.executeGetCall(
                "/rest/api/2/issue/$issueKey/comment",
                mapOf(
                    "startAt" to startAt.toString(),
                    "maxResults" to "$ticketsToLoad"
                ),
            ).bind()

            val jsonObject = JsonParser.parseString(json).asJsonObject

            startAt = jsonObject.get("startAt").asInt + ticketsToLoad
            size = jsonObject.get("total").asInt
            val currentComments: List<IssueComment> = if (size == 0) {
                emptyList()
            } else {
                jsonObject.getAsJsonArray("comments").map {
                    val obj = it.asJsonObject
                    IssueComment(
                        obj.get("id").asString,
                        obj.get("body").asString,
                        obj.get("author").asJsonObject.get("name").asString,
                        obj.get("created").asString
                    )
                }
            }
            commentObjects = commentObjects + currentComments
        } while (startAt < size || startAt >= max)
        commentObjects
    }

    override suspend fun createComment(issueKey: String, content: String): Either<DomainError, Unit> = either {
        val jsonBody = JsonObject()
        jsonBody.addProperty("body", content)
        JiraConfig.httpClient.executeRestCall(
            "POST",
            "/rest/api/2/issue/$issueKey/comment",
            emptyMap(),
            jsonBody.toString(),
            "application/json"
        ).bind()
    }

    override suspend fun updateComment(
        issueKey: String,
        commentId: String,
        content: String
    ): Either<DomainError, Unit> = either {
        val jsonBody = JsonObject()
        jsonBody.addProperty("body", content)
        JiraConfig.httpClient.executeRestCall(
            "PUT",
            "/rest/api/2/issue/$issueKey/comment/$commentId",
            emptyMap(),
            jsonBody.toString(),
            "application/json"
        ).bind()
    }

    override suspend fun deleteComment(issueKey: String, id: String): Either<DomainError, Unit> = either {
        JiraConfig.httpClient.executeRestCall(
            "DELETE",
            "/rest/api/2/issue/$issueKey/comment/$id",
            emptyMap(),
            null,
            "application/json"
        ).bind()
    }

}