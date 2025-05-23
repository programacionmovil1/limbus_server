package com.limbus_server.auth.application.dto.request

import kotlinx.serialization.Serializable

// Data class que representa la petición de inicio de sesión con Google.
// El cliente (aplicación móvil o web) enviará el ID Token de Google a este endpoint.
@Serializable // Anotación necesaria para que kotlinx.serialization pueda serializar/deserializar esta clase
data class GoogleLoginRequest(
    // El ID Token de Google que se obtiene después de que el usuario se autentica
    // y autoriza la aplicación en Google. Este token será verificado por el backend.
    val idToken: String
)

// Notas:
// - Esta clase encapsula los datos mínimos necesarios para iniciar sesión con Google.
// - El backend será responsable de validar este ID Token con los servidores de Google
//   y de crear o autenticar al usuario en tu propio sistema.
