package sn.noreyni.user.dto;

import sn.noreyni.common.enums.UserRole;

public record UserListDto(
        String id,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        boolean active
) {}