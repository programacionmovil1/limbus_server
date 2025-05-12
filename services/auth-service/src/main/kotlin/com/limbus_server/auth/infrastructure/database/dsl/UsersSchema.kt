package com.limbus_server.auth.infrastructure.database.dsl

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object UsersSchema : Table("users") { // Nombre de la tabla en la base de datos
    // Define las columnas de la tabla
    // id: Clave primaria autoincremental
    val id = integer("id").autoIncrement()
    // email: Correo electrónico del usuario (VARCHAR, longitud, único, no nulo)
    val email = varchar("email", length = 255).uniqueIndex() // Usar email como identificador único
    // passwordHash: Hash de la contraseña (VARCHAR, longitud suficiente para el hash)
    val passwordHash = varchar("password_hash", length = 255) // Almacena el hash de la contraseña
    // name: Nombre del usuario (VARCHAR, longitud, opcional/nullable si no es obligatorio)
    val name = varchar("name", length = 100).nullable() // Nombre puede ser opcional

    // createdAt: Marca de tiempo de creación (usando Exposed Kotlin DateTime)
    // Convierte el Instant a LocalDateTime usando la zona horaria UTC
    val createdAt = datetime("created_at").clientDefault { Clock.System.now().toLocalDateTime(TimeZone.UTC) }

    // updatedAt: Marca de tiempo de última actualización (opcional)
    // Si necesitas establecer un valor por defecto para la actualización, también podrías usar clientDefault
    val updatedAt = datetime("updated_at").nullable()

    // Define la clave primaria de la tabla
    override val primaryKey = PrimaryKey(id)

    // Puedes añadir aquí otras columnas relevantes para la autenticación si es necesario,
    // como roles, estado de la cuenta (activo/inactivo), etc.
    // val role = varchar("role", length = 50).default("USER")
    // val isActive = bool("is_active").default(true)
}

// El resto de comentarios sobre ExposedUser, UserService y dbQuery se mantienen.
// Esta lógica debe ir en infrastructure/repository/UserRepositoryImpl.kt.
