import com.linkedplanet.kotlinjirawrapper.AbstractMainTest
import com.linkedplanet.kotlinjirawrapper.api.JiraConfig
import org.junit.BeforeClass
import com.linkedplanet.kotlinhttpclient.ktor.KtorHttpClient


class MainTest : AbstractMainTest() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            println("#### Starting setUp")
            val httpClient = KtorHttpClient(
                "http://localhost:8080",
                "admin",
                "admin"
            )
            JiraConfig.init("http://localhost:8080", httpClient)
        }
    }
}