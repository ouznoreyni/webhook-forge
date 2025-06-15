package sn.noreyni.project.dto;

import sn.noreyni.common.enums.InvitationStatus;
import sn.noreyni.user.dto.UserDetailsDto;

import java.time.LocalDateTime;

public record ProjectInvitationDetailsDto(
        String id,
        ProjectDetailsDto project,
        UserDetailsDto inviter,
        UserDetailsDto invitee,
        LocalDateTime sentAt,
        LocalDateTime expiresAt,
        InvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
