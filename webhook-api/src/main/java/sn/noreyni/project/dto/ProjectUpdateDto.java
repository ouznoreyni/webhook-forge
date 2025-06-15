package sn.noreyni.project.dto;

import jakarta.validation.constraints.Size;
import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;
import sn.noreyni.user.dto.UserDetailsDto;

public record ProjectUpdateDto(
        @Size(min = 2, max = 100, message = "Le nom du projet doit contenir entre 2 et 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        ProjectStatus status,

        Visibility visibility,

        ProjectType type,

        UserDetailsDto ownerId
) {
}