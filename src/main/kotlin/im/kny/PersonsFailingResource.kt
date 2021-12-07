package im.kny

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/persons/fails")
class PersonsFailingResource {

    @Inject
    lateinit var txManager: TxManager

    val objectMapper: com.fasterxml.jackson.databind.ObjectMapper by lazy {
        ObjectMapper()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun postPersonFails(
        personStr: String,

        @QueryParam(value = "wait-for-job")
        @DefaultValue("true")
        waitForJob: Boolean = true
    ): Response {
        val person = objectMapper.readValue(personStr, Person::class.java)

        val (asyncJob, personsList) = txManager.autoCommitTx { dbCtx ->
            /**
             * 1. First SQL / JPA
             *    1.1. Starts the Transaction, and picks the Connection.
             *    1.2. This connection is put on ThreadLocal if I understand correctly
             */
            dbCtx.person.list()

            /**
             * 2. Starts an Async job.
             *     2.1. This job will change thread.
             *     2.1 This job should not use this method's Database Context
             */
            val asyncJob = GlobalScope.async(Dispatchers.Unconfined) {
                persistInCoRoutine(person, delayMs = 1000L)
            }

            LOG.info("pid outside: ${dbCtx.backendPID()}")
            asyncJob to dbCtx.person.list()
        }

        /**
         * 3. Wait for job and expose exception in the log.
         */
        if (waitForJob) {
            runBlocking { asyncJob.await() }
        }

        /**
         * 4. Return the response.
         *     4.1. If NOT waitForJob, this will happen.
         *     4.2. IF waitForJob; then it will fail in 3.
         */
        LOG.info("Returning person: $personsList")
        return Response
            .ok(objectMapper.valueToTree<JsonNode?>(personsList).toString())
            .build()
    }

    suspend fun persistInCoRoutine(person: Person, delayMs: Long): Person {
        txManager.autoCommitTx { dbCtx ->
            dbCtx.person.list()
            println("First: ${dbCtx.backendPID()}")
        }
        delay(delayMs)

        val persisted = txManager.autoCommitTx { dbCtx ->
            val out = dbCtx.person.persist(person)
            LOG.info("pid in subroutine: ${dbCtx.backendPID()}")
            out
        }

        LOG.info("persisted $person")

        val personFromDb = txManager.autoCommitTx { dbCtx -> dbCtx.person.byId(persisted.id) }
        LOG.info("personFromDb: $personFromDb")
        return persisted
    }

    companion object {
        private val LOG: Logger by lazy(LazyThreadSafetyMode.NONE) { LoggerFactory.getLogger(javaClass.enclosingClass) }
    }
}