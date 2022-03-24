package com.linkedplanet.kotlinjirawrapper.core

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.JiraConfig
import com.linkedplanet.kotlinjirawrapper.api.interfaces.JiraTransitionOperatorInterface
import com.linkedplanet.kotlinjirawrapper.api.model.JiraTransition
import com.linkedplanet.kotlinjirawrapper.api.model.JiraTransitionsResponse

object JiraTransitionOperator : JiraTransitionOperatorInterface {

    override suspend fun getAvailableTransitions(ticketKey: String): Either<DomainError, List<JiraTransition>> =
        either {
            JiraConfig.httpClient.executeRest<JiraTransitionsResponse>(
                "GET",
                "/rest/api/2/issue/$ticketKey/transitions",
                emptyMap(),
                null,
                "application/json",
                object : TypeToken<JiraTransitionsResponse>() {}.type
            ).map { it?.transitions ?: emptyList() }.bind()
        }

    override suspend fun doTransition(
        issueKey: String,
        transitionId: String,
        comment: String?
    ): Either<DomainError, Boolean> {
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