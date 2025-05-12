package com.limbus_server.auth.infrastructure.config // Paquete corregido

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.*
import org.slf4j.event.*

fun Application.configureMonitoring() {

    // Configuración de Micrometer Metrics con Prometheus
    // Esto expone métricas de la aplicación (como el número de peticiones, latencia, etc.)
    // en un formato que Prometheus puede recolectar.
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        // Asocia el registro de Prometheus con el plugin de Micrometer de Ktor
        registry = appMicrometerRegistry
        // Aquí podrías añadir configuraciones adicionales para las métricas,
        // como tags personalizados, filtros, etc.
        // por ejemplo: distributionStatisticExpiry = 10.seconds
    }

    // Configuración de Call Logging
    // Esto registra información sobre cada petición entrante en los logs de la aplicación.
    install(CallLogging) {
        level = Level.INFO // Nivel de log para las peticiones (INFO, DEBUG, etc.)
        // Puedes añadir filtros si solo quieres loguear ciertas peticiones
        filter { call -> call.request.path().startsWith("/") } // Loguea todas las peticiones que empiezan con "/"
        // Puedes añadir MDC (Mapped Diagnostic Context) para incluir información adicional en los logs
        // mdc("user-id") { call -> call.principal<JWTPrincipal>()?.payload?.subject }
    }

    // Define un endpoint para exponer las métricas de Prometheus
    // Prometheus configurado externamente (ej: en Docker Compose) recolectará las métricas desde esta URL.
    routing {
        get("/metrics") { // Endpoint donde se exponen las métricas
            call.respond(appMicrometerRegistry.scrape()) // Responde con los datos de las métricas en formato Prometheus
        }
    }

    // Se elimina la configuración de Dropwizard Metrics ya que se está usando Micrometer.
    // Si realmente necesitas Dropwizard, asegúrate de tener la dependencia correcta
    // (ktor-server-metrics) y su reporter (Slf4jReporter).
}
