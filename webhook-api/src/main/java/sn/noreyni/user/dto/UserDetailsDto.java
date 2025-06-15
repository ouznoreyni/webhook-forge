package sn.noreyni.user.dto;

import sn.noreyni.common.enums.UserRole;

import java.time.LocalDateTime;

public record UserDetailsDto(
        String id,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        boolean active,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt
) {}