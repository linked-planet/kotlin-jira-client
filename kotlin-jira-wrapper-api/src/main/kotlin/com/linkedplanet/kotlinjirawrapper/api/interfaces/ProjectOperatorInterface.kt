package com.linkedplanet.kotlinjirawrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlinjirawrapper.api.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.model.JiraProject

interface ProjectOperatorInterface {

    suspend fun getProjects(): Either<DomainError, List<JiraProject>>

    suspend fun getProject(projectId: Int): Either<DomainError, JiraProject?>
}