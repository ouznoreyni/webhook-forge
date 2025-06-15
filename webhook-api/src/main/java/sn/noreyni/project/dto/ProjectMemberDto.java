package sn.noreyni.project.dto;

import sn.noreyni.user.dto.UserDetailsDto;

import java.time.LocalDateTime;

public record ProjectMemberDto(
        UserDetailsDto user,
        LocalDateTime joinedAt,
        boolean isOwner
) {}