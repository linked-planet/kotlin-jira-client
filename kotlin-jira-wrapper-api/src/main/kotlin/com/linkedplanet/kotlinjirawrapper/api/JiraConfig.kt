package com.linkedplanet.kotlinjirawrapper.api

import com.linkedplanet.kotlinhttpclient.api.http.BaseHttpClient


object JiraConfig {

    lateinit var baseUrl: String
    lateinit var httpClient: BaseHttpClient


    fun <T: BaseHttpClient> init(
        baseUrlIn: String,
        httpClientIn: T
    ) {
        baseUrl = baseUrlIn
        httpClient = httpClientIn
    }
}