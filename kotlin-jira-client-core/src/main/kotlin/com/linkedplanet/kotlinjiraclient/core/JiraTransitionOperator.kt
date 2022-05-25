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
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError
import com.linkedplanet.kotlinjiraclient.api.JiraConfig
import com.linkedplanet.kotlinjiraclient.api.interfaces.JiraTransitionOperatorInterface
import com.linkedplanet.kotlinjiraclient.api.model.JiraTransition
import com.linkedplanet.kotlinjiraclient.api.model.JiraTransitionsResponse

object JiraTransitionOperator : JiraTransitionOperatorInterface {

    override suspend fun getAvailableTransitions(ticketKey: String): Either<HttpDomainError, List<JiraTransition>> =
        either {
            JiraConfig.httpClient.executeRest<JiraTransitionsResponse>(
                "GET",
                "/rest/api/2/issue/$ticketKey/transitions",
                emptyMap(),
                null,
                "application/json",
                object : TypeToken<JiraTransitionsResponse>() {}.type
            ).map { it.body?.transitions ?: emptyList() }.bind()
        }

    override suspend fun doTransition(
        issueKey: String,
        transitionId: String,
        comment: String?
    ): Either<HttpDomainError, Boolean> {
        val commentJson = """
            "update": {
                "comment": [
                    {
                        "add": {
                            "body": "$comment"
                        }
                    }
                ]
            },
        """.trimIndent()
        val json = """
                {
                    ${
            if (comment != null) {
                commentJson
            } else ""
        }
                    "transition": {
                        "id": "$transitionId"
                    }
                }""".trimIndent()
        return JiraConfig.httpClient.executeRestCall(
            "POST",
            "/rest/api/2/issue/$issueKey/transitions",
            emptyMap(),
            json,
            "application/json"
        ).map { true }
    }

}