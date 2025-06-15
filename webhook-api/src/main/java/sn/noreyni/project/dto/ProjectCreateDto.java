package sn.noreyni.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bson.codecs.pojo.annotations.BsonProperty;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;

public record ProjectCreateDto(
        String avatarUrl,
        @NotBlank(message = "Le nom du projet est requis")
        @Size(min = 2, max = 100, message = "Le nom du projet doit contenir entre 2 et 100 caractères")
        String name,

        @NotBlank(message = "La description est requise")
        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        @NotNull(message = "La visibilité est requise")
        Visibility visibility,

        @NotNull(message = "Le type est requis")
        ProjectType type

) {
}
