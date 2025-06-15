package sn.noreyni.project.dto;

import jakarta.validation.constraints.Size;
import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;

public record ProjectUpdateDto(
        @Size(min = 2, max = 100, message = "Le nom du projet doit contenir entre 2 et 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        ProjectType type,

        Visibility visibility,

        ProjectStatus status,

        String avatarUrl
) {}
