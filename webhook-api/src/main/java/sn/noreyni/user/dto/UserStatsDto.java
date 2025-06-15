package sn.noreyni.user.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record UserStatsDto(
        @Schema(description = "Number of active users")
        Long activeUsersCount
) {}