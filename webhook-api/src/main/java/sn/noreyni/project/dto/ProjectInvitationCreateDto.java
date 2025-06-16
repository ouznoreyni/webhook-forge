package sn.noreyni.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectInvitationCreateDto(
        @NotBlank(message = "L'ID du projet est requis")
        String projectId,

        @NotEmpty(message = "Au moins un email d'invité est requis")
        @Valid
        List<@Email(message = "Tous les emails doivent être valides") String> inviteeEmails,

        LocalDateTime expiresAt
) {
}