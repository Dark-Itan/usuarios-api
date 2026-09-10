package com.ejemplo.usuarios.repository;

import com.ejemplo.usuarios.dto.UsuarioResponseDTO;
import com.ejemplo.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    // Buscar usuario por email (para login y validación de duplicados)
    Optional<Usuario> findByEmail(String email);

    // Verificar si existe un email (para registro)
    boolean existsByEmail(String email);

    // Buscar usuarios por estado (activo/inactivo)
    List<Usuario> findByEstado(Boolean estado);

    // Obtener todos los usuarios sin exponer password (proyección DTO)
    @Query("SELECT new com.ejemplo.usuarios.dto.UsuarioResponseDTO(u.id, u.nombre, u.email, u.estado) " +
            "FROM Usuario u ORDER BY u.nombre ASC")
    List<UsuarioResponseDTO> findAllUsuarios();

    // Obtener usuario por ID sin exponer password (proyección DTO)
    @Query("SELECT new com.ejemplo.usuarios.dto.UsuarioResponseDTO(u.id, u.nombre, u.email, u.estado) " +
            "FROM Usuario u WHERE u.id = :id")
    Optional<UsuarioResponseDTO> findUsuarioById(@Param("id") UUID id);
}