package com.linkedplanet.kotlinjirawrapper.api.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.linkedplanet.kotlinjirawrapper.api.resolveConfig
import org.joda.time.DateTime


// JIRA Field definitions

abstract class JiraField {
    abstract fun render(jsonObject: JsonObject, mappings: Map<String, String>)
}

class JiraSummeryField(private val summary: String) : JiraField() {

    override fun render(jsonObject: JsonObject, mappings: Map<String, String>) {
        jsonObject.addProperty("summary", summary)
    }
}

class JiraDescriptionField(private val description: String) : JiraField() {
    override fun render(jsonObject: JsonObject, mappings: Map<String, String>) {
        jsonObject.addProperty("description", description)
    }
}

class JiraProjectField(private val projectId: Int) : JiraField() {
    override fun render(jsonObject: JsonObject, mappings: Map<String, String>) {
        val projectJson = JsonObject()
        projectJson.addProperty("id", projectId)
        jsonObject.add("project", projectJson)
    }
}

class JiraIssueTypeField(private val issueTypeId: Int) : JiraField() {
    override fun render(jsonObject: JsonObject, mappings: Map<String, String>) {
        val projectJson = JsonObject()
        projectJson.addProperty("id", issueTypeId)
        jsonObject.add("issuetype", projectJson)
    }
}

class JiraIssueTypeNameField(private val issueTypeName: String) : JiraField() {
    override fun render(jsonObject: JsonObject, mappings: Map<String, String>) {
        val projectJson = JsonObject()
        projectJson.addProperty("name", issueTypeName)
        jsonObject.add("issuetype", projectJson)
    }
}

class JiraAssigneeField(private val username: String) : JiraField() {
    override fun render(jsonObject: JsonObject, mappings: Map<String, String>) {
        val fieldName = resolveConfig("assignee", mappings)
        val userJson = JsonObject()
        userJson.addProperty("name", username)
        jsonObject.add(fieldName, userJson)
    }
}

class JiraEpicField(
    private val ticketKey: String?
) : JiraField() {
    override fun render(result: JsonObject, mappings: Map<String, String>) {
        val fieldName = resolveConfig("Epic Link", mappings)
        result.addProperty(fieldName, ticketKey)
    }
}

class JiraCustomInsightObjectField(
    private val customFieldName: String,
    private val insightKey: String?
) : JiraField() {
    override fun render(result: JsonObject, mappings: Map<String, String>) {
        val fieldName = resolveConfig(customFieldName, mappings)
        val jsonArray = JsonArray()
        val jsonObject = JsonObject()
        jsonObject.addProperty("key", insightKey)
        jsonArray.add(jsonObject)
        result.add(fieldName, jsonArray)
    }
}

class JiraCustomInsightObjectsField(
    private val customFieldName: String,
    private val insightKeys: List<String>
) : JiraField() {
    override fun render(result: JsonObject, mappings: Map<String, String>) {
        val fieldName = resolveConfig(customFieldName, mappings)
        val jsonArray = JsonArray()
        insightKeys.forEach { objectKey ->
            val jsonObject = JsonObject()
            jsonObject.addProperty("key", objectKey)
            jsonArray.add(jsonObject)
        }
        result.add(fieldName, jsonArray)
    }
}

class JiraCustomTextTimeField(
    private val customFieldName: String,
    private val value: String
) : JiraField() {
    override fun render(result: JsonObject, mappings: Map<String, String>) {
        // Date to String
        val fieldName = resolveConfig(customFieldName, mappings)
        result.addProperty(fieldName, value)
    }
}

class JiraCustomIntegerTimeField(
    private val customFieldName: String,
    private val value: Int
) : JiraField() {
    override fun render(result: JsonObject, mappings: Map<String, String>) {
        // Date to String
        val fieldName = resolveConfig(customFieldName, mappings)
        result.addProperty(fieldName, value)
    }
}

class JiraCustomDateTimeField(
    private val customFieldName: String,
    private val value: DateTime?,
    private val mappings: Map<String, String>
) : JiraField() {
    override fun render(result: JsonObject, mappings: Map<String, String>) {
        // Date to String
        val fieldName = resolveConfig(customFieldName, mappings)
        val dateToJiraDateString = value.toString()
        result.addProperty(fieldName, dateToJiraDateString)
    }
}

class JiraCustomYesNoField(
    private val customFieldName: String,
    private val value: Boolean
) : JiraField() {
    override fun render(result: JsonObject, mappings: Map<String, String>) {
        val fieldName = resolveConfig(customFieldName, mappings)
        val valueIn = run {
            val jsonObject = JsonObject()
            jsonObject.add("value", if (value) JsonPrimitive("yes") else JsonPrimitive("no"))
            jsonObject
        }
        result.add(fieldName, valueIn)
    }
}

class JiraCustomRadioField(
    private val customFieldName: String,
    private val value: String
) : JiraField() {
    override fun render(result: JsonObject, mappings: Map<String, String>) {
        val fieldName = resolveConfig(customFieldName, mappings)
        val valueIn = run {
            val jsonObject = JsonObject()
            jsonObject.add("value", JsonPrimitive(value))
            jsonObject
        }
        result.add(fieldName, valueIn)
    }
}