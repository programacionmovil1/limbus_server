package com.limbus_server.gateway

import com.limbus_server.gateway.application.service.configureAdministration
import com.limbus_server.gateway.infrastructure.config.configureHTTP
import com.limbus_server.gateway.infrastructure.config.configureMonitoring
import com.limbus_server.gateway.infrastructure.config.configureSecurity
import com.limbus_server.gateway.infrastructure.config.configureSerialization
import com.limbus_server.gateway.infrastructure.routing.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureAdministration()
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureHTTP()
    configureRouting()
}
