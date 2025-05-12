package com.limbus_server.auth.infrastructure.database

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.limbus_server.auth.infrastructure.database.dsl.UsersSchema
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*

// Esta función configura e inicializa la conexión a la base de datos
fun Application.configureDatabases() {
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
    // Se ejecuta dentro de una transacción
    transaction(database) {
        SchemaUtils.create(UsersSchema) // Crea la tabla UsersSchema si no existe
        // Si tienes otras tablas relacionadas con autenticación (ej: tokens de refresco), créalas aquí también
        // SchemaUtils.create(RefreshTokensSchema)
    }

    // Puedes loguear que la conexión fue exitosa
    log.info("Database initialized successfully")

    // Las rutas y la lógica de negocio que usan la base de datos
    // se definirán en otras capas (routing, repository, service).
    // La conexión 'database' o el 'dataSource' se proporcionarán
    // a través de inyección de dependencias (Koin).
}

// Nota: La función connectToPostgres original con lógica de H2 embebido
// puede ser útil para testing, pero la configuración principal debería usar HikariCP
// y leer de application.yaml para entornos de desarrollo/producción.
// Si necesitas la lógica de H2 para tests, puedes mantener esa función en un archivo separado
// o dentro de los sources de test.