package sn.noreyni.project.dto;

import jakarta.validation.constraints.NotNull;
import sn.noreyni.common.enums.InvitationStatus;

public record ProjectInvitationResponseDto(
        @NotNull(message = "La réponse à l'invitation est requise")
        InvitationStatus status // ACCEPTED or REJECTED
) {}

