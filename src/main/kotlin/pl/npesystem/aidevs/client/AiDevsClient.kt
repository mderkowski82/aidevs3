package pl.npesystem

import jakarta.ws.rs.GET
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import pl.npesystem.aidevs.client.TaskAnswerAsListString
import pl.npesystem.aidevs.client.TaskResponse

/**
 * To use it via injection.
 *
 * ```kotlin
 *     @Inject
 *     @RestClient
 *     lateinit var myRemoteService: MyRemoteService
 *
 *     fun doSomething() {
 *         val restClientExtensions = myRemoteService.getExtensionsById("io.quarkus:quarkus-rest-client")
 *     }
 * ```
 */
@RegisterRestClient(baseUri = "https://poligon.aidevs.pl")
interface AiDevsClient {

    @POST
    @Path("/verify")
    @Produces("application/json")
    fun verifyTask(task: TaskAnswerAsListString): TaskResponse

}
