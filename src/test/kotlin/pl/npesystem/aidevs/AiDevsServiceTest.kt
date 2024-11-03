package pl.npesystem.aidevs

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@QuarkusTest
class AiDevsServiceTest {

    @Inject
    lateinit var aiDevsService: AiDevsService

    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    @Test
    fun `test verifyTaskTest returns expected response`() {
        // When
        val response = aiDevsService.verifyTaskTest()

        println(json.encodeToString(response))
        Assertions.assertEquals(0, response.code)
    }
}
