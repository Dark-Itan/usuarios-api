package com.ejemplo.usuarios.service;

import com.ejemplo.usuarios.dto.*;
import com.ejemplo.usuarios.entity.Usuario;

import java.util.List;
import java.util.UUID;

public interface UsuarioService {

    UsuarioResponseDTO registrarUsuario(RegisterRequestDTO registerRequest);

    String loginUsuario(LoginRequestDTO loginRequest);

    List<UsuarioResponseDTO> obtenerTodosUsuarios();

    UsuarioResponseDTO obtenerUsuarioPorId(UUID id);

    UsuarioResponseDTO actualizarUsuario(UUID id, UsuarioUpdateDTO updateRequest);

    void eliminarUsuario(UUID id);

    Usuario buscarUsuarioPorEmail(String email);
}