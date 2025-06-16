package sn.noreyni.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;

import org.modelmapper.ModelMapper;
import sn.noreyni.project.dto.ProjectInvitationCreateDto;
import sn.noreyni.project.dto.ProjectInvitationDetailsDto;
import sn.noreyni.project.dto.ProjectInvitationListDto;
import sn.noreyni.project.dto.ProjectInvitationUpdateDto;
import sn.noreyni.user.UserMapper;
import sn.noreyni.user.dto.UserListDto;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ProjectInvitationMapper {

    @Inject
    ModelMapper modelMapper;

    @Inject
    ProjectMapper projectMapper;

    @Inject
    UserMapper userMapper;

    /**
     * Convert ProjectInvitation entity to ProjectInvitationListDto
     */
    public ProjectInvitationListDto toListDto(ProjectInvitation invitation) {
        if (invitation == null) {
            return null;
        }

        return new ProjectInvitationListDto(
                invitation.getIdAsString(),
                invitation.getProjectId(),
                invitation.getProject() != null ? invitation.getProject().getName() : null,
                invitation.getInviterId(),
                invitation.getInviter() != null ? userMapper.toListDto(invitation.getInviter()) : null,
                invitation.getStatus(),
                invitation.getSentAt(),
                invitation.getExpiresAt()
        );
    }

    /**
     * Convert list of ProjectInvitation entities to list of ProjectInvitationListDto
     */
    public List<ProjectInvitationListDto> toListDto(List<ProjectInvitation> invitations) {
        if (invitations == null) {
            return null;
        }
        return invitations.stream()
                .map(this::toListDto)
                .toList();
    }

    /**
     * Convert ProjectInvitation entity to ProjectInvitationDetailsDto
     * Note: This assumes single invitation entity but maps to the multi-invitee DTO structure
     */
    public ProjectInvitationDetailsDto toDetailsDto(ProjectInvitation invitation) {
        if (invitation == null) {
            return null;
        }

        // For single invitation, wrap invitee data in lists
        List<String> inviteeIds = invitation.getInviteeId() != null ?
                List.of(invitation.getInviteeId()) : List.of();

        List<UserListDto> invitees = invitation.getInvitee() != null ?
                List.of(userMapper.toListDto(invitation.getInvitee())) : List.of();

        return new ProjectInvitationDetailsDto(
                invitation.getIdAsString(),
                invitation.getProjectId(),
                invitation.getProject() != null ? projectMapper.toListDto(invitation.getProject()) : null,
                invitation.getInviterId(),
                invitation.getInviter() != null ? userMapper.toListDto(invitation.getInviter()) : null,
                inviteeIds,
                invitees,
                invitation.getStatus(),
                invitation.getSentAt(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt(),
                invitation.getUpdatedAt(),
                invitation.getCreatedBy(),
                invitation.getUpdatedBy()
        );
    }

    /**
     * Convert list of ProjectInvitation entities to single ProjectInvitationDetailsDto
     * This groups multiple invitations (for same project/inviter) into one DTO
     */
    public ProjectInvitationDetailsDto toGroupedDetailsDto(List<ProjectInvitation> invitations) {
        if (invitations == null || invitations.isEmpty()) {
            return null;
        }

        ProjectInvitation firstInvitation = invitations.getFirst();

        List<String> inviteeIds = invitations.stream()
                .map(ProjectInvitation::getInviteeId)
                .filter(Objects::nonNull)
                .toList();

        List<UserListDto> invitees = invitations.stream()
                .map(ProjectInvitation::getInvitee)
                .filter(Objects::nonNull)
                .map(userMapper::toListDto)
                .toList();

        return new ProjectInvitationDetailsDto(
                firstInvitation.getIdAsString(), // Use first invitation's ID as group ID
                firstInvitation.getProjectId(),
                firstInvitation.getProject() != null ? projectMapper.toListDto(firstInvitation.getProject()) : null,
                firstInvitation.getInviterId(),
                firstInvitation.getInviter() != null ? userMapper.toListDto(firstInvitation.getInviter())
                        : null,
                inviteeIds,
                invitees,
                firstInvitation.getStatus(), // You might want to compute a combined status
                firstInvitation.getSentAt(),
                firstInvitation.getExpiresAt(),
                firstInvitation.getCreatedAt(),
                firstInvitation.getUpdatedAt(),
                firstInvitation.getCreatedBy(),
                firstInvitation.getUpdatedBy()
        );
    }

    /**
     * Convert ProjectInvitationCreateDto to ProjectInvitation entity for a specific email
     */
    public ProjectInvitation toEntity(ProjectInvitationCreateDto createDto, String inviteeEmail, String inviteeId) {
        if (createDto == null || inviteeEmail == null) {
            return null;
        }

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProjectId(createDto.projectId());
        invitation.setInviteeId(inviteeId);
        invitation.setExpiresAt(createDto.expiresAt());

        return invitation;
    }

    /**
     * Convert ProjectInvitationCreateDto to list of ProjectInvitation entities
     * Note: inviteeIds need to be resolved separately by finding users by email
     */
    public List<ProjectInvitation> toEntities(ProjectInvitationCreateDto createDto, List<String> inviteeIds) {
        if (createDto == null || createDto.inviteeEmails() == null || inviteeIds == null) {
            return Collections.emptyList();
        }

        if (createDto.inviteeEmails().size() != inviteeIds.size()) {
            throw new IllegalArgumentException("Email list and invitee ID list must have the same size");
        }

        return inviteeIds.stream()
                .map(inviteeId -> {
                    ProjectInvitation invitation = new ProjectInvitation();
                    invitation.setProjectId(createDto.projectId());
                    invitation.setInviteeId(inviteeId);
                    invitation.setExpiresAt(createDto.expiresAt());
                    return invitation;
                })
                .toList();
    }

    /**
     * Create individual ProjectInvitation for each email in the bulk request
     */
    public ProjectInvitation createInvitationForEmail(ProjectInvitationCreateDto createDto, String email, String inviteeId) {
        if (createDto == null || email == null || inviteeId == null) {
            return null;
        }

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProjectId(createDto.projectId());
        invitation.setInviteeId(inviteeId);
        invitation.setExpiresAt(createDto.expiresAt());

        return invitation;
    }

    /**
     * Update ProjectInvitation entity from ProjectInvitationUpdateDto
     */
    public void updateEntity(ProjectInvitation invitation, ProjectInvitationUpdateDto updateDto) {
        if (invitation == null || updateDto == null) {
            return;
        }

        if (updateDto.status() != null) {
            invitation.setStatus(updateDto.status());
        }
        if (updateDto.expiresAt() != null) {
            invitation.setExpiresAt(updateDto.expiresAt());
        }
    }


}

