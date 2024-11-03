package pl.npesystem.aidevs.client

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

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
interface PoligonClient {

    @GET
    @Path("/dane.txt")
    @Produces("text/plain")
    fun getTaskTest(): String
}
