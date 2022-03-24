package com.linkedplanet.kotlinjirawrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.model.JiraIssueType
import com.linkedplanet.kotlinjirawrapper.api.model.JiraIssueTypeAttribute

interface IssueTypeOperatorInterface {

    suspend fun getIssueTypes(
        projectId: Int
    ): Either<DomainError, List<JiraIssueType>>

    suspend fun getIssueType(
        issueTypeId: Int
    ): Either<DomainError, JiraIssueType?>

    suspend fun getAttributesOfIssueType(
        projectId: Int,
        issueTypeId: Int
    ): Either<DomainError, List<JiraIssueTypeAttribute>>
}