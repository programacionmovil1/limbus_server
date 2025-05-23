package com.limbus_server.auth.infrastructure.repository

import com.limbus_server.auth.domain.model.VerificationToken
import com.limbus_server.auth.domain.repository.VerificationTokenRepository
import com.limbus_server.auth.infrastructure.database.dsl.VerificationTokensSchema
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Implementación concreta de la interfaz VerificationTokenRepository usando Exposed.
// Esta clase interactúa directamente con la base de datos a través del DSL de Exposed.
class VerificationTokenRepositoryImpl(private val database: Database) : VerificationTokenRepository { // Acepta la instancia de Database

    // Función auxiliar para ejecutar operaciones de base de datos en un contexto de corrutinas IO.
    // Esto asegura que las operaciones de BD (que son inherentemente bloqueantes)
    // no bloqueen el hilo principal de Ktor. Utiliza la instancia de Database inyectada.
    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(db = database, context = Dispatchers.IO) { block() } // Usa 'database' aquí

    // Implementación del método para crear un nuevo token de verificación.
    override suspend fun create(token: VerificationToken) {
        dbQuery {
            VerificationTokensSchema.insert {
                // Mapea las propiedades del modelo de dominio VerificationToken a las columnas del esquema.
                it[VerificationTokensSchema.token] = token.token
                it[VerificationTokensSchema.userId] = token.userId
                // createdAt se establece automáticamente por clientDefault en el esquema (si está configurado de esta manera).
                it[VerificationTokensSchema.expiresAt] = token.expiresAt
                it[VerificationTokensSchema.isUsed] = token.isUsed
            }
        }
    }

    // Implementación del método para buscar un token por su valor.
    override suspend fun findByToken(token: String): VerificationToken? = dbQuery {
        // Usando .where { ... } para la condición
        VerificationTokensSchema.select(VerificationTokensSchema.columns) // Selecciona todas las columnas
            .where { VerificationTokensSchema.token eq token } // Usa la cláusula where para la condición
            .map(::toDomainModel) // Mapea el resultado a un modelo de dominio.
            .singleOrNull() // Devuelve el único resultado o null.
    }

    // Implementación del método para buscar el token activo por ID de usuario.
    override suspend fun findActiveByUserId(userId: Int): VerificationToken? = dbQuery {
        // Usando .where { ... } para la condición y operadores importados
        VerificationTokensSchema.select(VerificationTokensSchema.columns) // Selecciona todas las columnas
            .where {
                // Busca el token que pertenece al usuario, no ha expirado y no ha sido usado.
                (VerificationTokensSchema.userId eq userId) and
                        (VerificationTokensSchema.expiresAt greaterEq Clock.System.now().toLocalDateTime(TimeZone.UTC)) and
                        (VerificationTokensSchema.isUsed eq false)
            }
            .map(::toDomainModel)
            .singleOrNull()
    }

    // Implementación del método para marcar un token como usado.
    override suspend fun markAsUsed(tokenId: Int) {
        dbQuery {
            // Usando el parámetro where con la condición
            VerificationTokensSchema.update({ VerificationTokensSchema.id eq tokenId }) { // Actualiza donde el ID del token coincide.
                it[VerificationTokensSchema.isUsed] = true // Marca como usado.
            }
        }
    }

    // Implementación del método para eliminar un token por su ID.
    override suspend fun deleteById(tokenId: Int) {
        dbQuery {
            // Usando el parámetro where con la condición
            VerificationTokensSchema.deleteWhere { VerificationTokensSchema.id eq tokenId } // Elimina donde el ID del token coincide.
        }
    }

    // Implementación del método para limpiar tokens expirados y usados para un usuario.
    override suspend fun cleanExpiredAndUsedTokens(userId: Int, currentTime: LocalDateTime) {
        dbQuery {
            // Usando el parámetro where con la condición y operadores importados
            VerificationTokensSchema.deleteWhere {
                // Elimina tokens que pertenecen al usuario Y (han expirado O han sido usados).
                (VerificationTokensSchema.userId eq userId) and (VerificationTokensSchema.expiresAt less currentTime or (VerificationTokensSchema.isUsed eq true))
            }
        }
    }

    // Implementación del método para eliminar todos los tokens de verificación de un usuario.
    override suspend fun deleteAllByUserId(userId: Int) {
        dbQuery {
            // Usando el parámetro where con la condición
            VerificationTokensSchema.deleteWhere { VerificationTokensSchema.userId eq userId } // Elimina donde el userId coincide.
        }
    }

    // Función para mapear un ResultRow de Exposed a un modelo de dominio VerificationToken.
    private fun toDomainModel(row: ResultRow): VerificationToken {
        return VerificationToken(
            id = row[VerificationTokensSchema.id],
            token = row[VerificationTokensSchema.token],
            userId = row[VerificationTokensSchema.userId],
            createdAt = row[VerificationTokensSchema.createdAt],
            expiresAt = row[VerificationTokensSchema.expiresAt],
            isUsed = row[VerificationTokensSchema.isUsed]
        )
    }
}
