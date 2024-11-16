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
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.File
import java.util.*

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

    private fun messageImage(prompt: String, system: String, files: List<File>): String {

        val reqList: ArrayList<ContentPart> = ArrayList<ContentPart>()
        reqList.add(TextPart(prompt))

        files.map { file ->
            FileSystem.SYSTEM.read(file.path.toPath()) {
                val fileAsBase64 = Base64.getEncoder().encodeToString(file.readBytes())
                reqList.add(ImagePart("data:image/${file.extension};base64,$fileAsBase64"))
            }
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

    private fun message(prompt: String, system: String): String {

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
        val request = TranscriptionRequest(
            audio = FileSource(file.name, FileSystem.SYSTEM.source(file.path.toPath())),
            model = ModelId("whisper-1"),
            language = "en"
        )
        val transcription = openAI.transcription(request)
        transcription.text
    }

    fun processFileInTask24(it: File): String {
        return when (it.extension.lowercase(Locale.getDefault())) {
            "txt" -> {
                val message = message(
                    """
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
                """.trimIndent(),
                    """<DANE_DO_ANALIZY>
                     ${it.readText()}
                     </DANE_DO_ANALIZY>""".trimMargin()
                )
                println("Processing text file: ${it.name} -> $message")
//                println(it.readText())
                message
            }

            "jpg", "jpeg", "png" -> {
                val messageImage = messageImage(
                    """
                    Przeanalizuj zdjęcie użytkownika.
                    Opisz dokładnie co na nim jest i czego dotyczy.
                """.trimIndent(),
                    "",
                    listOf(it)
                )
//                println("Processing image file: ${it.name} -> $messageImage")
                val message = message(
                    messageImage, """
<Cel>
Analiza danych tekstowych i klasyfikacja fragmentów informacji na trzy kategorie:
</Cel>
<DANE>
PEOPLE – raporty o schwytanych osobach lub śladach ich obecności.
HARDWARE – raporty o naprawionych usterkach sprzętowych (nie związanych z oprogramowaniem).
OTHER – wszystko, co nie pasuje do powyższych kategorii, w tym katalog faktów, raporty techniczne niezwiązane z ludźmi i sprzętem.
</DANE>
<Instrukcja>
Otrzymasz różne raporty w formacie tekstowym. Twoim zadaniem jest:

Analiza każdego fragmentu tekstu.
Klasyfikacja fragmentu jako PERSON, HARDWARE lub OTHER.
NIE UWZGLĘDNIAJ informacji o personelu i rutynowych czynnościach"
Zwrócenie wyniku tylko w postaci KATEGORII.
</Instrukcja>
                """.trimIndent()
                )
                println("Processing image file: ${it.name} -> $message")
                message
            }

            "mp3" -> {
                val transcribe = transcribe(it)
                val message = message(
                    """
<Cel>
Analiza danych tekstowych i klasyfikacja fragmentów informacji na trzy kategorie:
</Cel>
<DANE>
PEOPLE – raporty o schwytanych osobach lub śladach ich obecności.
HARDWARE – raporty o naprawionych usterkach sprzętowych (nie związanych z oprogramowaniem).
OTHER – wszystko, co nie pasuje do powyższych kategorii, w tym katalog faktów, raporty techniczne niezwiązane z ludźmi i sprzętem.
</DANE>
<Instrukcja>
Otrzymasz różne raporty w formacie tekstowym. Twoim zadaniem jest:

Analiza każdego fragmentu tekstu.
Klasyfikacja fragmentu jako PERSON, HARDWARE lub OTHER.
NIE UWZGLĘDNIAJ informacji o personelu i rutynowych czynnościach"
Zwrócenie wyniku tylko w postaci KATEGORII.
</Instrukcja>
                """.trimIndent(),
                    transcribe
                )
                println("Processing audio file: ${it.name} -> $message")
//                println(transcribe)
                message
              }

            else -> {
                // Process unknown file type
                println("Unknown file type: ${it.name}")
                // Add your processing logic here
                it.name
            }
        }
    }

    fun getModels(): List<Model> = runBlocking { openAI.models() }


}