package com.limbus_server.auth.domain.model

// Modelo de dominio que representa la información básica de un usuario obtenida de Google.
// Esta clase encapsula los datos relevantes extraídos de un ID Token de Google verificado.
data class GoogleUser(
    // El identificador único del usuario en Google (sub claim del ID Token).
    val id: String,
    // El correo electrónico del usuario verificado por Google.
    val email: String,
    // El nombre completo del usuario.
    val name: String?,
    // Opcional: URL de la foto de perfil del usuario.
    val picture: String?
)

// Notas:
// - Esta clase es un modelo de dominio puro, agnóstico a la base de datos o serialización.
// - Contiene la información esencial del perfil de Google necesaria para tu aplicación.
// - El GoogleAuthClient (en infrastructure/security) será responsable de mapear
//   la respuesta de Google a este modelo de dominio.
