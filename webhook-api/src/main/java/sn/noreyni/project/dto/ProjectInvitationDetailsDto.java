package sn.noreyni.project.dto;

import sn.noreyni.common.enums.InvitationStatus;

import java.time.LocalDateTime;

public record ProjectInvitationDetailsDto(
        String id,
        String projectId,
        String inviterId,
        String inviteeId,
        LocalDateTime sentAt,
        LocalDateTime expiresAt,
        InvitationStatus status
) {}