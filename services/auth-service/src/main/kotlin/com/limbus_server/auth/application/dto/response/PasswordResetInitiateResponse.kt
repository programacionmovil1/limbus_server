package com.limbus_server.auth.application.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetInitiateResponse(
    val message: String,
    // TEMPORAL PARA DESARROLLO/DEBUGGING: El token de restablecimiento de contraseña.
    // ¡REMUEVE ESTO PARA PRODUCCIÓN! No debes exponer tokens de restablecimiento en la respuesta.
    val resetToken: String? = null
)
