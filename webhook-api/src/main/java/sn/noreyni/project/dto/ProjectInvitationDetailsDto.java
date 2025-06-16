package sn.noreyni.project.dto;

import sn.noreyni.common.enums.InvitationStatus;
import sn.noreyni.user.dto.UserListDto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectInvitationDetailsDto(
        String id,
        String projectId,
        ProjectListDto project,
        String inviterId,
        UserListDto inviter,
        List<String> inviteeIds,
        List<UserListDto> invitees,
        InvitationStatus status,
        LocalDateTime sentAt,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
}