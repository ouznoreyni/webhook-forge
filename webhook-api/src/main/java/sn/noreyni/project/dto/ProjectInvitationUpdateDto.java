package sn.noreyni.project.dto;

import sn.noreyni.common.enums.InvitationStatus;

import java.time.LocalDateTime;

public record ProjectInvitationUpdateDto(
        String projectId,
        String inviterId,
        String inviteeId,
        LocalDateTime expiresAt,
        InvitationStatus status
) {}
