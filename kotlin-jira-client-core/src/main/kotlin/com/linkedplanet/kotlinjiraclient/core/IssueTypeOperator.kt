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
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError
import com.linkedplanet.kotlinjiraclient.api.JiraConfig
import com.linkedplanet.kotlinjiraclient.api.interfaces.IssueTypeOperatorInterface
import com.linkedplanet.kotlinjiraclient.api.model.*

object IssueTypeOperator : IssueTypeOperatorInterface {

    override suspend fun getIssueTypes(projectId: Int): Either<HttpDomainError, List<JiraIssueType>> = either {
        JiraConfig.httpClient.executeRest<JiraIssueTypesResponse>(
            "GET",
            "/rest/api/2/issue/createmeta/$projectId/issuetypes",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<JiraIssueTypesResponse>() {}.type
        ).map { it.body?.values ?: emptyList() }.bind()
    }

    override suspend fun getIssueType(issueTypeId: Int): Either<HttpDomainError, JiraIssueType?> = either {
        JiraConfig.httpClient.executeGet<JiraIssueTypeResponse>(
            "/rest/api/2/issuetype/$issueTypeId",
            emptyMap(),
            object : TypeToken<JiraIssueTypeResponse>() {}.type
        ).bind()
            .takeIf { it.body != null }
            ?.let {
                JiraIssueType(
                    it.body!!.id,
                    it.body!!.name
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
            (it.body?.values ?: emptyList())
                .map { JiraIssueTypeAttribute(it.fieldId, it.name) }
        }.bind()
    }

}