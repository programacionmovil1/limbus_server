package com.limbus_server.auth.infrastructure.database

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.limbus_server.auth.infrastructure.database.dsl.UsersSchema
import com.limbus_server.auth.infrastructure.database.dsl.RefreshTokensSchema
import com.limbus_server.auth.infrastructure.database.dsl.VerificationTokensSchema
import com.limbus_server.auth.infrastructure.database.dsl.PasswordResetTokensSchema
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

// Esta función configura e inicializa la conexión a la base de datos y crea las tablas.
fun Application.configureDatabases(): Database { // Devuelve database
    // Obtiene la configuración de la base de datos desde application.yaml
    val dbConfig = environment.config.config("database") // Asume que la configuración de BD está bajo 'database'

    // Configura HikariCP para gestionar el pool de conexiones
    val hikariConfig = HikariConfig().apply {
        driverClassName = dbConfig.property("driverClassName").getString()
        jdbcUrl = dbConfig.property("jdbcUrl").getString()
        username = dbConfig.property("user").getString()
        password = dbConfig.property("password").getString()
        maximumPoolSize = dbConfig.propertyOrNull("maxPoolSize")?.getString()?.toInt() ?: 10 // Tamaño del pool (default 10)
        isAutoCommit = false // Deshabilita auto-commit para usar transacciones explícitas con Exposed
        transactionIsolation = "TRANSACTION_REPEATABLE_READ" // Nivel de aislamiento recomendado
        validate() // Valida la configuración
    }

    // Crea el DataSource usando HikariCP
    val dataSource = HikariDataSource(hikariConfig)

    // Conecta Exposed al DataSource
    val database = Database.connect(dataSource)

    // Inicializa el esquema de la base de datos (crea tablas si no existen)
    // Se ejecuta dentro de una transacción para asegurar atomicidad.
    transaction(database) {
        // Crea la tabla UsersSchema si no existe
        SchemaUtils.create(UsersSchema)
        // Crea las tablas para los diferentes tipos de tokens si no existen
        SchemaUtils.create(RefreshTokensSchema)
        SchemaUtils.create(VerificationTokensSchema)
        SchemaUtils.create(PasswordResetTokensSchema)

        // Puedes añadir aquí lógica de inicialización de datos si es necesario,
        // como la creación de un usuario administrador inicial.
        // if (UsersSchema.selectAll().count() == 0L) {
        //     UsersSchema.insert {
        //         it[email] = "admin@example.com"
        //         it[passwordHash] = "hashed_admin_password" // Asegúrate de hashear esto correctamente
        //         it[name] = "Admin"
        //     }
        // }
    }

    // Puedes loguear que la conexión y la inicialización fueron exitosas
    log.info("Database initialized successfully and schemas created/updated.")

    return database // Devuelve la instancia de Database
}

// Nota: La función connectToPostgres original con lógica de H2 embebido
// puede ser útil para testing, pero la configuración principal debería usar HikariCP
// y leer de application.yaml para entornos de desarrollo/producción.