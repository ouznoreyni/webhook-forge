package sn.noreyni.project.dto;

import sn.noreyni.common.enums.InvitationStatus;

import java.time.LocalDateTime;

public record ProjectInvitationListDto(
        String id,
        String projectId,
        String projectName,
        String inviterName,
        String inviteeName,
        LocalDateTime sentAt,
        LocalDateTime expiresAt,
        InvitationStatus status,
        boolean isExpired
) {}