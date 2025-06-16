package sn.noreyni.project.dto;

import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;
import sn.noreyni.user.dto.UserListDto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectDetailsDto(
        String id,
        String name,
        String description,
        ProjectStatus status,
        Visibility visibility,
        ProjectType type,
        String avatarUrl,
        String ownerId,
        UserListDto owner,
        List<UserListDto> members,
        List<UserListDto> invitedUsers,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy

) {}