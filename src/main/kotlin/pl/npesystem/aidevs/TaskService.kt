package pl.npesystem.aidevs

import com.aallam.openai.api.model.Model
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import pl.npesystem.CentralaClient
import pl.npesystem.aidevs.client.TaskAnswerString
import pl.npesystem.aidevs.client.TaskResponse

@Singleton
class TaskService {

    @Inject
    lateinit var openAiService: OpenAiService

    @Inject
    lateinit var aiDevsService: AiDevsService

    @Inject
    @RestClient
    lateinit var centralaClient: CentralaClient

    @ConfigProperty(name = "aidevs.key") private lateinit var aidevsKey: String
    @ConfigProperty(name = "openai.key") private lateinit var openaiKey: String

    fun task23(): TaskResponse {
        val description = aiDevsService.getRobotDescription().description
        println(description)
        val generateImageUrl = openAiService.generateImage(description)
        val taskAnswerString = TaskAnswerString("robotid", aidevsKey, generateImageUrl)
        val verifyTask = centralaClient.verifyTask(taskAnswerString)
        return verifyTask
    }

    fun task24(): TaskResponse {
        val processTask24 = aiDevsService.processTask24()
        println(processTask24)
        return processTask24
    }

    fun listModels(): List<Model> {
        return openAiService.getModels()
    }

    fun task25(): TaskResponse {
        val task25Question = aiDevsService.getTask25Question()

        val processTask25 = aiDevsService.processTask25(task25Question)

        return processTask25
    }

    fun testImage(): String {
        return aiDevsService.testImage()
    }
}