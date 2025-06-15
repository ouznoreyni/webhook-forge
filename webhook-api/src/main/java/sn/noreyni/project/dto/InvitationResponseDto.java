package sn.noreyni.project.dto;

import jakarta.validation.constraints.NotNull;

public class InvitationResponseDto{
    @NotNull(message = "Response status is required")
    private Boolean accept;
}
