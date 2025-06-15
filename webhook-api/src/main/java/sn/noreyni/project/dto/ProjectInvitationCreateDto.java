package sn.noreyni.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sn.noreyni.common.enums.InvitationStatus;

import java.time.LocalDateTime;

public record ProjectInvitationCreateDto(
        @NotBlank(message = "L'ID du projet est requis")
        String projectId,

        @NotBlank(message = "L'ID de l'inviteur est requis")
        String inviterId,

        @NotBlank(message = "L'ID de l'invité est requis")
        String inviteeId,

        @NotNull(message = "La date d'expiration est requise")
        LocalDateTime expiresAt,

        @NotNull(message = "Le statut est requis")
        InvitationStatus status
) {}