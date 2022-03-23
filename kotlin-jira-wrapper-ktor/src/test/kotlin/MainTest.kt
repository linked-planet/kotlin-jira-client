import com.linkedplanet.kotlinjirawrapper.AbstractMainTest
import com.linkedplanet.kotlinjirawrapper.api.http.JiraConfig
import com.linkedplanet.kotlinjirawrapper.ktor.KtorHttpClient
import org.junit.BeforeClass


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