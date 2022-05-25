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

import arrow.core.*
import arrow.core.computations.either
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError
import com.linkedplanet.kotlinjiraclient.api.JiraConfig
import com.linkedplanet.kotlinjiraclient.api.interfaces.IssueOperatorInterface
import com.linkedplanet.kotlinjiraclient.api.model.CreateTicketResponse
import com.linkedplanet.kotlinjiraclient.api.model.JiraField

object IssueOperator : IssueOperatorInterface {

    override var RESULTS_PER_PAGE: Int = 25

    override suspend fun <T> getTicketsByJQL(
        jql: String,
        startPage: Int,
        endPage: Int?,
        perPage: Int,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, List<T>> = either {
        val ticketsToLoad = 1000
        var ticketObjects = emptyList<T>()
        var startAt = 0
        var size: Int
        do {
            val json = JiraConfig.httpClient.executeGetCall(
                "/rest/api/2/search",
                mapOf(
                    "jql" to jql,
                    "startAt" to startAt.toString(),
                    "maxResults" to "$ticketsToLoad",
                    "expand" to "names,transitions",
                ),
            ).bind()

            val jsonObject = JsonParser().parse(json.body).asJsonObject

            startAt = jsonObject.get("startAt").asInt + ticketsToLoad
            size = jsonObject.get("total").asInt
            val bookings: List<T> = if (size == 0) {
                emptyList()
            } else {
                val response = JsonParser().parse(json.body).asJsonObject
                val names = response.get("names").asJsonObject
                // name to customfield_XXXX
                val mappings = names
                    .keySet()
                    .map {
                        names.get(it).asString to it
                    }.toMap()
                val issues = response.get("issues").asJsonArray
                issues.map {
                    parser(it.asJsonObject, mappings).bind()
                }
            }
            ticketObjects = ticketObjects + bookings
        } while (startAt < size)
        ticketObjects
    }

    override suspend fun <T> getTicketByJQL(
        jql: String,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, T?> = either {
        getTicketsByJQL(jql, 1, 1, 1, parser).bind().firstOrNull()
    }

    override suspend fun <T> getTicketsByIssueType(
        projectId: Int,
        issueTypeId: Int,
        startPage: Int,
        endPage: Int?,
        perPage: Int,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, List<T>> = either {
        getTicketsByJQL("project=$projectId AND issueType=$issueTypeId", startPage, endPage, perPage, parser).bind()
    }

    override suspend fun <T> getTicketByKey(
        key: String,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, T?> = either {
        val httpResponse = JiraConfig.httpClient.executeGetCall(
            "/rest/api/2/issue/${key}",
            mapOf(
                "expand" to "names,transitions"
            ),
        ).map { it.body }

        // if response is 404 this means no object found therefore null as valid response
        val successResponse = httpResponse.getOrHandle {
            if(it.statusCode == 404) {
                return@either null
            } else {
                it.left().bind()
            }
        }

        val jsonObject = JsonParser.parseString(successResponse).asJsonObject
        if (jsonObject.has("id")) {
            val names = jsonObject.get("names").asJsonObject
            val mappings = names
                .keySet()
                .associateBy {
                    names.get(it).asString
                }
            parser(jsonObject, mappings).bind()
        } else {
            null
        }
    }

    override suspend fun <T> getTicketById(
        id: Int,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, T?> = getTicketByKey(id.toString(), parser)

    override suspend fun createTicket(
        projectId: Int,
        issueTypeId: Int,
        vararg fields: JiraField
    ): Either<HttpDomainError, CreateTicketResponse?> = either {
        val mappingsResponse = getMappings(projectId.toString(), issueTypeId.toString())
        val jsonBody = mappingsResponse.map { mappings ->
            val jsonBody = JsonObject()
            val fieldsObject = JsonObject()
            fields.onEach {
                it.render(fieldsObject, mappings)
            }
            jsonBody.add("fields", fieldsObject)
            jsonBody
        }.bind()

        JiraConfig.httpClient.executeRest<CreateTicketResponse>(
            "POST",
            "/rest/api/2/issue",
            emptyMap(),
            jsonBody.toString(),
            "application/json",
            object : TypeToken<CreateTicketResponse>() {}.type
        ).bind().body
    }

    override suspend fun updateTicket(
        projectId: Int,
        issueTypeId: Int,
        ticketKey: String,
        vararg fields: JiraField
    ): Either<HttpDomainError, Unit> = either {
        val mappingsResponse = getMappings(projectId.toString(), issueTypeId.toString())
        val jsonBody = mappingsResponse.map { mappings ->
            val jsonBody = JsonObject()
            val fieldsObject = JsonObject()
            fields.onEach {
                it.render(fieldsObject, mappings)
            }
            jsonBody.add("fields", fieldsObject)
            jsonBody
        }.bind()

        JiraConfig.httpClient.executeRestCall(
            "PUT",
            "/rest/api/2/issue/$ticketKey",
            emptyMap(),
            jsonBody.toString(),
            "application/json"
        ).bind()
    }

    override suspend fun deleteTicket(
        ticketKey: String
    ): Either<HttpDomainError, Unit> = either {
        JiraConfig.httpClient.executeRestCall(
            "DELETE",
            "/rest/api/2/issue/$ticketKey",
            emptyMap(),
            null,
            "application/json"
        ).bind()
    }

    // private methods down here

    private suspend fun getMappings(
        projectId: String,
        issueTypeId: String
    ): Either<HttpDomainError, Map<String, String>> {
        val response = JiraConfig.httpClient.executeGetCall(
            "/rest/api/2/issue/createmeta",
            mapOf(
                "projectIds" to projectId,
                "issuetypeIds" to issueTypeId,
                "expand" to "projects.issuetypes.fields"
            ),
        )

        return response.map { json ->
            val jsonObject = JsonParser().parse(json.body).asJsonObject
            jsonObject
                .get("projects").asJsonArray[0].asJsonObject
                .getAsJsonArray("issuetypes")[0].asJsonObject
                .getAsJsonObject("fields")
                .entrySet()
                .map { it.value.asJsonObject.get("name").asString to it.key }
                .toMap()
        }
    }
}