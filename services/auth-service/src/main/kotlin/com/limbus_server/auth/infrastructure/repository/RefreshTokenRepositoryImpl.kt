package com.limbus_server.auth.infrastructure.repository

import com.limbus_server.auth.domain.model.RefreshToken
import com.limbus_server.auth.domain.repository.RefreshTokenRepository
import com.limbus_server.auth.infrastructure.database.dsl.RefreshTokensSchema
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDateTime

// Implementación concreta de la interfaz RefreshTokenRepository usando Exposed.
// Esta clase interactúa directamente con la base de datos a través del DSL de Exposed.
// Ahora el constructor recibe la instancia de Database, que será inyectada por Koin.
class RefreshTokenRepositoryImpl(private val database: Database) : RefreshTokenRepository { // Acepta la instancia de Database

    // Función helper para ejecutar operaciones de base de datos en un contexto de corrutinas IO.
    // Utiliza la instancia de Database inyectada.
    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(db = database, context = Dispatchers.IO) { block() } // Usa 'database' aquí

    // Implementación del método para crear un nuevo token de refresco.
    override suspend fun create(token: RefreshToken) {
        dbQuery {
            RefreshTokensSchema.insert {
                // Mapea las propiedades del modelo de dominio RefreshToken a las columnas del esquema.
                it[RefreshTokensSchema.token] = token.token
                it[RefreshTokensSchema.userId] = token.userId
                // createdAt se establece automáticamente por clientDefault en el esquema (si está configurado así).
                it[RefreshTokensSchema.expiresAt] = token.expiresAt
                it[RefreshTokensSchema.isUsed] = token.isUsed
            }
        }
    }

    // Implementación del método para buscar un token por su valor.
    override suspend fun findByToken(token: String): RefreshToken? = dbQuery {
        // Using .where { ... } for the condition
        RefreshTokensSchema.select(RefreshTokensSchema.columns) // Selects all columns
            .where { RefreshTokensSchema.token eq token } // Uses the where clause for the condition
            .map(::toDomainModel) // Maps the result to a RefreshToken domain model.
            .singleOrNull() // Returns the single result or null.
    }

    // Implementación del método para buscar tokens por ID de usuario.
    override suspend fun findByUserId(userId: Int): List<RefreshToken> = dbQuery {
        // Using .where { ... } for the condition
        RefreshTokensSchema.select(RefreshTokensSchema.columns) // Selects all columns
            .where { RefreshTokensSchema.userId eq userId } // Uses the where clause for the condition
            .map(::toDomainModel) // Maps the results to a list of models.
            .toList() // Ensures a list is returned
    }

    // Implementación del método para marcar un token como usado.
    override suspend fun markAsUsed(tokenId: Int) {
        dbQuery {
            // Using the where parameter with the condition
            RefreshTokensSchema.update({ RefreshTokensSchema.id eq tokenId }) { // Updates where the token ID matches.
                it[RefreshTokensSchema.isUsed] = true // Marks as used.
            }
        }
    }

    // Implementación del método para eliminar un token por su ID.
    override suspend fun deleteById(tokenId: Int) {
        dbQuery {
            // Using the where parameter with the condition
            RefreshTokensSchema.deleteWhere { RefreshTokensSchema.id eq tokenId } // Deletes where the token ID matches.
        }
    }

    // Implementación del método para limpiar tokens expirados y usados para un usuario.
    override suspend fun cleanExpiredAndUsedTokens(userId: Int, currentTime: LocalDateTime) {
        dbQuery {
            // Using the where parameter with the condition and imported operators
            RefreshTokensSchema.deleteWhere {
                // Deletes tokens that belong to the user AND (have expired OR have been used).
                (RefreshTokensSchema.userId eq userId) and
                        (RefreshTokensSchema.expiresAt less currentTime or (RefreshTokensSchema.isUsed eq true))
            }
        }
    }

    // Implementación del método para eliminar todos los tokens de refresco de un usuario.
    override suspend fun deleteAllByUserId(userId: Int) {
        dbQuery {
            // Using the where parameter with the condition
            RefreshTokensSchema.deleteWhere { RefreshTokensSchema.userId eq userId } // Deletes where the userId matches.
        }
    }

    // Función de mapeo de un resultado de Exposed a un modelo de dominio RefreshToken.
    private fun toDomainModel(row: ResultRow): RefreshToken {
        return RefreshToken(
            id = row[RefreshTokensSchema.id],
            token = row[RefreshTokensSchema.token],
            userId = row[RefreshTokensSchema.userId],
            createdAt = row[RefreshTokensSchema.createdAt],
            expiresAt = row[RefreshTokensSchema.expiresAt],
            isUsed = row[RefreshTokensSchema.isUsed]
        )
    }
}
