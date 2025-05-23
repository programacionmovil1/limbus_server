package com.limbus_server.auth.domain.repository

import com.limbus_server.auth.domain.model.User

// Interfaz que define las operaciones de acceso a datos para la entidad User.
// Esta interfaz es parte de la capa de dominio y es agnóstica a la implementación de persistencia.
interface UserRepository {

    /**
     * Crea un nuevo usuario en el sistema.
     * @param user El modelo de dominio User a crear.
     * @return El ID del usuario recién creado.
     */
    suspend fun createUser(user: User): Int

    /**
     * Busca un usuario por su identificador único.
     * @param id El ID del usuario a buscar.
     * @return El modelo de dominio User si se encuentra, o null en caso contrario.
     */
    suspend fun findById(id: Int): User?

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * @param email El correo electrónico del usuario a buscar.
     * @return El modelo de dominio User si se encuentra, o null en caso contrario.
     */
    suspend fun findByEmail(email: String): User?

    /**
     * Actualiza la contraseña (hash) de un usuario específico.
     * @param userId El ID del usuario cuya contraseña se actualizará.
     * @param passwordHash El nuevo hash de la contraseña.
     */
    suspend fun updatePassword(userId: Int, passwordHash: String)

    /**
     * Marca la dirección de correo electrónico de un usuario como verificada.
     * @param userId El ID del usuario cuyo email se marcará como verificado.
     */
    suspend fun markEmailAsVerified(userId: Int)

    // Puedes añadir aquí otras operaciones de acceso a datos que necesite el AuthService, por ejemplo:

    /**
     * Busca un usuario asociado a un token de verificación de email.
     * @param token El valor del token de verificación.
     * @return El modelo de dominio User si se encuentra un token válido asociado, o null.
     * (Nota: Esto asume que el token de verificación se puede buscar directamente para encontrar al usuario.
     * La implementación podría implicar buscar el token en una tabla separada y luego obtener el usuario).
     */
    // suspend fun findByVerificationToken(token: String): User?

    /**
     * Busca un usuario asociado a un token de restablecimiento de contraseña.
     * @param token El valor del token de restablecimiento.
     * @return El modelo de dominio User si se encuentra un token válido asociado, o null.
     * (Nota: Similar a findByVerificationToken, la implementación puede variar).
     */
    // suspend fun findByPasswordResetToken(token: String): User?

    /**
     * Elimina un usuario por su ID.
     * @param userId El ID del usuario a eliminar.
     */
    // suspend fun deleteUser(userId: Int)

    /**
     * Actualiza el nombre de un usuario.
     * @param userId El ID del usuario.
     * @param name El nuevo nombre.
     */
    // suspend fun updateUserName(userId: Int, name: String?)
}

// Notas:
// - Esta interfaz solo define QUÉ operaciones de acceso a datos existen.
// - NO contiene la lógica de implementación (cómo se guardan, buscan o actualizan los datos).
// - La implementación concreta de esta interfaz (UserRepositoryImpl) residirá en la capa de infraestructura.
// - El AuthService (en la capa de aplicación) dependerá de esta interfaz para realizar operaciones de persistencia.
