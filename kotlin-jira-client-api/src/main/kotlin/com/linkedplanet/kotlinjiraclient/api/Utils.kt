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
package com.linkedplanet.kotlinjiraclient.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.regex.Pattern

fun resolveConfig(name: String, mappings: Map<String, String>): String =
    mappings.get(name) ?: name

fun JsonObject.parseUserField(): String {
    return this.get("name").asString
}

fun JsonArray.parseInsightField(): String {
    val value = this.asJsonArray.firstOrNull()?.asString ?: ""
    val key = Pattern.compile("\\(([^)]*)\\)[^(]*\$").matcher(value).takeIf { it.find() }?.let {
        it.group(it.groupCount())
    } ?: ""
    return key
}

fun JsonArray.parseInsightFields(): List<String> {
    val values = this.asJsonArray.map { it.asString }
    val pattern = Pattern.compile("\\(([^)]*)\\)[^(]*\$")
    val keys = values.map {
        pattern.matcher(it).takeIf { it.find() }?.let {
            it.group(it.groupCount())
        } ?: ""
    }
    return keys
}