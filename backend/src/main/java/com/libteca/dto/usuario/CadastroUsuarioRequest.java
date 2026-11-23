package com.libteca.dto.usuario;

import jakarta.validation.constraints.*;

public record CadastroUsuarioRequest(
        @NotBlank @Size(max=70) String nome,
        @NotBlank @Email @Size(max=100) String email,
        @NotBlank @Size(min=6) String senha,
        @NotBlank(message="O telefone é obrigatório")
        @Pattern(regexp="[0-9]{10,11}",message="Informe o DDD e telefone, com 10 ou 11 dígitos") String telefone
) {}
