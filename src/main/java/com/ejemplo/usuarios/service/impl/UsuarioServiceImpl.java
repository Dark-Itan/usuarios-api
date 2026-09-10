package com.ejemplo.usuarios.service.impl;

import com.ejemplo.usuarios.dto.*;
import com.ejemplo.usuarios.entity.Usuario;
import com.ejemplo.usuarios.exception.EmailAlreadyExistsException;
import com.ejemplo.usuarios.exception.InvalidCredentialsException;  // ← FALTA ESTE
import com.ejemplo.usuarios.exception.ResourceNotFoundException;
import com.ejemplo.usuarios.repository.UsuarioRepository;
import com.ejemplo.usuarios.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ejemplo.usuarios.exception.AccountDeactivatedException;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponseDTO registrarUsuario(RegisterRequestDTO registerRequest) {
        logger.info("Iniciando registro de usuario con email: {}", registerRequest.getEmail());

        if (usuarioRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Intento de registro con email duplicado: {}", registerRequest.getEmail());
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(registerRequest.getNombre());
        usuario.setEmail(registerRequest.getEmail());
        usuario.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        usuario.setEstado(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        logger.info("Usuario registrado exitosamente con ID: {}", usuarioGuardado.getId());

        return new UsuarioResponseDTO(
                usuarioGuardado.getId(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getEmail(),
                usuarioGuardado.getEstado()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public String loginUsuario(LoginRequestDTO loginRequest) {
        logger.info("Intento de login para email: {}", loginRequest.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login fallido: email no encontrado");
                    return new InvalidCredentialsException("Credenciales inválidas");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            logger.warn("Login fallido: contraseña incorrecta para email: {}", loginRequest.getEmail());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        // Validar que el usuario esté activo
        if (!usuario.getEstado()) {
            logger.warn("Login fallido: usuario desactivado con email: {}", loginRequest.getEmail());
            throw new AccountDeactivatedException("Cuenta desactivada");
        }

        logger.info("Login exitoso para usuario ID: {}", usuario.getId());
        return "Login exitoso";
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodosUsuarios() {
        logger.info("Obteniendo lista de todos los usuarios");
        return usuarioRepository.findAllUsuarios();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioPorId(UUID id) {
        logger.info("Buscando usuario con ID: {}", id);
        return usuarioRepository.findUsuarioById(id)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarUsuario(UUID id, UsuarioUpdateDTO updateRequest) {
        logger.info("Actualizando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado para actualizar con ID: {}", id);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });

        if (!usuario.getEmail().equals(updateRequest.getEmail()) &&
                usuarioRepository.existsByEmail(updateRequest.getEmail())) {
            logger.warn("Email duplicado en actualización: {}", updateRequest.getEmail());
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        usuario.setNombre(updateRequest.getNombre());
        usuario.setEmail(updateRequest.getEmail());

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        logger.info("Usuario actualizado exitosamente con ID: {}", id);

        return new UsuarioResponseDTO(
                usuarioActualizado.getId(),
                usuarioActualizado.getNombre(),
                usuarioActualizado.getEmail(),
                usuarioActualizado.getEstado()
        );
    }

    @Override
    @Transactional
    public void eliminarUsuario(UUID id) {
        logger.info("Eliminando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado para eliminar con ID: {}", id);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });

        usuario.desactivar();
        usuarioRepository.save(usuario);
        logger.info("Usuario desactivado exitosamente con ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorEmail(String email) {
        logger.info("Buscando usuario por email: {}", email);
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado con email: {}", email);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });
    }
}