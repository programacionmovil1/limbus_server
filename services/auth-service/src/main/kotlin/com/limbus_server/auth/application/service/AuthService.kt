package com.limbus_server.auth.application.service

import com.limbus_server.auth.application.dto.request.*
import com.limbus_server.auth.application.dto.response.*
import com.limbus_server.auth.application.exception.*
import com.limbus_server.auth.domain.model.User
import com.limbus_server.auth.domain.repository.UserRepository
import com.limbus_server.auth.domain.service.PasswordHasher
import com.limbus_server.auth.domain.service.GoogleAuthClient
import com.limbus_server.auth.infrastructure.security.TokenService
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Clase de servicio de aplicación para manejar la lógica de autenticación y autorización.
// Esta clase orquesta las operaciones, interactuando con repositorios, servicios de dominio
// y componentes de infraestructura según sea necesario.
class AuthService(
    // Dependencias inyectadas (probablemente usando Koin):
    private val userRepository: UserRepository, // Interfaz para interactuar con los datos de usuario
    private val passwordHasher: PasswordHasher, // Servicio para hashear y verificar contraseñas
    private val tokenService: TokenService, // Servicio para generar y validar tokens JWT y de refresco
    private val googleAuthClient: GoogleAuthClient // ¡NUEVA DEPENDENCIA: Cliente para la autenticación de Google!
    // Puedes necesitar otras dependencias aquí, como un servicio para enviar correos electrónicos
    // si manejas verificación de email o restablecimiento de contraseña con emails.
    // private val emailService: EmailService // TODO: Descomentar e inyectar si se implementa el envío de emails
) {

    // Método para registrar un nuevo usuario.
    // Recibe los datos individuales y devuelve un DTO de respuesta (con tokens para auto-login).
    // Lanza excepciones si el registro falla (ej: usuario ya existe).
    suspend fun registerUser(email: String, password: String, name: String?): RegisterResponse {
        // 1. Validar datos de entrada (ej: formato de email, fortaleza de contraseña - aunque algunas validaciones pueden estar en el DTO o en un validador separado).
        // 2. Verificar si ya existe un usuario con el mismo email.
        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            // Si el usuario ya existe, lanzar la excepción personalizada
            throw UserAlreadyExistsException("User with email $email already exists")
        }

        // 3. Hashear la contraseña proporcionada por el usuario.
        val hashedPassword = passwordHasher.hashPassword(password)

        // 4. Crear una nueva entidad de usuario (modelo de dominio).
        // El ID será 0 porque la base de datos lo generará automáticamente.
        val newUser = User(
            id = 0, // El ID será autoincremental en la BD
            email = email,
            passwordHash = hashedPassword,
            name = name,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC), // Establece la fecha de creación
            updatedAt = null, // Se puede dejar nulo inicialmente o establecer también
            isEmailVerified = false, // Por defecto, el email no está verificado al registrarse
            isAccountLocked = false // Por defecto, la cuenta no está bloqueada
        )

        // 5. Guardar el nuevo usuario en la base de datos a través del repositorio.
        val userId = userRepository.createUser(newUser) // Pasa el objeto User completo

        // 6. Generar un token de verificación de email y enviarlo (si implementas verificación).
        val verificationToken = tokenService.generateVerificationToken(userId)
        // TODO: Descomentar si se implementa EmailService
        // emailService.sendVerificationEmail(email, verificationToken.token) // Pasar la cadena del token

        // 7. Generar tokens de acceso y refresco para iniciar sesión automáticamente después del registro.
        val accessToken = tokenService.generateAccessToken(userId)
        val refreshTokenObject = tokenService.generateRefreshToken(userId) // Obtiene el objeto RefreshToken

        // 8. Devolver la respuesta de registro exitoso con los tokens.
        return RegisterResponse(
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshTokenObject.token, // Extrae la cadena del token del objeto
            expiresIn = tokenService.getAccessTokenExpirationSeconds(), // Obtener la expiración del token de acceso
            verificationToken = verificationToken.token // Incluye el token de verificación en la respuesta (temporal para desarrollo)
        )
    }

    // Método para iniciar sesión.
    // Recibe las credenciales individuales y devuelve un DTO de respuesta (con tokens).
    // Lanza excepciones si el login falla (ej: usuario no encontrado, credenciales inválidas, email no verificado, cuenta bloqueada).
    suspend fun loginUser(email: String, password: String): AuthResponse {
        // 1. Validar datos de entrada (ej: formato de email).
        // 2. Buscar el usuario por email.
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException("User with email $email not found")

        // 3. Verificar si la cuenta está bloqueada.
        if (user.isAccountLocked) {
            throw AccountLockedException("Account for user ${user.email} is locked")
        }

        // 4. Verificar si el email está verificado.
        if (!user.isEmailVerified) {
            throw EmailNotVerifiedException("Email for user ${user.email} is not verified")
        }

        // 5. Verificar la contraseña proporcionada contra el hash almacenado.
        val passwordMatches = passwordHasher.verifyPassword(password, user.passwordHash)
        if (!passwordMatches) {
            // Si la contraseña no coincide, lanzar la excepción personalizada
            // TODO: Considerar lógica para contar intentos fallidos y bloquear cuenta.
            throw InvalidCredentialsException("Invalid password for user with email $email")
        }

        // 6. Si las credenciales son válidas, generar nuevos tokens de acceso y refresco.
        val accessToken = tokenService.generateAccessToken(user.id)
        val refreshTokenObject = tokenService.generateRefreshToken(user.id) // Obtiene el objeto RefreshToken

        // 7. Devolver la respuesta de autenticación exitosa.
        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenObject.token, // Extrae la cadena del token del objeto
            expiresIn = tokenService.getAccessTokenExpirationSeconds()
        )
    }

    /**
     * Método para iniciar sesión utilizando un ID Token de Google.
     * Valida el ID Token con Google, busca o crea el usuario en la base de datos local,
     * y genera tokens de autenticación para el sistema.
     * @param idToken El ID Token de Google proporcionado por el cliente.
     * @return AuthResponse con los tokens de acceso y refresco de tu sistema.
     * @throws InvalidTokenException Si el ID Token de Google no es válido o no se puede verificar.
     * @throws UserAlreadyExistsException Si un usuario con el mismo email ya existe pero no está vinculado a Google.
     */
    suspend fun loginWithGoogle(idToken: String): AuthResponse {
        // 1. Validar el ID Token de Google con el cliente de autenticación de Google.
        // Esto verificará la firma, la audiencia, el emisor y la expiración del token.
        val googleUser = googleAuthClient.verifyIdToken(idToken)
            ?: throw InvalidTokenException("Invalid Google ID Token.")

        // 2. Buscar si ya existe un usuario en tu base de datos con el email de Google.
        val existingUser = userRepository.findByEmail(googleUser.email)

        val user: User
        if (existingUser != null) {
            // Si el usuario ya existe, lo usamos.
            // TODO: Considerar si el usuario existente no tiene passwordHash (creado solo con Google)
            // o si tiene passwordHash pero no está vinculado a Google.
            // Para simplificar, asumimos que si el email existe, es el mismo usuario.
            // En un sistema real, podrías querer vincular cuentas o manejar conflictos.
            user = existingUser
        } else {
            // Si no existe, creamos un nuevo usuario en tu base de datos.
            // La contraseña se puede dejar vacía o generar una aleatoria si no se usa para login directo.
            // Opcionalmente, podrías marcarlo como "registrado vía Google" con un campo adicional.
            val newUser = User(
                id = 0, // El ID será autoincremental en la BD
                email = googleUser.email,
                passwordHash = "", // No hay contraseña para login con Google, o un hash vacío/placeholder
                name = googleUser.name,
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                updatedAt = null,
                isEmailVerified = true, // El email ya está verificado por Google
                isAccountLocked = false
            )
            val userId = userRepository.createUser(newUser)
            user = newUser.copy(id = userId) // Actualiza el ID del objeto User
        }

        // 3. Generar tokens de acceso y refresco para el usuario en tu sistema.
        val accessToken = tokenService.generateAccessToken(user.id)
        val refreshTokenObject = tokenService.generateRefreshToken(user.id)

        // 4. Devolver la respuesta de autenticación exitosa.
        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenObject.token,
            expiresIn = tokenService.getAccessTokenExpirationSeconds()
        )
    }

    // Método para refrescar un token de acceso usando un token de refresco.
    // Recibe el token de refresco y devuelve un DTO de respuesta (nuevos tokens).
    // Lanza excepciones si el refresh token es inválido o expirado.
    suspend fun refreshAccessToken(refreshToken: String): AuthResponse {
        // 1. Validar el refresh token proporcionado.
        // Esto puede implicar verificar su formato, firma y si existe y es válido en la base de datos.
        val userId = tokenService.validateRefreshToken(refreshToken) // Este método debería devolver el ID del usuario si es válido
            ?: throw InvalidTokenException("Invalid or not found refresh token") // Lanzar si el token no es válido/encontrado

        // 2. Verificar si el usuario asociado al refresh token existe y está activo.
        val user = userRepository.findById(userId) ?: throw UserNotFoundException("User associated with token not found")
        if (user.isAccountLocked) {
            throw AccountLockedException("Account associated with token is locked")
        }
        if (!user.isEmailVerified) {
            throw EmailNotVerifiedException("Email for user associated with token is not verified")
        }

        // 3. Generar un nuevo token de acceso (y opcionalmente un nuevo token de refresco).
        val newAccessToken = tokenService.generateAccessToken(userId)
        // val newRefreshTokenObject = tokenService.generateRefreshToken(userId) // Si rotas refresh tokens

        // 4. Opcional: Invalidar el refresh token antiguo si rotas tokens.
        // tokenService.invalidateRefreshToken(refreshToken) // Asume que TokenService tiene un método para invalidar por cadena

        // 5. Devolver la respuesta con los nuevos tokens.
        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = refreshToken, // O newRefreshTokenObject.token si rotas
            expiresIn = tokenService.getAccessTokenExpirationSeconds()
        )
    }

    // Método para iniciar el proceso de "Olvidé mi contraseña".
    // Recibe el email.
    // Ahora devuelve un DTO con el token (temporalmente para desarrollo).
    suspend fun initiatePasswordReset(email: String): PasswordResetInitiateResponse { // CAMBIO AQUÍ: Devuelve PasswordResetInitiateResponse
        val user = userRepository.findByEmail(email)

        if (user == null) {
            // Loguear intento para email no encontrado, pero responder como si fuera exitoso
            // para evitar enumeración de usuarios.
            // Para desarrollo, podrías decidir no exponer el token si el usuario no existe.
            return PasswordResetInitiateResponse(
                message = "Password reset initiated successfully. If an account with that email exists, an email has been sent.",
                resetToken = null // No devolvemos token si el usuario no existe.
            )
        }

        val resetTokenObject = tokenService.generatePasswordResetToken(user.id)

        // TODO: Descomentar si se implementa EmailService
        // emailService.sendPasswordResetEmail(email, resetTokenObject.token)

        return PasswordResetInitiateResponse(
            message = "Password reset initiated successfully. If an account with that email exists, an email has been sent.",
            resetToken = resetTokenObject.token // INCLUYE EL TOKEN TEMPORALMENTE
        )
    }

    // Método para restablecer la contraseña usando un token de restablecimiento.
    // Recibe el token y la nueva contraseña. No devuelve datos específicos en caso de éxito.
    // Lanza excepciones si el token es inválido/expirado o la nueva contraseña no es válida.
    suspend fun resetPassword(resetToken: String, newPassword: String) { // Ajustado el parámetro
        // 1. Validar datos de entrada (ej: fortaleza de la nueva contraseña, que coincida con confirmación si la incluyes).
        // 2. Validar el token de restablecimiento proporcionado.
        // Esto implica verificar su formato, si existe en la BD, si no ha expirado y a qué usuario pertenece.
        val userId = tokenService.validatePasswordResetToken(resetToken) // Este método debería devolver el ID del usuario si es válido
            ?: throw InvalidTokenException("Invalid or expired password reset token") // Lanzar si el token no es válido/expirado

        // 3. Buscar el usuario asociado al token.
        val user = userRepository.findById(userId) ?: throw UserNotFoundException("User associated with reset token not found")

        // 4. Hashear la nueva contraseña.
        val newHashedPassword = passwordHasher.hashPassword(newPassword)

        // 5. Actualizar la contraseña del usuario en la base de datos.
        userRepository.updatePassword(userId, newHashedPassword)

        // 6. Invalidar el token de restablecimiento para que no pueda ser usado de nuevo.
        tokenService.invalidatePasswordResetToken(resetToken) // Asume que TokenService tiene un método para invalidar por cadena

        // 7. Opcional: Notificar al usuario por email que su contraseña ha sido cambiada.
        // TODO: Descomentar si se implementa EmailService
        // emailService.sendPasswordChangedNotification(user.email)

        // 8. No devolver datos específicos en caso de éxito (usar SuccessResponse en el handler de ruta).
    }

    // Método para verificar el correo electrónico usando un token de verificación.
    // Recibe el token de verificación. No devuelve datos específicos en caso de éxito.
    // Lanza excepciones si el token es inválido o expirado.
    suspend fun verifyEmail(verificationToken: String) { // Ajustado el parámetro
        // 1. Validar el token de verificación proporcionado.
        // Esto implica verificar su formato, si existe en la BD, si no ha expirado y a qué usuario pertenece.
        val userId = tokenService.validateVerificationToken(verificationToken) // Este método debería devolver el ID del usuario si es válido
            ?: throw InvalidTokenException("Invalid or expired email verification token") // Lanzar si el token no es válido/expirado

        // 2. Buscar el usuario asociado al token.
        val user = userRepository.findById(userId) ?: throw UserNotFoundException("User associated with verification token not found")

        // 3. Marcar el email del usuario como verificado en la base de datos.
        userRepository.markEmailAsVerified(userId)

        // 4. Invalidar el token de verificación.
        tokenService.invalidateVerificationToken(verificationToken) // Asume que TokenService tiene un método para invalidar por cadena

        // 5. No devolver datos específicos en caso de éxito (usar SuccessResponse en el handler de ruta).
    }

    // Opcional: Método para obtener información básica del usuario autenticado
    // suspend fun getUserInfo(userId: Int): UserResponse {
    //     val user = userRepository.findById(userId) ?: throw UserNotFoundException("User not found")
    //     // Mapear la entidad de usuario a un UserResponse DTO
    //     return UserResponse(id = user.id, email = user.email, name = user.name)
    // }
}
