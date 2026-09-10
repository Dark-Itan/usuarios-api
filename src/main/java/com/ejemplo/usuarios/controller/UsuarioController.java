package com.ejemplo.usuarios.controller;

import com.ejemplo.usuarios.dto.UsuarioResponseDTO;
import com.ejemplo.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ejemplo.usuarios.dto.UsuarioUpdateDTO;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;

    // Inyección por constructor
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodosUsuarios() {
        logger.info("Petición GET recibida en /api/v1/users");

        List<UsuarioResponseDTO> usuarios = usuarioService.obtenerTodosUsuarios();

        logger.info("Retornando {} usuarios", usuarios.size());
        return ResponseEntity.ok(usuarios);
    }

    // Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorId(@PathVariable UUID id) {
        logger.info("Petición GET recibida en /api/v1/users/{}", id);

        UsuarioResponseDTO usuario = usuarioService.obtenerUsuarioPorId(id);

        logger.info("Usuario encontrado con ID: {}", id);
        return ResponseEntity.ok(usuario);
    }

    // Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @PathVariable UUID id,
            @Valid @RequestBody UsuarioUpdateDTO updateRequest) {
        logger.info("Petición PUT recibida en /api/v1/users/{}", id);

        UsuarioResponseDTO usuarioActualizado = usuarioService.actualizarUsuario(id, updateRequest);

        logger.info("Usuario actualizado exitosamente con ID: {}", id);
        return ResponseEntity.ok(usuarioActualizado);
    }

    // Eliminar usuario (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable UUID id) {
        logger.info("Petición DELETE recibida en /api/v1/users/{}", id);

        usuarioService.eliminarUsuario(id);

        logger.info("Usuario desactivado exitosamente con ID: {}", id);
        return ResponseEntity.ok("Usuario desactivado exitosamente");
    }
}