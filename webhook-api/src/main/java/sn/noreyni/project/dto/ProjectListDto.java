package sn.noreyni.project.dto;

import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;

import java.time.LocalDateTime;

public record ProjectListDto(
        String id,
        String name,
        String description,
        ProjectStatus status,
        Visibility visibility,
        ProjectType type,
        String avatarUrl,
        String ownerName,
        int memberCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
