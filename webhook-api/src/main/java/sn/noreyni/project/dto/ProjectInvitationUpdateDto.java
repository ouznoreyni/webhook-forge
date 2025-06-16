package sn.noreyni.project.dto;

import sn.noreyni.common.enums.InvitationStatus;

import java.time.LocalDateTime;

public record ProjectInvitationUpdateDto(
        InvitationStatus status,
        LocalDateTime expiresAt
) {}