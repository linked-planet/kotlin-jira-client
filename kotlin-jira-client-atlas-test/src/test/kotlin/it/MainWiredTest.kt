package it

import com.atlassian.applinks.api.ApplicationLinkService
import com.atlassian.applinks.api.application.jira.JiraApplicationType
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import com.atlassian.confluence.user.UserAccessor
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner
import com.atlassian.sal.api.ApplicationProperties
import com.linkedplanet.kotlinhttpclient.atlas.AtlasHttpClient
import com.linkedplanet.kotlinjiraclient.AbstractMainTest
import com.linkedplanet.kotlinjiraclient.api.JiraConfig
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AtlassianPluginsTestRunner::class)
class MainWiredTest : AbstractMainTest {

    private lateinit var userAccessor: UserAccessor
    private lateinit var applicationProperties: ApplicationProperties
    private lateinit var applicationLinkService: ApplicationLinkService

    constructor(
        userAccessor: UserAccessor,
        applicationProperties: ApplicationProperties,
        applicationLinkService: ApplicationLinkService
    ) {
        this.userAccessor = userAccessor
        this.applicationProperties = applicationProperties
        this.applicationLinkService = applicationLinkService
        println("### Starting MainWiredTest")
        println("### AppLinkUrl: ${applicationLinkService.getPrimaryApplicationLink(JiraApplicationType::class.java).displayUrl}")
        val serviceUser = userAccessor.getUserByName("admin")
        AuthenticatedUserThreadLocal.asUser(serviceUser)
        val appLink = applicationLinkService.getPrimaryApplicationLink(JiraApplicationType::class.java)
        val httpClient = AtlasHttpClient(
            appLink
        )
        JiraConfig.init(appLink.rpcUrl.toString(), httpClient)
        println("### Starting MainWiredTest")
    }

    @Before
    fun initTest() {
        val serviceUser = userAccessor.getUserByName("admin")
        AuthenticatedUserThreadLocal.asUser(serviceUser)
    }
}
