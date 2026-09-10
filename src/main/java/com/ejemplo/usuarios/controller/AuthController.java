package com.ejemplo.usuarios.controller;

import com.ejemplo.usuarios.dto.LoginRequestDTO;
import com.ejemplo.usuarios.dto.RegisterRequestDTO;
import com.ejemplo.usuarios.dto.UsuarioResponseDTO;
import com.ejemplo.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioService usuarioService;

    // Inyección por constructor
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint de registro
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        logger.info("Petición POST recibida en /api/v1/auth/register");

        UsuarioResponseDTO usuarioCreado = usuarioService.registrarUsuario(registerRequest);

        logger.info("Usuario registrado exitosamente con ID: {}", usuarioCreado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
    }

    // Endpoint de login
    @PostMapping("/login")
    public ResponseEntity<String> loginUsuario(@Valid @RequestBody LoginRequestDTO loginRequest) {
        logger.info("Petición POST recibida en /api/v1/auth/login");

        String mensaje = usuarioService.loginUsuario(loginRequest);

        logger.info("Login exitoso para email: {}", loginRequest.getEmail());
        return ResponseEntity.ok(mensaje);
    }
}