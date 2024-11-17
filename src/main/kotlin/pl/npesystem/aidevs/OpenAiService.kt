package pl.npesystem.aidevs

import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


@Singleton
class OpenAiService(
    @ConfigProperty(name = "openai.key") val openaiKey: String
) {


    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }
    val openAI = OpenAI(openaiKey, logging = LoggingConfig(LogLevel.None))


    fun generateImage(description: String): String {
        return runBlocking {
            val images = openAI.imageURL( // or openAI.imageJSON
                creation = ImageCreation(
                    prompt = description,
                    model = ModelId("dall-e-3"),
                    n = 1,
                    size = ImageSize.is1024x1024
                )
            )
            return@runBlocking images[0].url
        }
    }

    fun messageImage(prompt: String, system: String, files: List<File>): String {

        val reqList: ArrayList<ContentPart> = ArrayList<ContentPart>()
        reqList.add(TextPart(prompt))

        files.map {
            val fileAsBase64 = Base64.getEncoder().encodeToString(it.readBytes())
            reqList.add(ImagePart("data:image/${it.extension};base64,$fileAsBase64"))
        }

        return runBlocking {
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4o"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = system
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = reqList,
                    )
                )
            )
            val chatCompletion = openAI.chatCompletion(chatCompletionRequest)
            return@runBlocking chatCompletion.choices.last().message.content ?: ""
        }
    }

    fun message(prompt: String, system: String): String {

        return runBlocking {
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4o"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = system
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = prompt,
                    )
                )
            )
            val chatCompletion = openAI.chatCompletion(chatCompletionRequest)
            return@runBlocking chatCompletion.choices.last().message.content ?: ""
        }
    }

    private fun transcribe(file: File) = runBlocking {
        val toPath = Path(file.path)
        val request = TranscriptionRequest(
            audio = FileSource(toPath),
            model = ModelId("whisper-1"),
            language = "en"
        )
        val transcription = openAI.transcription(request)
        transcription.text
    }

    fun processFileWithAi(it: File, instruction: String, additionalInfo: String): String {

        return when (it.extension.lowercase(Locale.getDefault())) {
            "txt" -> {
                val dataToAnalyze = """<DANE_DO_ANALIZY>
                    |${it.readText()}
                    |</DANE_DO_ANALIZY>
                    |DODATKOWE INFORMACJE:
                    |$additionalInfo
                    |""".trimMargin()
                val message = createMessageForAnalysis(instruction, dataToAnalyze)
                logProcessing("text file", it.name, message)
            }
            "jpg", "jpeg", "png" -> {
                val messageImage = messageImage(
                    """
                Przeanalizuj zdjęcie użytkownika.
                Opisz dokładnie co na nim jest i czego dotyczy.
                Jeśli to zdjęcie przedstawiające miejsce opisz je. nie zwracaj miasta tylko opis.
            """.trimIndent(), "", listOf(it)
                )
                val message = createMessageForAnalysis(messageImage, instruction)
                logProcessing("image file", it.name, message)
            }
            "mp3" -> {
                val transcribedText = transcribe(it)
                val message = createMessageForAnalysis(instruction, transcribedText)
                logProcessing("audio file", it.name, message)
            }
            else -> {
                println("Unknown file type: ${it.name}")
                it.name
            }
        }
    }

    private fun createMessageForAnalysis(system: String, user: String): String {
        return message(system, user)
    }

    private fun logProcessing(fileType: String, fileName: String, message: String): String {
        println("Processing $fileType: $fileName -> $message")
        return message
    }

    fun getModels(): List<Model> = runBlocking { openAI.models() }


}