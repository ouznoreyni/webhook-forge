package sn.noreyni.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import sn.noreyni.common.enums.UserRole;

public record UserUpdateDto(
        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        String firstName,

        @Size(min = 2, max = 50, message = "Le nom de famille doit contenir entre 2 et 50 caractères")
        String lastName,

        @Email(message = "L'email doit être valide")
        String email,
        UserRole role
) {
}
