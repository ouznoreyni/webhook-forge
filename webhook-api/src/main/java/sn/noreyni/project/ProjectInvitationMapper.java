package sn.noreyni.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import sn.noreyni.common.enums.InvitationStatus;
import sn.noreyni.project.dto.*;
import sn.noreyni.user.UserMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProjectInvitation mapper for converting between entities and DTOs
 * Uses manual mapping for records and ModelMapper for entities
 *
 * @author Noreyni Team
 * @version 1.0
 * @since 2025
 */
@ApplicationScoped
public class ProjectInvitationMapper {

    @Inject
    ModelMapper modelMapper;

    @Inject
    UserMapper userMapper;

    @Inject
    ProjectMapper projectMapper;

    /**
     * Converts BulkInviteUsersDto to list of individual ProjectInviteUserDto
     *
     * @param bulkDto the bulk invitation DTO
     * @return List of individual invitation DTOs
     */
    public List<ProjectInviteUserDto> toIndividualInvites(BulkInviteUsersDto bulkDto) {
        if (bulkDto == null || bulkDto.emails() == null) {
            return null;
        }

        return bulkDto.emails().stream()
                .map(email -> new ProjectInviteUserDto(
                        email,
                        bulkDto.projectId(),
                        bulkDto.expiresAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Converts ProjectInvitation entity to ProjectInvitationListDto
     * Manual mapping because records don't have no-arg constructors
     *
     * @param invitation the invitation entity
     * @return ProjectInvitationListDto or null if input is null
     */
    public ProjectInvitationListDto toListDto(ProjectInvitation invitation) {
        if (invitation == null) {
            return null;
        }

        String projectName = getProjectName(invitation);
        String inviterName = getInviterName(invitation);
        String inviteeName = getInviteeName(invitation);
        boolean isExpired = isExpired(invitation);

        return new ProjectInvitationListDto(
                invitation.getIdAsString(),
                invitation.getProjectId(),
                projectName,
                inviterName,
                inviteeName,
                invitation.getSentAt(),
                invitation.getExpiresAt(),
                invitation.getStatus(),
                isExpired
        );
    }

    /**
     * Converts ProjectInvitation entity to ProjectInvitationDetailsDto
     * Manual mapping because records don't have no-arg constructors
     *
     * @param invitation the invitation entity
     * @return ProjectInvitationDetailsDto or null if input is null
     */
    public ProjectInvitationDetailsDto toDetailsDto(ProjectInvitation invitation) {
        if (invitation == null) {
            return null;
        }

        return new ProjectInvitationDetailsDto(
                invitation.getIdAsString(),
                projectMapper.toDetailsDto(invitation.getProject()),
                userMapper.toDetailsDto(invitation.getInviter()),
                userMapper.toDetailsDto(invitation.getInvitee()),
                invitation.getSentAt(),
                invitation.getExpiresAt(),
                invitation.getStatus(),
                invitation.getCreatedAt(),
                invitation.getUpdatedAt()
        );
    }

    /**
     * Converts ProjectInvitationCreateDto to ProjectInvitation entity
     * Uses ModelMapper since ProjectInvitation is a regular class
     *
     * @param dto the creation DTO
     * @return ProjectInvitation entity or null if input is null
     */
    public ProjectInvitation toEntity(ProjectInvitationCreateDto dto) {
        if (dto == null) {
            return null;
        }

        // ModelMapper works fine for mapping TO regular classes
        ProjectInvitation invitation = modelMapper.map(dto, ProjectInvitation.class);

        // Set defaults
        invitation.setSentAt(LocalDateTime.now());
        invitation.setStatus(InvitationStatus.PENDING);

        if (invitation.getExpiresAt() == null) {
            invitation.setExpiresAt(getDefaultExpirationDate());
        }

        return invitation;
    }

    /**
     * Converts ProjectInviteUserDto to ProjectInvitation entity
     * Uses manual mapping since we need to handle email lookup
     *
     * @param dto the invite user DTO
     * @return ProjectInvitation entity or null if input is null
     */
    public ProjectInvitation toEntity(ProjectInviteUserDto dto) {
        if (dto == null) {
            return null;
        }

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProjectId(dto.projectId());
        // Note: inviteeId will be set by service after finding user by email
        invitation.setSentAt(LocalDateTime.now());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(dto.expiresAt() != null ? dto.expiresAt() : getDefaultExpirationDate());

        return invitation;
    }

    /**
     * Updates ProjectInvitation entity status from ProjectInvitationResponseDto
     * Uses manual mapping to handle status update
     *
     * @param invitation the target invitation entity to update
     * @param dto the response DTO with new status
     */
    public void updateStatusFromDto(ProjectInvitation invitation, ProjectInvitationResponseDto dto) {
        if (dto == null || invitation == null) {
            return;
        }

        if (dto.status() != null) {
            invitation.setStatus(dto.status());
        }

        // Always update the timestamp using BaseEntity method
        invitation.preUpdate();
    }

    /**
     * Converts list of ProjectInvitations to list of ProjectInvitationListDto
     *
     * @param invitations the list of invitation entities
     * @return List of ProjectInvitationListDto
     */
    public List<ProjectInvitationListDto> toListDtoList(List<ProjectInvitation> invitations) {
        if (invitations == null) {
            return null;
        }

        return invitations.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts list of ProjectInvitations to list of ProjectInvitationDetailsDto
     *
     * @param invitations the list of invitation entities
     * @return List of ProjectInvitationDetailsDto
     */
    public List<ProjectInvitationDetailsDto> toDetailsDtoList(List<ProjectInvitation> invitations) {
        if (invitations == null) {
            return null;
        }

        return invitations.stream()
                .map(this::toDetailsDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates ProjectInvitationStatsDto from individual counts
     *
     * @param totalInvitations total number of invitations
     * @param pendingInvitations number of pending invitations
     * @param acceptedInvitations number of accepted invitations
     * @param rejectedInvitations number of rejected invitations
     * @param expiredInvitations number of expired invitations
     * @return ProjectInvitationStatsDto
     */
    public ProjectInvitationStatsDto toStatsDto(long totalInvitations, long pendingInvitations,
                                                long acceptedInvitations, long rejectedInvitations,
                                                long expiredInvitations) {
        return new ProjectInvitationStatsDto(totalInvitations, pendingInvitations, acceptedInvitations,
                rejectedInvitations, expiredInvitations);
    }

    /**
     * Converts string ID to ObjectId with validation
     *
     * @param id the string ID to convert
     * @return ObjectId or null if input is null/empty
     * @throws IllegalArgumentException if ID format is invalid
     */
    public ObjectId toObjectId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ObjectId format: " + id, e);
        }
    }

    /**
     * Converts ObjectId to String safely
     *
     * @param objectId the ObjectId to convert
     * @return String representation or null if input is null
     */
    public String toStringId(ObjectId objectId) {
        return objectId != null ? objectId.toString() : null;
    }

    /**
     * Validates if a string is a valid ObjectId format
     *
     * @param id the string to validate
     * @return true if valid ObjectId format, false otherwise
     */
    public boolean isValidObjectId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        try {
            new ObjectId(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Helper methods
    private String getProjectName(ProjectInvitation invitation) {
        if (invitation.getProject() != null) {
            return invitation.getProject().getName();
        }
        return "Projet inconnu";
    }

    private String getInviterName(ProjectInvitation invitation) {
        if (invitation.getInviter() != null) {
            return invitation.getInviter().getFirstName() + " " + invitation.getInviter().getLastName();
        }
        return "Utilisateur inconnu";
    }

    private String getInviteeName(ProjectInvitation invitation) {
        if (invitation.getInvitee() != null) {
            return invitation.getInvitee().getFirstName() + " " + invitation.getInvitee().getLastName();
        }
        return "Utilisateur inconnu";
    }

    private boolean isExpired(ProjectInvitation invitation) {
        return invitation.getExpiresAt() != null &&
                invitation.getExpiresAt().isBefore(LocalDateTime.now()) &&
                invitation.getStatus() == InvitationStatus.PENDING;
    }

    private LocalDateTime getDefaultExpirationDate() {
        return LocalDateTime.now().plusDays(7); // 7 days expiration by default
    }
}
