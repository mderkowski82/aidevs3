package pl.npesystem

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
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
}