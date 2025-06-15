package sn.noreyni.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record BulkInviteUsersDto(
        @NotBlank(message = "L'ID du projet est requis")
        String projectId,

        @NotEmpty(message = "La liste des emails ne peut pas être vide")
        @Size(max = 50, message = "Vous ne pouvez pas inviter plus de 50 utilisateurs à la fois")
        @Valid
        List<@Email(message = "L'email doit être valide") String> emails,

        LocalDateTime expiresAt
) {}