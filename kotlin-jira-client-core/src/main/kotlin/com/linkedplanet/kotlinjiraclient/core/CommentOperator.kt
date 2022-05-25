/**
 * Copyright 2022 linked-planet GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedplanet.kotlinjiraclient.core

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError
import com.linkedplanet.kotlinjiraclient.api.JiraConfig
import com.linkedplanet.kotlinjiraclient.api.interfaces.CommentOperatorInterface
import com.linkedplanet.kotlinjiraclient.api.model.IssueComment

object CommentOperator : CommentOperatorInterface {

    override var RESULTS_PER_PAGE: Int = 25

    override suspend fun getComments(
        issueKey: String,
        startPage: Int,
        endPage: Int?,
        perPage: Int
    ): Either<HttpDomainError, List<IssueComment>> = either {
        val ticketsToLoad = RESULTS_PER_PAGE
        var commentObjects = emptyList<IssueComment>()
        val max = endPage?.let { it * perPage } ?: Int.MAX_VALUE
        var startAt = startPage
        var size: Int
        do {
            val json = JiraConfig.httpClient.executeGetCall(
                "/rest/api/2/issue/$issueKey/comment",
                mapOf(
                    "startAt" to startAt.toString(),
                    "maxResults" to "$ticketsToLoad"
                ),
            ).bind().body

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

    override suspend fun createComment(issueKey: String, content: String): Either<HttpDomainError, Unit> =
        either {
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
    ): Either<HttpDomainError, Unit> = either {
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

    override suspend fun deleteComment(issueKey: String, id: String): Either<HttpDomainError, Unit> =
        either {
            JiraConfig.httpClient.executeRestCall(
                "DELETE",
                "/rest/api/2/issue/$issueKey/comment/$id",
                emptyMap(),
                null,
                "application/json"
            ).bind()
        }

}