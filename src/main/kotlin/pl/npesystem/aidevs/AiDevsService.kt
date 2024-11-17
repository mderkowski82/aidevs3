package pl.npesystem.aidevs

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import pl.npesystem.AiDevsClient
import pl.npesystem.CentralaClient
import pl.npesystem.aidevs.client.*
import pl.npesystem.aidevs.models.ParsedUrlMarkdown
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.text.toByteArray


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

    val task24Instruction = """
        <Cel>
        Analiza danych tekstowych i klasyfikacja fragmentów informacji na trzy kategorie:
        </Cel>
        <DANE>
        PEOPLE – raporty o schwytanych osobach lub śladach ich obecności.
        HARDWARE – raporty o naprawionych usterkach sprzętowych (nie związanych z oprogramowaniem).
        OTHER – wszystko, co nie pasuje do powyższych kategorii, w tym katalog faktów, raporty techniczne niezwiązane z ludźmi i sprzętem.
        </DANE>
        <Instrukcja>
        Otrzymasz różne wiadomości użytkownika. Twoim zadaniem jest:
        Analiza każdego fragmentu tekstu.
        Klasyfikacja fragmentu jako PERSON, HARDWARE lub OTHER.
        NIE UWZGLĘDNIAJ informacji o personelu i rutynowych czynnościach"
        Zwrócenie wyniku tylko w postaci KATEGORII.
        </Instrukcja>
    """.trimIndent()

    val task25Instruction = """
        <Cel>
        utwórz szczegółowe podsumowanie
        </Cel>
        <Instrukcja>
        Otrzymasz różne wiadomości . Twoim zadaniem jest:
        Analiza każdego fragmentu tekstu.
        Zwrócenie szczegółowych informacji o zawartości.
        Weź pod uwagę opisy pod plikami, są bardzo ważne.
        </Instrukcja>
    """.trimIndent()


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

    fun getTask25Question(): String {
        return centralaClient.getTask25Question(aidevsKey)
    }

    fun processTask24() = runBlocking {
        val resource = object {}.javaClass.classLoader.getResource("task24") ?: throw RuntimeException()

        val task24Path = Paths.get(resource.toURI())
        val task24Files = Files.list(task24Path).use { stream ->
            stream.filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()
        }

        val mapFoFile = task24Files.associate { file ->
            val processFileInTask24 = openAiService.processFileWithAi(file, task24Instruction, "")
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

    fun extractLinks(markdown: String): List<ParsedUrlMarkdown> {
        val regex = Regex("""(!?\[(.*?)])\((https?://.+?)\)""")
        val extensionRegex = Regex("""\.(\w+)(\?.*)?$""")

        return regex.findAll(markdown).map { match ->
            val url = match.groupValues[3]
            val extensionMatch = extensionRegex.find(url)
            val extension = extensionMatch?.groupValues?.get(1) ?: "Brak rozszerzenia"

            ParsedUrlMarkdown(
                match.groupValues[2],  // Tekst w nawiasach kwadratowych
                url,                           // URL
                match.value,             // Pełne dopasowanie
                extension,               // Rozszerzenie pliku
                "" // Opcjonalne parametry
            )
        }.toList()
    }

    fun processTask25(task25Question: String): TaskResponse {

        val markdown = getMarkdownTask25()

        val task25Dir =
            Paths.get(object {}.javaClass.classLoader.getResource("task25")?.toURI() ?: throw RuntimeException())
        val markdownFilePath = task25Dir.resolve("Markdown.md")


        Files.write(markdownFilePath, markdown.toByteArray())
        println("Created file at: $markdownFilePath")

        val questions = createMapFromString(task25Question)

        val answers = questions.map {
            it.key to openAiService.message(
                it.value,
"""
ODPOWIADAJ NA PYTANIA UŻYTKOWNIKA JEDNYM ZDANIEM ZGODNIE Z CONTEXTEM

<CONTEXT>
$markdown
</CONTEXT>
""".trimIndent()
            )
        }.toMap()

        println(Json.encodeToString(answers))

        val taskAnswer = TaskAnswerMapStringString("arxiv", aidevsKey, answers)
        println(json.encodeToString(taskAnswer))
        val verifyTask = centralaClient.verifyTask(taskAnswer)
        return TaskResponse(0, Json.encodeToString(verifyTask))
    }

    fun createMapFromString(input: String): Map<String, String> {
        return input.lines()
            .filter { it.contains("=") }.associate { line ->
                val (key, value) = line.split("=", limit = 2)
                key to value
            }
    }

    private fun getMarkdownTask25(): String {
        val resource = object {}.javaClass.classLoader.getResource("task25") ?: throw RuntimeException()

        val task25Path = Paths.get(resource.toURI())
        val files = Files.list(task25Path).use { stream ->
            stream.filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()
        }
        if(files.isEmpty()) {
            var markdown =  runBlocking {
                val client = HttpClient()
                val responseText =
                    client.get("https://webink.app/api/markdown?url=https://centrala.ag3nts.org/dane/arxiv-draft.html")
                        .bodyAsText()
                return@runBlocking responseText
            }
            val extractLinks = extractLinks(markdown)
            val client = HttpClient()
            extractLinks.forEach { parsedUrlMarkdown ->
                val url = parsedUrlMarkdown.url
                val extension = parsedUrlMarkdown.extension
                runBlocking {
                    client.get(url).body<ByteArray>().also {
                        val tempFile = Files.createTempFile("download_" + Date().time, ".${extension}").toFile().apply {
                            deleteOnExit() // Ensure the file is deleted when the JVM exits
                        }
                        tempFile.writeBytes(it)
                        parsedUrlMarkdown.content = openAiService.processFileWithAi(tempFile, "Podsumuj", parsedUrlMarkdown.info)
                    }
                }
            }

            extractLinks.forEach {
                println(it.complete)
                markdown = markdown.replace(it.complete, """
${it.complete}
### START FILE CONTENT: ${it.url} ###
${it.content}
### END FILE CONTENT: ${it.url} ###
            """.trimIndent(), true)
            }
            return markdown
        } else {
            return files.first().readText()
        }
    }

    fun testImage(): String {
        return ""
    }

}