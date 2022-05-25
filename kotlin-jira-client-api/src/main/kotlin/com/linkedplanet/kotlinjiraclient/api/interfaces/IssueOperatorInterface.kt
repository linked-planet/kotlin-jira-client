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
package com.linkedplanet.kotlinjiraclient.api.interfaces

import arrow.core.Either
import com.google.gson.JsonObject
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError
import com.linkedplanet.kotlinjiraclient.api.model.CreateTicketResponse
import com.linkedplanet.kotlinjiraclient.api.model.JiraField

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
    ): Either<HttpDomainError, CreateTicketResponse?>

    suspend fun updateTicket(
        projectId: Int,
        issueTypeId: Int,
        ticketKey: String,
        vararg fields: JiraField
    ): Either<HttpDomainError, Unit>

    suspend fun deleteTicket(
        ticketKey: String
    ): Either<HttpDomainError, Unit>
}