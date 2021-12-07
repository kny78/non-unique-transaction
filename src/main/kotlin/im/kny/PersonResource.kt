package im.kny

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/persons")
class PersonResource {

    @Inject
    lateinit var txManager: TxManager

    val objectMapper: com.fasterxml.jackson.databind.ObjectMapper by lazy {
        ObjectMapper()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun listPersons(): Response {
        val persons = txManager.autoCommitTx { dbCtx ->
            dbCtx.person.list()
        }

        return Response.ok(objectMapper.valueToTree<JsonNode>(persons).toString()).build()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun postPerson(personStr: String): Response {
        val person = objectMapper.readValue(personStr, Person::class.java)


        val person2 = txManager.autoCommitTx { dbCtx ->
            dbCtx.person.persist(person)
        }
        LOG.info("Returning person: $person2")
        return Response
            .ok(objectMapper.valueToTree<JsonNode?>(person2).toString())
            .build()
    }

    companion object {
        private val LOG: Logger by lazy(LazyThreadSafetyMode.NONE) { LoggerFactory.getLogger(javaClass.enclosingClass) }
    }
}