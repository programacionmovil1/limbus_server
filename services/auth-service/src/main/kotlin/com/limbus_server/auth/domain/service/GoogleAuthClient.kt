package com.limbus_server.auth.domain.service

import com.limbus_server.auth.domain.model.GoogleUser

// Interfaz que define el contrato para interactuar con los servicios de autenticación de Google.
// Esta interfaz es parte de la capa de dominio y es agnóstica a la implementación concreta
// de cómo se verifica el ID Token de Google.
interface GoogleAuthClient {

    /**
     * Verifica un ID Token de Google.
     * Este método se encarga de validar la autenticidad, la audiencia, el emisor y la expiración del token
     * con los servidores de Google o una biblioteca de verificación de tokens.
     *
     * @param idToken El ID Token de Google (una cadena JWT) proporcionado por el cliente.
     * @return Un objeto GoogleUser que contiene la información del usuario si el token es válido y verificado,
     * o null si el token es inválido, expirado o no se puede verificar.
     */
    suspend fun verifyIdToken(idToken: String): GoogleUser?
}

// Notas:
// - Esta interfaz solo define QUÉ operaciones de verificación de Google existen.
// - NO contiene la lógica de implementación (cómo se realiza la llamada a Google o la verificación).
// - La implementación concreta de esta interfaz (GoogleAuthClientImpl) residirá en la capa de infraestructura.
// - El AuthService (en la capa de aplicación) dependerá de esta interfaz para delegar
//   la validación de los tokens de Google.
