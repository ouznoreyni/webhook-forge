package sn.noreyni.project.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProjectInvitationCreateDto(
        @NotNull(message = "L'ID du projet est requis")
        String projectId,

        @NotNull(message = "L'ID de l'utilisateur invité est requis")
        String inviteeId,
        LocalDateTime expiresAt // Optional
) {}