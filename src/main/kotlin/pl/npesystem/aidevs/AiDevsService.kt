package pl.npesystem.aidevs

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import pl.npesystem.AiDevsClient
import pl.npesystem.CentralaClient
import pl.npesystem.aidevs.client.*
import java.nio.file.Files
import java.nio.file.Paths

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

    @ConfigProperty(name = "aidevs.key")
    private lateinit var aidevsKey: String
    @ConfigProperty(name = "openai.key")
    private lateinit var openaiKey: String

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

    fun processTask24() = runBlocking {
        val resource = object {}.javaClass.classLoader.getResource("task24") ?: throw RuntimeException()

        val task24Path = Paths.get(resource.toURI())
        val task24Files = Files.list(task24Path).use { stream ->
            stream.filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()
        }

        val mapFoFile = task24Files.associate { file ->
            val processFileInTask24 = openAiService.processFileInTask24(file)
//            println("File: " + file.name + "  -->  " + processFileInTask24)
            return@associate file.name to processFileInTask24
        }

        val mapKeys = mapFoFile.filter { it.value == "PEOPLE" || it.value == "HARDWARE" }
            .entries.groupBy({ it.value }) { it.key }
            .mapKeys { if (it.key == "PEOPLE") "people" else "hardware" }

        val taskAnswer = TaskAnswerMapStringListString("kategorie", aidevsKey, mapKeys)
        println(json.encodeToString(taskAnswer))
        val verifyTask = centralaClient.verifyTask(taskAnswer)
        verifyTask
    }

}