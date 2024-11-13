package pl.npesystem.aidevs

import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.eclipse.microprofile.config.inject.ConfigProperty

@Singleton
class OpenAiService(
    @ConfigProperty(name = "openai.key") val openaiKey: String
) {


    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }
    val openAI = OpenAI(openaiKey)


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

}