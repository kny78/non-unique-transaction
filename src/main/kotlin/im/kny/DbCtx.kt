package im.kny

import org.hibernate.internal.SessionImpl
import org.postgresql.jdbc.PgConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.persistence.EntityManager
import javax.persistence.EntityTransaction

class DbCtx(val entityManager: EntityManager) : AutoCloseable {


    val transaction: EntityTransaction// = em.transaction


    init {



        transaction = entityManager.transaction
        transaction.begin()
    }

    val person: PersonDao by lazy { PersonDao(entityManager) }

    override fun close() {
        when {
            transaction == null -> LOG.debug("Transaction is already closed.")
            !transaction.rollbackOnly -> transaction.commit()
            else -> {
                val msg = "Transaction is not committed or rolled back. Rolling back!"
                LOG.warn(msg)
                LOG.trace(msg, RuntimeException("Stacktrace"))
                transaction.rollback()
            }
        }
        if (this.entityManager.isOpen) this.entityManager.close()
    }

    fun setRollbackOnly() {
        transaction.setRollbackOnly()
    }


    fun backendPID(): Int {
        val pgConn = entityManager
            .unwrap(SessionImpl::class.java)
            .connection()
            .unwrap(PgConnection::class.java)

        val backendPID = pgConn.backendPID
        return backendPID
    }

    companion object {
        private val LOG: Logger by lazy(LazyThreadSafetyMode.NONE) { LoggerFactory.getLogger(javaClass.enclosingClass) }
    }
}