package com.linkedplanet.plugin.confluence.jirawrapper.test

import org.junit.Test
import com.linkedplanet.plugin.confluence.jirawrapper.test.api.PluginComponent

import org.junit.Assert.assertEquals

class PluginComponentUnitTest {

    @Test
    fun testMyName() {
        assertEquals("kotlin-jira-wrapper-atlas-test", PluginComponent.name)
    }

}
