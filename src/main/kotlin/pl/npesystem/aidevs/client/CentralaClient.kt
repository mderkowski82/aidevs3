package pl.npesystem

import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import org.jboss.resteasy.reactive.RestPath
import pl.npesystem.aidevs.client.DescriptionResponse
import pl.npesystem.aidevs.client.TaskAnswer
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
@RegisterRestClient(baseUri = "https://centrala.ag3nts.org")
interface CentralaClient {

    @GET
    @Path("/data/{apiKey}/robotid.json")
    @Produces("application/json")
    fun getRobotDescription(@RestPath apiKey: String): DescriptionResponse

    @POST
    @Path("/verify")
    @Produces("application/json")
    fun verifyTask(taskAnswer: TaskAnswer): TaskResponse

}
