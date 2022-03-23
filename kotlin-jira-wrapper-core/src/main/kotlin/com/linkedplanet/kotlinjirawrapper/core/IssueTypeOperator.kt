package com.linkedplanet.kotlinjirawrapper.core

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlinjirawrapper.api.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.http.JiraConfig
import com.linkedplanet.kotlinjirawrapper.api.interfaces.IssueTypeOperatorInterface
import com.linkedplanet.kotlinjirawrapper.api.model.*

object IssueTypeOperator : IssueTypeOperatorInterface {

    override suspend fun getIssueTypes(projectId: Int): Either<DomainError, List<JiraIssueType>> = either {
        JiraConfig.httpClient.executeRest<JiraIssueTypesResponse>(
            "GET",
            "/rest/api/2/issue/createmeta/$projectId/issuetypes",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<JiraIssueTypesResponse>() {}.type
        ).map { it?.values ?: emptyList() }.bind()
    }

    override suspend fun getIssueType(issueTypeId: Int): Either<DomainError, JiraIssueType?> = either {
        JiraConfig.httpClient.executeGet<JiraIssueTypeResponse>(
            "/rest/api/2/issuetype/$issueTypeId",
            emptyMap(),
            object : TypeToken<JiraIssueTypeResponse>() {}.type
        ).bind()?.let {
            JiraIssueType(
                it.id,
                it.name
            )
        }
    }

    override suspend fun getAttributesOfIssueType(
        projectId: Int,
        issueTypeId: Int
    ): Either<DomainError, List<JiraIssueTypeAttribute>> = either {
        JiraConfig.httpClient.executeRest<JiraIssueTypeAttributesResponse>(
            "GET",
            "/rest/api/2/issue/createmeta/$projectId/issuetypes/$issueTypeId",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<JiraIssueTypeAttributesResponse>() {}.type
        ).map {
            (it?.values ?: emptyList())
                .map { JiraIssueTypeAttribute(it.fieldId, it.name) }
        }.bind()
    }

}