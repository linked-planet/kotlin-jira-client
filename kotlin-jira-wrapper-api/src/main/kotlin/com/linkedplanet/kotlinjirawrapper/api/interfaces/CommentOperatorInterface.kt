package com.linkedplanet.kotlinjirawrapper.api.interfaces

import arrow.core.Either
import com.google.gson.JsonObject
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.model.CreateTicketResponse
import com.linkedplanet.kotlinjirawrapper.api.model.IssueComment
import com.linkedplanet.kotlinjirawrapper.api.model.JiraField

interface CommentOperatorInterface {

    var RESULTS_PER_PAGE: Int

    suspend fun getComments(
        issueKey: String,
        startPage: Int = 1,
        endPage: Int? = null,
        perPage: Int = RESULTS_PER_PAGE,
    ): Either<DomainError, List<IssueComment>>

    suspend fun createComment(
        issueKey: String,
        content: String
    ): Either<DomainError, Unit>

    suspend fun  updateComment(
        issueKey: String,
        commentId: String,
        content: String
    ): Either<DomainError, Unit>

    suspend fun deleteComment(
        issueKey: String,
        id: String
    ): Either<DomainError, Unit>
}