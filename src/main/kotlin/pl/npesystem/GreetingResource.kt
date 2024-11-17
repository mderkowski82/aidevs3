package pl.npesystem

import com.aallam.openai.api.model.Model
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import kotlinx.serialization.json.Json
import pl.npesystem.aidevs.AiDevsService
import pl.npesystem.aidevs.TaskService

@Path("/aidevs")
class GreetingResource(private val aiDevsService: AiDevsService, private val taskService: TaskService) {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    fun hello() = "Hello from Quarkus REST"

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/task23")
    fun task23() = taskService.task23()

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/task24")
    fun task24() = taskService.task24()

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/task25")
    fun task25() = taskService.task25()

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/models")
    fun getModels(): List<Model> {
        return taskService.listModels()
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/testImage")
    fun testImage() = taskService.testImage()

}