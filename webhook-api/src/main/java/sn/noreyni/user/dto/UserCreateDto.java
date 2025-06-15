package sn.noreyni.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.noreyni.common.enums.UserRole;

public record UserCreateDto(
        @NotBlank(message = "Le prénom est requis")
        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        String firstName,

        @NotBlank(message = "Le nom de famille est requis")
        @Size(min = 2, max = 50, message = "Le nom de famille doit contenir entre 2 et 50 caractères")
        String lastName,

        @NotBlank(message = "L'email est requis")
        @Email(message = "L'email doit être valide")
        String email,

        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        String password,

        @NotNull(message = "Le rôle est requis")
        UserRole role
) {}