package pl.npesystem.aidevs

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import pl.npesystem.AiDevsClient
import pl.npesystem.aidevs.client.PoligonClient
import pl.npesystem.aidevs.client.TaskAnswerAsListString
import pl.npesystem.aidevs.client.TaskResponse

@Singleton
class AiDevsService() {

    @Inject
    @RestClient
    lateinit var aiDevsClient: AiDevsClient

    @Inject
    @RestClient
    lateinit var poligonClient: PoligonClient

    @ConfigProperty(name = "aidevs.key") lateinit var apiKey: String

    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    fun verifyTaskTest(): TaskResponse {
        val taskTest = poligonClient.getTaskTest()
        val splitedData = taskTest.split("\n").filter { it.isNotBlank() }

        val taskAnswer = TaskAnswerAsListString("POLIGON", apiKey, splitedData)
        json.encodeToString(taskAnswer).also { println(it) }

        return aiDevsClient.verifyTask(taskAnswer)
    }

}