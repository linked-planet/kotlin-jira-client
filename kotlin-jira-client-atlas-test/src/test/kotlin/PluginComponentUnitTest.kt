package com.linkedplanet.plugin.confluence.jiraclient.test

import org.junit.Test
import com.linkedplanet.plugin.confluence.jiraclient.test.api.PluginComponent

import org.junit.Assert.assertEquals

class PluginComponentUnitTest {

    @Test
    fun testMyName() {
        assertEquals("kotlin-jira-client-atlas-test", PluginComponent.name)
    }

}
