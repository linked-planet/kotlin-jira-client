package com.linkedplanet.kotlinjirawrapper.core

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.JiraConfig
import com.linkedplanet.kotlinjirawrapper.api.interfaces.IssueOperatorInterface
import com.linkedplanet.kotlinjirawrapper.api.model.CreateTicketResponse
import com.linkedplanet.kotlinjirawrapper.api.model.JiraField

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

            val jsonObject = JsonParser().parse(json).asJsonObject

            startAt = jsonObject.get("startAt").asInt + ticketsToLoad
            size = jsonObject.get("total").asInt
            val bookings: List<T> = if (size == 0) {
                emptyList()
            } else {
                val response = JsonParser().parse(json).asJsonObject
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
        getTicketByJQL("key=$key", parser).bind()
    }

    override suspend fun <T> getTicketById(
        id: Int,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, T?> = either {
        getTicketByJQL("id=$id", parser).bind()
    }

    override suspend fun createTicket(
        projectId: Int,
        issueTypeId: Int,
        vararg fields: JiraField
    ): Either<DomainError, CreateTicketResponse?> = either {
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
        ).bind()
    }

    override suspend fun updateTicket(
        projectId: Int,
        issueTypeId: Int,
        ticketKey: String,
        vararg fields: JiraField
    ): Either<DomainError, Unit> = either {
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
    ): Either<DomainError, Unit> = either {
        JiraConfig.httpClient.executeRestCall(
            "DELETE",
            "/rest/api/2/issue/$ticketKey",
            emptyMap(),
            null,
            "application/json"
        ).bind()
    }

    // private methods down here

    private suspend fun getMappings(projectId: String, issueTypeId: String): Either<DomainError, Map<String, String>> {
        val response = JiraConfig.httpClient.executeGetCall(
            "/rest/api/2/issue/createmeta",
            mapOf(
                "projectIds" to projectId,
                "issuetypeIds" to issueTypeId,
                "expand" to "projects.issuetypes.fields"
            ),
        )

        return response.map { json ->
            val jsonObject = JsonParser().parse(json).asJsonObject
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