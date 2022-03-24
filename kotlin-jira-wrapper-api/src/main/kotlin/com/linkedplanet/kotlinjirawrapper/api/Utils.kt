package com.linkedplanet.kotlinjirawrapper.api

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