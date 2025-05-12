import com.limbus_server.auth.infrastructure.config.configureAdministration
import com.limbus_server.auth.infrastructure.config.configureHTTP
import com.limbus_server.auth.infrastructure.config.configureMonitoring
import com.limbus_server.auth.infrastructure.config.configureSerialization
import com.limbus_server.auth.infrastructure.routing.configureRouting
import com.limbus_server.auth.infrastructure.security.configureSecurity
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
