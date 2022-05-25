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
import com.linkedplanet.kotlinhttpclient.api.http.recursiveRestCall
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError
import com.linkedplanet.kotlinjiraclient.api.JiraConfig
import com.linkedplanet.kotlinjiraclient.api.interfaces.JiraUserOperatorInterface
import com.linkedplanet.kotlinjiraclient.api.model.JiraUser
import kotlinx.coroutines.runBlocking

object JiraUserOperator : JiraUserOperatorInterface {

    override suspend fun getUsersByProjectKey(projectKey: String): Either<HttpDomainError, List<JiraUser>> = either {
        recursiveRestCall { index, maxResults ->
            runBlocking {
                JiraConfig.httpClient.executeRestList<JiraUser>(
                    "GET",
                    "/rest/api/2/user/assignable/search?project=$projectKey&startAt=$index&maxResults=$maxResults",
                    emptyMap(),
                    null,
                    "application/json",
                    object : TypeToken<List<JiraUser>>() {}.type
                ).map { it.body }
            }
        }.bind()
    }

    override suspend fun getAdminUsersByProjectKey(projectKey: String): Either<HttpDomainError, List<JiraUser>> =
        getUsersByPermission(projectKey, "PROJECT_ADMIN")

    override suspend fun getAssignableUsersByProjectKey(projectKey: String): Either<HttpDomainError, List<JiraUser>> =
        getUsersByPermission(projectKey, "ASSIGNABLE_USER")

    override suspend fun getUsersByPermission(
        projectKey: String,
        permissionName: String
    ): Either<HttpDomainError, List<JiraUser>> = either {
        recursiveRestCall { index, maxResults ->
            runBlocking {
                JiraConfig.httpClient.executeRestList<JiraUser>(
                    "GET",
                    "/rest/api/2/user/permission/search?permissions=$permissionName&projectKey=$projectKey&startAt=$index&maxResults=$maxResults",
                    emptyMap(),
                    null,
                    "application/json",
                    object : TypeToken<List<JiraUser>>() {}.type
                ).map { it.body }
            }
        }.bind()
    }


}