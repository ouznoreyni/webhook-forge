package sn.noreyni.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProjectInviteUserDto(
        @NotBlank(message = "L'email de l'utilisateur est requis")
        @Email(message = "L'email doit être valide")
        String userEmail,

        @NotNull(message = "L'ID du projet est requis")
        String projectId,

        LocalDateTime expiresAt // Optional, if null, default expiration will be set
) {}