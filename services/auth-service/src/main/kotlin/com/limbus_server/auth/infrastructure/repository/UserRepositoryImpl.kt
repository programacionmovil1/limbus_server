package com.limbus_server.auth.infrastructure.repository

import com.limbus_server.auth.domain.model.User
import com.limbus_server.auth.domain.repository.UserRepository
import com.limbus_server.auth.infrastructure.database.dsl.UsersSchema
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Implementación concreta de la interfaz UserRepository usando Exposed.
// Esta clase interactúa directamente con la base de datos a través del DSL de Exposed.
// Ahora el constructor recibe la instancia de Database, que será inyectada por Koin.
class UserRepositoryImpl(private val database: Database) : UserRepository { // Acepta la instancia de Database

    // Función helper para ejecutar operaciones de base de datos en un contexto de corrutinas IO.
    // Utiliza la instancia de Database inyectada.
    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(db = database, context = Dispatchers.IO) { block() } // Usa 'database' aquí

    // Implementación del método para crear un nuevo usuario.
    override suspend fun createUser(user: User): Int = dbQuery {
        UsersSchema.insert {
            // Mapea las propiedades del modelo de dominio User a las columnas del esquema de BD.
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[name] = user.name
            // createdAt y updatedAt se establecen automáticamente por clientDefault en el esquema,
            // o se mapean si vienen en el modelo y se desea sobrescribir el default.
            // Si tu modelo User ya tiene createdAt/updatedAt y quieres usarlos:
            // it[UsersSchema.createdAt] = user.createdAt
            // it[UsersSchema.updatedAt] = user.updatedAt
            it[isEmailVerified] = user.isEmailVerified // Mapea el estado de verificación del email
            it[isAccountLocked] = user.isAccountLocked // Mapea el estado de bloqueo de la cuenta
        }[UsersSchema.id] // Devuelve el ID autoincremental del usuario insertado.
    }

    // Implementación del método para buscar un usuario por su ID.
    override suspend fun findById(id: Int): User? = dbQuery {
        UsersSchema.select(UsersSchema.columns) // Selecciona todas las columnas por defecto
            .where { UsersSchema.id eq id }
            .map(::toDomainModel) // Mapea el resultado de la fila de la BD a un modelo de dominio User.
            .singleOrNull() // Devuelve el único resultado o null si no se encuentra.
    }

    // Implementación del método para buscar un usuario por su email.
    override suspend fun findByEmail(email: String): User? = dbQuery {
        UsersSchema.select(UsersSchema.columns)
            .where { UsersSchema.email eq email }
            .map(::toDomainModel) // Mapea el resultado a un modelo de dominio User.
            .singleOrNull() // Devuelve el único resultado o null si no se encuentra.
    }

    // Implementación del método para actualizar la contraseña de un usuario.
    override suspend fun updatePassword(userId: Int, passwordHash: String) {
        dbQuery {
            UsersSchema.update({ UsersSchema.id eq userId }) { // Usa el parámetro where con la condición
                it[UsersSchema.passwordHash] = passwordHash // Establece el nuevo hash de contraseña.
                // Opcional: Actualiza la marca de tiempo de actualización.
                it[UsersSchema.updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
        }
    }

    // Implementación del método para marcar el email de un usuario como verificado.
    override suspend fun markEmailAsVerified(userId: Int) {
        dbQuery {
            // Implementa la lógica para actualizar la columna `isEmailVerified` en UsersSchema.
            UsersSchema.update({ UsersSchema.id eq userId }) { // Usa el parámetro where
                it[UsersSchema.isEmailVerified] = true // Marca el email como verificado
                it[UsersSchema.updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC) // Actualiza la marca de tiempo
            }
        }
    }

    // TODO: Implementar otros métodos definidos en la interfaz UserRepository si fuesen necesarios.
    // override suspend fun findByVerificationToken(token: String): User? = dbQuery { ... }
    // override suspend fun findByPasswordResetToken(token: String): User? = dbQuery { ... }
    // override suspend fun deleteUser(userId: Int) = dbQuery { ... }
    // override suspend fun updateUserName(userId: Int, name: String?) = dbQuery { ... }


    // Función de mapeo de un resultado de Exposed (ResultRow) a un modelo de dominio User.
    // Esta función convierte los datos de la fila de la base de datos al formato de tu modelo User.
    private fun toDomainModel(row: ResultRow): User {
        return User(
            id = row[UsersSchema.id],
            email = row[UsersSchema.email],
            passwordHash = row[UsersSchema.passwordHash],
            name = row[UsersSchema.name],
            // Mapea las marcas de tiempo. Exposed con kotlinx.datetime ya debería devolver LocalDateTime.
            createdAt = row[UsersSchema.createdAt],
            updatedAt = row[UsersSchema.updatedAt],
            isEmailVerified = row[UsersSchema.isEmailVerified], // Mapea el estado de verificación
            isAccountLocked = row[UsersSchema.isAccountLocked] // Mapea el estado de bloqueo
        )
    }
}
