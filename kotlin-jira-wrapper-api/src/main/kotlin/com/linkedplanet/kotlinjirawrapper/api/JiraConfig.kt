package com.linkedplanet.kotlinjirawrapper.api

import com.linkedplanet.kotlinhttpclient.api.http.BaseHttpClient


object JiraConfig {

    lateinit var baseUrl: String
    lateinit var httpClient: BaseHttpClient


    fun init(
        baseUrlIn: String,
        httpClientIn: BaseHttpClient
    ) {
        baseUrl = baseUrlIn
        httpClient = httpClientIn
    }
}