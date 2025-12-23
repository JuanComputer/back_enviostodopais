package com.sanchez.Envios.Repositories;

import com.sanchez.Envios.Models.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, UUID> {

    @Query("SELECT t from Usuarios as t WHERE t.dni=:dni AND t.correo=:correo")
    Optional<Usuarios> findByDniAndCorreo(String dni,String correo);
    @Query("SELECT t from Usuarios as t WHERE t.rol.nombre=:rol AND t.activo=:active")
    List<Usuarios> findByRolNombreAndActivo(String rol, Boolean active);
    @Query("SELECT t from Usuarios as t WHERE t.rol.nombre=:rol")
    List<Usuarios> findByRolNombre(String rol);
    @Query("SELECT t FROM Usuarios as t WHERE t.activo=:active")
    List<Usuarios> findByActivo(Boolean active);
    long countByActivo(Boolean activo);
    Optional<Usuarios> findByDni(String dni);
    // Nueva consulta para buscar por correo
    Optional<Usuarios> findByCorreo(String correo);
}
