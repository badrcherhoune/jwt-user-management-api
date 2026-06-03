package com.challenge.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthRequest {

    @Schema(description = "Username ou email de l'utilisateur")
    private String username;

    @Schema(description = "Mot de passe")
    private String password;
}
