package com.linkedplanet.kotlinjirawrapper.core

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlinjirawrapper.api.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.http.JiraConfig
import com.linkedplanet.kotlinjirawrapper.api.interfaces.ProjectOperatorInterface
import com.linkedplanet.kotlinjirawrapper.api.model.JiraProject

object ProjectOperator : ProjectOperatorInterface {

    override suspend fun getProjects(): Either<DomainError, List<JiraProject>> = either {
        JiraConfig.httpClient.executeRestList<JiraProject>(
            "GET",
            "/rest/api/2/project",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<JiraProject>>() {}.type
        ).bind()
    }

    override suspend fun getProject(projectId: Int): Either<DomainError, JiraProject?> = either {
        JiraConfig.httpClient.executeRest<JiraProject?>(
            "GET",
            "/rest/api/2/project/$projectId",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<JiraProject?>() {}.type
        ).bind()
    }

}