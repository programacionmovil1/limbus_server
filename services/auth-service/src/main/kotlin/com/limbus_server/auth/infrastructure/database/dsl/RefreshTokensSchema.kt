package com.limbus_server.auth.infrastructure.database.dsl

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Define el objeto que representa la tabla de tokens de refresco en la base de datos.
// Este objeto extiende Table de Exposed y define las columnas de la tabla.
object RefreshTokensSchema : Table("refresh_tokens") { // Nombre de la tabla en la base de datos
    // Define las columnas de la tabla
    // id: Clave primaria autoincremental para la tabla de tokens.
    val id = integer("id").autoIncrement()
    // token: El valor único del token de refresco (la cadena).
    val token = varchar("token", length = 255).uniqueIndex() // El token debe ser único
    // userId: ID del usuario al que pertenece este token. Clave foránea a la tabla de usuarios.
    val userId = integer("user_id").references(UsersSchema.id, onDelete = ReferenceOption.CASCADE) // Referencia a UsersSchema
    // createdAt: Marca de tiempo de creación del token.
    val createdAt = datetime("created_at").clientDefault { Clock.System.now().toLocalDateTime(TimeZone.UTC) }
    // expiresAt: Marca de tiempo de expiración del token.
    val expiresAt = datetime("expires_at") // Este valor se calculará al crear el token
    // isUsed: Indica si el token ha sido usado (si implementas rotación de tokens).
    val isUsed = bool("is_used").default(false)

    // Define la clave primaria de la tabla.
    override val primaryKey = PrimaryKey(id)

    // Puedes añadir índices adicionales si las búsquedas por userId son frecuentes.
    // val userIdIndex = index(false, userId)
}

// Notas:
// - Este archivo solo define la estructura de la tabla de refresh tokens.
// - La lógica de creación, búsqueda, validación y eliminación de tokens
//   residirá en la implementación del repositorio (RefreshTokenRepositoryImpl).
