package com.linkedplanet.kotlinjirawrapper.api.error

import com.linkedplanet.kotlinjirawrapper.api.http.GSON

data class DomainErrorObject(
    val error: String,
    val message: String
)

open class DomainError(val error: String, val message: String) {

    fun toJson(): String =
        GSON.toJson(DomainErrorObject(error, message))
}

class ResponseError(message: String) : DomainError("Schnittstellen-Fehler", message)