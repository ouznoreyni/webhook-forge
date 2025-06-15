package sn.noreyni.project.dto;

public record ProjectInvitationStatsDto(
        long totalInvitations,
        long pendingInvitations,
        long acceptedInvitations,
        long rejectedInvitations,
        long expiredInvitations
) {}