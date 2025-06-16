package sn.noreyni.project.dto;

import sn.noreyni.common.enums.InvitationStatus;
import sn.noreyni.user.dto.UserListDto;

import java.time.LocalDateTime;

public record ProjectInvitationListDto(
        String id,
        String projectId,
        String projectName,
        String inviterId,
        UserListDto inviter,
        InvitationStatus status,
        LocalDateTime sentAt,
        LocalDateTime expiresAt
) {
}
