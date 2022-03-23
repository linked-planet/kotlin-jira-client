package com.linkedplanet.kotlinjirawrapper.api.interfaces

import arrow.core.Either
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlinjirawrapper.api.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.model.CreateTicketResponse
import com.linkedplanet.kotlinjirawrapper.api.model.JiraField

interface IssueOperatorInterface {

    var RESULTS_PER_PAGE: Int

    suspend fun <T> getTicketsByJQL(
        jql: String,
        startPage: Int = 1,
        endPage: Int? = null,
        perPage: Int = RESULTS_PER_PAGE,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, List<T>>

    suspend fun <T> getTicketByJQL(
        jql: String,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, T?>

    suspend fun <T> getTicketsByIssueType(
        projectId: Int,
        issueTypeId: Int,
        startPage: Int = 1,
        endPage: Int? = null,
        perPage: Int = RESULTS_PER_PAGE,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, List<T>>

    suspend fun <T> getTicketByKey(
        key: String,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, T?>

    suspend fun <T> getTicketById(
        id: Int,
        parser: suspend (JsonObject, Map<String, String>) -> Either<DomainError, T>
    ): Either<DomainError, T?>

    suspend fun createTicket(
        projectId: Int,
        issueTypeId: Int,
        vararg fields: JiraField
    ): Either<DomainError, CreateTicketResponse?>

    suspend fun updateTicket(
        projectId: Int,
        issueTypeId: Int,
        ticketKey: String,
        vararg fields: JiraField
    ): Either<DomainError, Unit>

    suspend fun deleteTicket(
        ticketKey: String
    ): Either<DomainError, Unit>
}