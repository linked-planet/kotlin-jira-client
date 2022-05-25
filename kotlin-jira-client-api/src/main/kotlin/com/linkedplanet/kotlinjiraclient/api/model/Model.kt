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
package com.linkedplanet.kotlinjiraclient.api.model

data class JiraUser(
    val key: String,
    val name: String,
    val emailAddress: String,
    var avatarUrl: String?,
    val displayName: String,
    var isAdmin: Boolean = false
)

data class JiraStatus(
    val id: String,
    val name: String,
    val color: String
)

data class JiraTransitionsResponse(
    val transitions: List<JiraTransition>
)

data class JiraTransition(
    val id: String,
    val name: String
)

data class JiraProject(
    val id: String,
    val key: String,
    val name: String
)

data class JiraIssueTypesResponse(
    val values: List<JiraIssueType>
)

data class JiraIssueTypeResponse(
    val self: String,
    val id: String,
    val description: String,
    val iconUrl: String,
    val name: String,
    val subtask: Boolean,
    val avatarId: Int
)

data class JiraIssueType(
    val id: String,
    val name: String
)

data class JiraIssueTypeAttributesResponse(
    val values: List<JiraIssueTypeAttributeResponse>
)

data class JiraIssueTypeAttributeResponse(
    val name: String,
    val fieldId: String
)

data class JiraIssueTypeAttribute(
    val id: String,
    val name: String
)

data class CreateTicketResponse(
    val id: String,
    val key: String,
    val self: String
)

data class IssueCommentRequest(
    val issueKey: String,
    val content: String
)

data class IssueComment(
    val id: String,
    val content: String,
    val author: String,
    val dateTime: String
)