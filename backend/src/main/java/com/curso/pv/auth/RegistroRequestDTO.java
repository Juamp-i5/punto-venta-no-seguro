package com.curso.pv.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequestDTO(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(max = 255) String password,
        @Size(max = 100) String nombre,
        @Size(max = 120) String email
) {
}
