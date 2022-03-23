package com.linkedplanet.kotlinjirawrapper.core

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlinjirawrapper.api.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.http.JiraConfig
import com.linkedplanet.kotlinjirawrapper.api.interfaces.JiraUserOperatorInterface
import com.linkedplanet.kotlinjirawrapper.api.model.JiraUser
import com.linkedplanet.kotlinjirawrapper.api.recursiveRestCall

object JiraUserOperator : JiraUserOperatorInterface {

    override suspend fun getUsersByProjectKey(projectKey: String): Either<DomainError, List<JiraUser>> = either {
        recursiveRestCall { index, maxResults ->
            JiraConfig.httpClient.executeRestList<JiraUser>(
                "GET",
                "/rest/api/2/user/assignable/search?project=$projectKey&startAt=$index&maxResults=$maxResults",
                emptyMap(),
                null,
                "application/json",
                object : TypeToken<List<JiraUser>>() {}.type
            )
        }.bind()
    }

    override suspend fun getAdminUsersByProjectKey(projectKey: String): Either<DomainError, List<JiraUser>> =
        getUsersByPermission(projectKey, "PROJECT_ADMIN")

    override suspend fun getAssignableUsersByProjectKey(projectKey: String): Either<DomainError, List<JiraUser>> =
        getUsersByPermission(projectKey, "ASSIGNABLE_USER")

    override suspend fun getUsersByPermission(
        projectKey: String,
        permissionName: String
    ): Either<DomainError, List<JiraUser>> = either {
        recursiveRestCall { index, maxResults ->
            JiraConfig.httpClient.executeRestList<JiraUser>(
                "GET",
                "/rest/api/2/user/permission/search?permissions=$permissionName&projectKey=$projectKey&startAt=$index&maxResults=$maxResults",
                emptyMap(),
                null,
                "application/json",
                object : TypeToken<List<JiraUser>>() {}.type
            )
        }.bind()
    }


}