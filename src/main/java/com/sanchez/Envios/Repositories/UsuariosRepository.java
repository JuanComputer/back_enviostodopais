package com.sanchez.Envios.Repositories;

import com.sanchez.Envios.Models.Tiendas;
import com.sanchez.Envios.Models.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, UUID> {

    Optional<Usuarios> findByCorreo(String correo);
    Optional<Usuarios> findByDni(String dni);

    @Query("SELECT u FROM Usuarios u WHERE u.dni = :dni AND u.correo = :correo")
    Optional<Usuarios> findByDniAndCorreo(String dni, String correo);

    @Query("SELECT u FROM Usuarios u WHERE u.rol.nombre = :rol AND u.activo = :activo")
    List<Usuarios> findByRolNombreAndActivo(String rol, Boolean activo);

    @Query("SELECT u FROM Usuarios u WHERE u.rol.nombre = :rol")
    List<Usuarios> findByRolNombre(String rol);

    @Query("SELECT u FROM Usuarios u WHERE u.activo = :activo")
    List<Usuarios> findByActivo(Boolean activo);

    // Usuarios por sede (para Admin de Sede)
    List<Usuarios> findBySede(Tiendas sede);

    @Query("SELECT u FROM Usuarios u WHERE u.sede = :sede AND u.activo = :activo")
    List<Usuarios> findBySedeAndActivo(Tiendas sede, Boolean activo);

    long countByActivo(Boolean activo);

    @Query("SELECT COUNT(u) FROM Usuarios u WHERE u.sede = :sede")
    long countBySede(Tiendas sede);
}
