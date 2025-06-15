package sn.noreyni.project.dto;

import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;

public record ProjectSearchDto(
        String name,
        String description,
        ProjectStatus status,
        ProjectType type,
        Visibility visibility,
        String ownerId,
        Boolean isOwner,
        Boolean isMember
) {}