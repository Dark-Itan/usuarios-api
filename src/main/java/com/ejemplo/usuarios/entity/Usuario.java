package com.ejemplo.usuarios.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "usuarios",
        indexes = {
                @Index(name = "idx_usuarios_email", columnList = "email", unique = true),
                @Index(name = "idx_usuarios_estado", columnList = "estado")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    // Método helper para activar/desactivar usuario
    public void activar() {
        this.estado = true;
    }

    public void desactivar() {
        this.estado = false;
    }
}