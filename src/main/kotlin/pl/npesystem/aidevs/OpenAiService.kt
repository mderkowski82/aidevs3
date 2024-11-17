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

    val json = Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true }
    val openAI = OpenAI(openaiKey, logging = LoggingConfig(LogLevel.None))

    fun generateImage(description: String): String = runBlocking {
        val imageRequest = ImageCreation(
            prompt = description,
            model = ModelId("dall-e-3"),
            n = 1,
            size = ImageSize.is1024x1024
        )
        val images = openAI.imageURL(imageRequest)
        images[0].url
    }

    private fun createChatCompletionRequest(model: String, systemPrompt: String, userPrompt: String) = ChatCompletionRequest(
        model = ModelId(model),
        messages = listOf(
            ChatMessage(role = ChatRole.System, content = systemPrompt),
            ChatMessage(role = ChatRole.User, content = userPrompt)
        )
    )

    fun message(prompt: String, system: String): String = runBlocking {
        val chatRequest = createChatCompletionRequest("gpt-4o", system, prompt)
        val chatCompletion = openAI.chatCompletion(chatRequest)
        chatCompletion.choices.last().message.content ?: ""
    }

    fun messageImage(prompt: String, system: String, files: List<File>): String = runBlocking {
        val reqList: ArrayList<ContentPart> = ArrayList<ContentPart>().apply {
            add(TextPart(prompt))
            files.map {
                val fileAsBase64 = Base64.getEncoder().encodeToString(it.readBytes())
                add(ImagePart("data:image/${it.extension};base64,$fileAsBase64"))
            }
        }
        val chatRequest = createChatCompletionRequest("gpt-4o", system, reqList.joinToString())
        val chatCompletion = openAI.chatCompletion(chatRequest)
        chatCompletion.choices.last().message.content ?: ""
    }

    private fun transcribe(file: File) = runBlocking {
        val transcriptionRequest = TranscriptionRequest(
            audio = FileSource(Path(file.path)),
            model = ModelId("whisper-1"),
            language = "en"
        )
        openAI.transcription(transcriptionRequest).text
    }

    fun processFileWithAi(file: File, instruction: String, additionalInfo: String): String {
        return when (file.extension.lowercase(Locale.getDefault())) {
            "txt" -> processTextFile(file, instruction, additionalInfo)
            "jpg", "jpeg", "png" -> processImageFile(file, instruction)
            "mp3" -> processAudioFile(file, instruction)
            else -> handleUnknownFileType(file)
        }
    }

    private fun processTextFile(file: File, instruction: String, additionalInfo: String): String {
        val dataToAnalyze = """
            <DANE_DO_ANALIZY>
            |${file.readText()}
            </DANE_DO_ANALIZY>
            |DODATKOWE INFORMACJE:
            |$additionalInfo
        """.trimMargin()
        val analysisMessage = createMessageForAnalysis(instruction, dataToAnalyze)
        return logProcessing("plik tekstowy", file.name, analysisMessage)
    }

    private fun processImageFile(file: File, instruction: String): String {
        val imageAnalysisPrompt = """
            Przeanalizuj zdjęcie użytkownika.
            Opisz dokładnie, co na nim jest i czego dotyczy.
            Jeśli to zdjęcie przedstawiające miejsce, opisz je. Nie zwracaj miasta, tylko opis.
        """.trimIndent()
        val imageAnalysis = messageImage(imageAnalysisPrompt, "", listOf(file))
        val analysisMessage = createMessageForAnalysis(imageAnalysis, instruction)
        return logProcessing("plik graficzny", file.name, analysisMessage)
    }

    private fun processAudioFile(file: File, instruction: String): String {
        val transcribedText = transcribe(file)
        val analysisMessage = createMessageForAnalysis(instruction, transcribedText)
        return logProcessing("plik audio", file.name, analysisMessage)
    }

    private fun handleUnknownFileType(file: File): String {
        println("Nieznany typ pliku: ${file.name}")
        return file.name
    }

    private fun createMessageForAnalysis(system: String, user: String): String = message(system, user)

    private fun logProcessing(fileType: String, fileName: String, message: String): String {
        println("Przetwarzanie $fileType: $fileName -> $message")
        return message
    }

    fun getModels(): List<Model> = runBlocking { openAI.models() }
}