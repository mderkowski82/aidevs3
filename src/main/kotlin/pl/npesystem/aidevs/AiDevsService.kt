package pl.npesystem.aidevs

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import pl.npesystem.AiDevsClient
import pl.npesystem.CentralaClient
import pl.npesystem.aidevs.client.*

@Singleton
class AiDevsService {

    @Inject
    @RestClient
    private lateinit var aiDevsClient: AiDevsClient

    @Inject
    @RestClient
    private lateinit var poligonClient: PoligonClient

    @Inject
    @RestClient
    private lateinit var centralaClient: CentralaClient

    @Inject
    private lateinit var openAiService: OpenAiService

    @ConfigProperty(name = "aidevs.key") private lateinit var aidevsKey: String
    @ConfigProperty(name = "openai.key") private lateinit var openaiKey: String

    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    fun verifyTaskTest(): TaskResponse {
        val taskTest = poligonClient.getTaskTest()
        val splitedData = taskTest.split("\n").filter { it.isNotBlank() }

        val taskAnswer = TaskAnswerStringList("POLIGON", aidevsKey, splitedData)
        json.encodeToString(taskAnswer).also { println(it) }

        return aiDevsClient.verifyTask(taskAnswer)
    }

    fun getRobotDescription(): DescriptionResponse {
        return centralaClient.getRobotDescription(aidevsKey)
    }



}