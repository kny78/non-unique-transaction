package im.kny

import javax.enterprise.context.ApplicationScoped
import javax.persistence.EntityManagerFactory
import javax.persistence.SynchronizationType
import javax.persistence.SynchronizationType.UNSYNCHRONIZED
import javax.sql.DataSource

@ApplicationScoped
class TxManager(
    val entityManagerFactory: EntityManagerFactory,
) {

    fun <R> autoCommitTx(block: (dbCtx: DbCtx) -> R): R {
        val entityManager = entityManagerFactory.createEntityManager(UNSYNCHRONIZED)

        return DbCtx(
            entityManager = entityManager
        ).use {
            try {
                block(it)
            } catch (e: Exception) {
                it.setRollbackOnly()
                throw e
            }
        }
    }
}
