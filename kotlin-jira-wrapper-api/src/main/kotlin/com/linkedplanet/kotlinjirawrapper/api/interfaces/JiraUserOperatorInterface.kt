package com.linkedplanet.kotlinjirawrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.model.JiraUser

interface JiraUserOperatorInterface {

    suspend fun getUsersByProjectKey(projectKey: String): Either<DomainError, List<JiraUser>>

    suspend fun getAdminUsersByProjectKey(projectKey: String): Either<DomainError, List<JiraUser>>

    suspend fun getAssignableUsersByProjectKey(projectKey: String): Either<DomainError, List<JiraUser>>

    suspend fun getUsersByPermission(projectKey: String, permissionName: String): Either<DomainError, List<JiraUser>>

}