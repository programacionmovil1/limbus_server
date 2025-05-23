package com.limbus_server.auth.infrastructure.repository

import com.limbus_server.auth.domain.model.PasswordResetToken
import com.limbus_server.auth.domain.repository.PasswordResetTokenRepository
import com.limbus_server.auth.infrastructure.database.dsl.PasswordResetTokensSchema
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


// Implementación concreta de la interfaz PasswordResetTokenRepository usando Exposed.
// Esta clase interactúa directamente con la base de datos a través del DSL de Exposed.
class PasswordResetTokenRepositoryImpl(private val database: Database) : PasswordResetTokenRepository { // Acepta la instancia de Database

    // Función auxiliar para ejecutar operaciones de base de datos en un contexto de corrutinas IO.
    // Esto asegura que las operaciones de BD (que son inherentemente bloqueantes)
    // no bloqueen el hilo principal de Ktor. Utiliza la instancia de Database inyectada.
    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(db = database, context = Dispatchers.IO) { block() } // Usa 'database' aquí

    // Implementación del método para crear un nuevo token de restablecimiento.
    override suspend fun create(token: PasswordResetToken) {
        dbQuery {
            PasswordResetTokensSchema.insert {
                // Mapea las propiedades del modelo de dominio PasswordResetToken a las columnas del esquema.
                it[PasswordResetTokensSchema.token] = token.token
                it[PasswordResetTokensSchema.userId] = token.userId
                // createdAt se establece automáticamente por clientDefault en el esquema (si está configurado de esta manera).
                it[PasswordResetTokensSchema.expiresAt] = token.expiresAt
                it[PasswordResetTokensSchema.isUsed] = token.isUsed
            }
        }
    }

    // Implementación del método para buscar un token por su valor.
    override suspend fun findByToken(token: String): PasswordResetToken? = dbQuery {
        // Usando .where { ... } para la condición
        PasswordResetTokensSchema.select(PasswordResetTokensSchema.columns) // Selecciona todas las columnas
            .where { PasswordResetTokensSchema.token eq token } // Usa la cláusula where para la condición
            .map(::toDomainModel) // Mapea el resultado a un modelo de dominio.
            .singleOrNull() // Devuelve el único resultado o null.
    }

    // Implementación del método para buscar el token activo por ID de usuario.
    override suspend fun findActiveByUserId(userId: Int): PasswordResetToken? = dbQuery {
        // Usando .where { ... } para la condición y operadores importados
        PasswordResetTokensSchema.select(PasswordResetTokensSchema.columns) // Selecciona todas las columnas
            .where {
                // Busca el token que pertenece al usuario, no ha expirado y no ha sido usado.
                (PasswordResetTokensSchema.userId eq userId) and
                        (PasswordResetTokensSchema.expiresAt greaterEq Clock.System.now().toLocalDateTime(TimeZone.UTC)) and
                        (PasswordResetTokensSchema.isUsed eq false)
            }
            .map(::toDomainModel)
            .singleOrNull()
    }

    // Implementación del método para marcar un token como usado.
    override suspend fun markAsUsed(tokenId: Int) {
        dbQuery {
            // Usando el parámetro where con la condición
            PasswordResetTokensSchema.update({ PasswordResetTokensSchema.id eq tokenId }) { // Actualiza donde el ID del token coincide.
                it[PasswordResetTokensSchema.isUsed] = true // Marca como usado.
            }
        }
    }

    // Implementación del método para eliminar un token por su ID.
    override suspend fun deleteById(tokenId: Int) {
        dbQuery {
            // Usando el parámetro where con la condición
            PasswordResetTokensSchema.deleteWhere { PasswordResetTokensSchema.id eq tokenId } // Elimina donde el ID del token coincide.
        }
    }

    // Implementación del método para limpiar tokens expirados y usados para un usuario.
    override suspend fun cleanExpiredAndUsedTokens(userId: Int, currentTime: LocalDateTime) {
        dbQuery {
            // Usando el parámetro where con la condición y operadores importados
            PasswordResetTokensSchema.deleteWhere {
                // Elimina tokens que pertenecen al usuario Y (han expirado O han sido usados).
                (PasswordResetTokensSchema.userId eq userId) and (PasswordResetTokensSchema.expiresAt less currentTime or (PasswordResetTokensSchema.isUsed eq true))
            }
        }
    }

    // Implementación del método para eliminar todos los tokens de restablecimiento de un usuario.
    override suspend fun deleteAllByUserId(userId: Int) {
        dbQuery {
            // Usando el parámetro where con la condición
            PasswordResetTokensSchema.deleteWhere { PasswordResetTokensSchema.userId eq userId } // Elimina donde el userId coincide.
        }
    }


    // Función para mapear un ResultRow de Exposed a un modelo de dominio PasswordResetToken.
    private fun toDomainModel(row: ResultRow): PasswordResetToken {
        return PasswordResetToken(
            id = row[PasswordResetTokensSchema.id],
            token = row[PasswordResetTokensSchema.token],
            userId = row[PasswordResetTokensSchema.userId],
            createdAt = row[PasswordResetTokensSchema.createdAt],
            expiresAt = row[PasswordResetTokensSchema.expiresAt],
            isUsed = row[PasswordResetTokensSchema.isUsed]
        )
    }
}
