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
package com.linkedplanet.kotlinjiraclient

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.JsonObject
import com.linkedplanet.kotlinjiraclient.api.model.JiraStatus
import com.linkedplanet.kotlinjiraclient.api.parseInsightField
import com.linkedplanet.kotlinjiraclient.api.resolveConfig
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError

val projectId: Int = 10000
val issueTypeId: Int = 10001

data class Story(
    val key: String,
    val summary: String,
    val insightObjectKey: String?,
    val status: JiraStatus
)

suspend fun ticketParser(jsonObject: JsonObject, map: Map<String, String>): Either<HttpDomainError, Story> = either {
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