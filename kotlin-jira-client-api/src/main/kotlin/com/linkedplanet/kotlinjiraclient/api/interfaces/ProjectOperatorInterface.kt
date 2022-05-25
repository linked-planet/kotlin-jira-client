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
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError
import com.linkedplanet.kotlinjiraclient.api.model.JiraProject

interface ProjectOperatorInterface {

    suspend fun getProjects(): Either<HttpDomainError, List<JiraProject>>

    suspend fun getProject(projectId: Int): Either<HttpDomainError, JiraProject?>
}