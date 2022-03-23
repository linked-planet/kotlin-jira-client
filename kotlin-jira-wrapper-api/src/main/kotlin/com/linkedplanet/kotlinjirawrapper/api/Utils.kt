package com.linkedplanet.kotlinjirawrapper.api

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.linkedplanet.kotlinjirawrapper.api.error.DomainError
import java.util.regex.Pattern

suspend fun <T> recursiveRestCall(start: Int = 0, max: Int? = null, call: suspend (Int, Int) -> Either<DomainError, List<T>>): Either<DomainError, List<T>> {
    var index = start
    val maxResults = 1
    val elements = mutableListOf<T>()
    var nextPage = false
    do {
        val tmpElements: List<T> = call(index, maxResults).getOrHandle {
            return@recursiveRestCall it.left()
        }
        elements.addAll(tmpElements)
        nextPage = tmpElements.size >= maxResults
        index = index + tmpElements.size
    } while (nextPage && (max == null || index <= max))
    return elements.right()
}

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