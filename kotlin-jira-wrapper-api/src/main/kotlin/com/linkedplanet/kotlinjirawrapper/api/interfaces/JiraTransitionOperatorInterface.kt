package com.linkedplanet.kotlinjirawrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinjirawrapper.api.model.JiraTransition

interface JiraTransitionOperatorInterface {

    suspend fun getAvailableTransitions(ticketKey: String): Either<DomainError, List<JiraTransition>>

    suspend fun doTransition(
        issueKey: String,
        transitionId: String,
        comment: String? = null
    ): Either<DomainError, Boolean>
}