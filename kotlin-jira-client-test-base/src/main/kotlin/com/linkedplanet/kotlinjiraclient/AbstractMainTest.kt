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
package com.linkedplanet.kotlinjiraclient

import arrow.core.*
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError
import com.linkedplanet.kotlinjiraclient.api.model.*
import com.linkedplanet.kotlinjiraclient.core.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
abstract class AbstractMainTest {

    @Test
    fun test1GetIssues() {
        println("### START test1GetIssues")
        val issues: List<Story> = runBlocking {
            IssueOperator.getTicketsByIssueType(projectId, issueTypeId, parser = ::ticketParser).orNull() ?: emptyList()
        }
        Assert.assertEquals(1, issues.size)
        Assert.assertEquals("TEST-1", issues.first().key)
        Assert.assertEquals("TestInsight", issues.first().summary)
        Assert.assertEquals("IT-1", issues.first().insightObjectKey)
        Assert.assertEquals("To Do", issues.first().status.name)
        println("### END test1GetIssues")
    }

    @Test
    fun test2CreateIssue() {
        println("### START test2CreateIssue")
        val newTicket = runBlocking {
            IssueOperator.createTicket(
                projectId,
                issueTypeId,
                JiraProjectField(projectId),
                JiraIssueTypeField(issueTypeId),
                JiraSummeryField("MyNewSummary"),
                JiraCustomInsightObjectField("InsightObject", "IT-1")
            )
        }
        val searchNewTicket = runBlocking {
            IssueOperator.getTicketByJQL("summary ~ \"MyNewSummary\"", ::ticketParser).orNull()
        }

        Assert.assertTrue(searchNewTicket != null)
        Assert.assertTrue(searchNewTicket!!.summary == "MyNewSummary")
        println("### END test2CreateIssue")
    }

    @Test
    fun test3UpdateIssue() {
        println("### START test3UpdateIssue")
        val searchNewTicket = runBlocking {
            IssueOperator.getTicketByJQL("summary ~ \"MyNewSummary\"", ::ticketParser).orNull()!!
        }

        runBlocking {
            IssueOperator.updateTicket(
                projectId, issueTypeId, searchNewTicket.key,
                JiraProjectField(projectId),
                JiraIssueTypeField(issueTypeId),
                JiraSummeryField("MySecondSummary"),
                JiraCustomInsightObjectField("InsightObject", null)
            ).orNull()!!
        }
        val searchNewTicket2 = runBlocking {
            IssueOperator.getTicketByKey(searchNewTicket.key, ::ticketParser).orNull()!!
        }

        Assert.assertTrue(searchNewTicket2.key == searchNewTicket.key)
        Assert.assertTrue(searchNewTicket2.summary == "MySecondSummary")
        Assert.assertTrue(searchNewTicket2.insightObjectKey == null)
        println("### END test3UpdateIssue")
    }

    @Test
    fun test4DeleteIssue() {
        println("### START test4DeleteIssue")
        val searchNewTicket = runBlocking {
            IssueOperator.getTicketByJQL("summary ~ \"MySecondSummary\"", ::ticketParser).orNull()!!
        }
        Assert.assertNotNull(searchNewTicket)

        val deleteResponse = runBlocking {
            IssueOperator.deleteTicket(searchNewTicket.key)
        }
        Assert.assertTrue(deleteResponse.isRight())

        val searchNewTicket2 = runBlocking {
            IssueOperator.getTicketByKey(searchNewTicket.key, ::ticketParser).orNull()
        }
        Assert.assertNull(searchNewTicket2)
        println("### END test4DeleteIssue")
    }

    @Test
    fun test5GetProjects() {
        println("### START test5GetProjects")
        val projects = runBlocking {
            ProjectOperator.getProjects().orNull() ?: emptyList()
        }
        Assert.assertTrue(projects.size == 1)
        Assert.assertTrue(projects.first().id == "10000")
        Assert.assertTrue(projects.first().key == "TEST")
        Assert.assertTrue(projects.first().name == "Test")

        val project = runBlocking {
            ProjectOperator.getProject(projectId).orNull()
        }
        Assert.assertTrue(project != null)
        Assert.assertTrue(project!!.id == "10000")
        Assert.assertTrue(project!!.key == "TEST")
        Assert.assertTrue(project!!.name == "Test")
        println("### END test5GetProjects")
    }

    @Test
    fun test6GetIssueTypes() {
        println("### START test6GetIssueTypes")
        val issueTypes = runBlocking {
            IssueTypeOperator.getIssueTypes(projectId).orNull() ?: emptyList()
        }
        Assert.assertTrue(issueTypes.size == 5)
        val issueTypeNames = listOf("Bug", "Epic", "Story", "Sub-task", "Task")
        issueTypes.forEach {
            Assert.assertTrue("IssueType does not contain: ${it.name}", issueTypeNames.contains(it.name))
        }

        val issueType = runBlocking {
            IssueTypeOperator.getIssueType(issueTypeId).orNull()
        }
        Assert.assertTrue(issueType != null)
        Assert.assertTrue(issueType!!.id == issueTypeId.toString())
        Assert.assertTrue(issueType!!.name == "Story")

        val attributes = runBlocking {
            IssueTypeOperator.getAttributesOfIssueType(projectId, issueTypeId).orNull() ?: emptyList()
        }
        val attributesList = listOf(
            "Epic Link", "Summary", "Issue Type", "Reporter", "Component/s", "Description",
            "Fix Version/s", "Priority", "Labels", "Attachment", "Linked Issues", "Assignee", "Project",
            "Sprint", "InsightObject"
        )

        Assert.assertEquals(attributes.size, attributesList.size)
        attributes.forEach {
            Assert.assertTrue("Attribute does not contain: ${it.name}", attributesList.contains(it.name))
        }
        println("### END test6GetIssueTypes")
    }

    @Test
    fun test7Transitions() {
        println("### START test7Transitions")
        val transitions = runBlocking {
            JiraTransitionOperator.getAvailableTransitions("TEST-1").orNull() ?: emptyList()
        }
        Assert.assertTrue(transitions.size == 2)
        val transitionsList = listOf(
            "11", "31"
        )
        transitions.forEach {
            Assert.assertTrue("Transition id [${it.id}] not in list!", transitionsList.contains(it.id))
        }

        runBlocking {
            JiraTransitionOperator.doTransition("TEST-1", "31")
        }

        Thread.sleep(3000)

        val ticketInProgress = runBlocking {
            IssueOperator.getTicketByKey("TEST-1", ::ticketParser).orNull()
        }

        Assert.assertTrue(ticketInProgress != null)
        Assert.assertTrue(ticketInProgress!!.status.name == "In Progress")

        runBlocking {
            JiraTransitionOperator.doTransition("TEST-1", "11")
        }

        Thread.sleep(3000)

        val ticketTodo = runBlocking {
            IssueOperator.getTicketByKey("TEST-1", ::ticketParser).orNull()
        }

        Assert.assertTrue(ticketTodo != null)
        Assert.assertTrue(ticketTodo!!.status.name == "To Do")

        println("### END test7Transitions")
    }

    @Test
    fun test8GetUsers() {
        println("### START test8GetUsers")
        // Todo: Update test data for testing
        println("### END test8GetUsers")
    }

    @Test
    fun test9GetNonExistingIssue() {
        println("### START test9GetNonExistingIssue")
        val response = runBlocking {
            IssueOperator.getTicketByKey("BLAAAA") { _, _ -> Either.Right(null) }
        }
        Assert.assertTrue(response.isRight())
        Assert.assertNull(response.getOrElse { -1 })
        println("### END test9GetNonExistingIssue")
    }

    @Test
    fun test10GetTicketsIqlError() {
        println("### START test10GetTicketsIqlError")
        val response = runBlocking {
            IssueOperator.getTicketByJQL("key = BLAAAA") { _, _ -> Either.Right(null) }
        }
        Assert.assertTrue(response.isLeft())
        Assert.assertEquals(400, response.getOrHandle { (it as HttpDomainError).statusCode })
        println("### END test10GetTicketsIqlError")
    }
}