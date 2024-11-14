import jakarta.ws.rs.ext.Provider
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.ClientErrorException
import org.jboss.resteasy.reactive.ClientWebApplicationException

@Provider
class CustomErrorHandler : ExceptionMapper<WebApplicationException> {
    override fun toResponse(exception: WebApplicationException): Response {
        val cause = exception.cause
        if (cause is WebApplicationException && cause.response.status == 400) {
            return Response
                .status(cause.response.status)
                .entity(cause.response.readEntity(String::class.java))   // Read the entity as String
                .build()
        }
        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("An unexpected error occurred")
            .build()
    }
}