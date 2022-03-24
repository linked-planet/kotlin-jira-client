package com.linkedplanet.kotlinjirawrapper

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.JsonObject
import com.linkedplanet.kotlinjirawrapper.api.model.JiraStatus
import com.linkedplanet.kotlinjirawrapper.api.parseInsightField
import com.linkedplanet.kotlinjirawrapper.api.resolveConfig
import com.linkedplanet.kotlinhttpclient.error.DomainError

val projectId: Int = 10000
val issueTypeId: Int = 10001

data class Story(
    val key: String,
    val summary: String,
    val insightObjectKey: String?,
    val status: JiraStatus
)

suspend fun ticketParser(jsonObject: JsonObject, map: Map<String, String>): Either<DomainError, Story> = either {
    val fields = jsonObject.get("fields").asJsonObject
    val insightObjectKey = fields.get(resolveConfig("InsightObject", map))
        .takeIf { it.isJsonArray }?.asJsonArray?.parseInsightField()
    val statusObject: JsonObject = fields.get("status").asJsonObject
    val status = JiraStatus(
        statusObject.get("id").asString,
        statusObject.get("name").asString,
        statusObject.get("statusCategory").asJsonObject.get("key").asString
    )
    Story(
        jsonObject.get("key").asString,
        fields.get("summary").asString,
        insightObjectKey,
        status
    )
}