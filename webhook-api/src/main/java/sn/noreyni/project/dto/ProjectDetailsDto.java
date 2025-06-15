package sn.noreyni.project.dto;

import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;
import sn.noreyni.user.dto.UserDetailsDto;

import java.util.Set;

public record ProjectDetailsDto(
        String id,
        String name,
        String description,
        ProjectStatus status,
        Visibility visibility,
        ProjectType type,
        UserDetailsDto owner,
        Set<UserDetailsDto> members,
        Set<UserDetailsDto> invitedUsers
) {
}