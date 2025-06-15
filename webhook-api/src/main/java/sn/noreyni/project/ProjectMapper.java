package sn.noreyni.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.Visibility;
import sn.noreyni.project.dto.*;
import sn.noreyni.user.User;
import sn.noreyni.user.UserMapper;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project mapper for converting between entities and DTOs
 * Uses manual mapping for records and ModelMapper for entities
 *
 * @author Noreyni Team
 * @version 1.0
 * @since 2025
 */
@ApplicationScoped
public class ProjectMapper {

    @Inject
    ModelMapper modelMapper;

    @Inject
    UserMapper userMapper;

    /**
     * Converts Project entity to ProjectListDto
     * Manual mapping because records don't have no-arg constructors
     *
     * @param project the project entity
     * @return ProjectListDto or null if input is null
     */
    public ProjectListDto toListDto(Project project) {
        if (project == null) {
            return null;
        }

        String ownerName = "Inconnu";
        if (project.getOwner() != null) {
            ownerName = project.getOwner().getFirstName() + " " + project.getOwner().getLastName();
        }

        int memberCount = project.getMemberIds() != null ? project.getMemberIds().size() : 0;

        return new ProjectListDto(
                project.getIdAsString(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getVisibility(),
                project.getType(),
                project.getAvatarUrl(),
                ownerName,
                memberCount,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    /**
     * Converts Project entity to ProjectDetailsDto
     * Manual mapping because records don't have no-arg constructors
     *
     * @param project the project entity
     * @return ProjectDetailsDto or null if input is null
     */
    public ProjectDetailsDto toDetailsDto(Project project) {
        if (project == null) {
            return null;
        }

        return new ProjectDetailsDto(
                project.getIdAsString(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getVisibility(),
                project.getType(),
                project.getAvatarUrl(),
                userMapper.toDetailsDto(project.getOwner()),
                project.getMembers() != null ? userMapper.toDetailsDtoList(project.getMembers()) : null,
                project.getInvitedUsers() != null ?
                        userMapper.toDetailsDtoList(project.getInvitedUsers()) : null,
                project.getCreatedBy(),
                project.updatedBy,
                project.getCreatedAt(),
                project.getUpdatedAt()

        );
    }

    /**
     * Converts ProjectCreateDto to Project entity
     * Uses ModelMapper since Project is a regular class
     *
     * @param dto the creation DTO
     * @return Project entity or null if input is null
     */
    public Project toEntity(ProjectCreateDto dto) {
        if (dto == null) {
            return null;
        }

        // ModelMapper works fine for mapping TO regular classes
        Project project = modelMapper.map(dto, Project.class);

        // Set defaults for fields not in DTO
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.DRAFT);
        }
        if (project.getVisibility() == null) {
            project.setVisibility(Visibility.PRIVATE);
        }

        // Initialize collections
        project.setMemberIds(new HashSet<>());
        project.setInvitedUserIds(new HashSet<>());

        return project;
    }

    /**
     * Updates Project entity with data from ProjectUpdateDto
     * Uses manual mapping to handle null values properly
     *
     * @param project the target project entity to update
     * @param dto the update DTO with new values
     */
    public void updateEntity(Project project, ProjectUpdateDto dto) {
        if (dto == null || project == null) {
            return;
        }

        // Manual mapping to handle null values correctly
        if (dto.name() != null) {
            project.setName(dto.name());
        }
        if (dto.description() != null) {
            project.setDescription(dto.description());
        }
        if (dto.type() != null) {
            project.setType(dto.type());
        }
        if (dto.visibility() != null) {
            project.setVisibility(dto.visibility());
        }
        if (dto.status() != null) {
            project.setStatus(dto.status());
        }
        if (dto.avatarUrl() != null) {
            project.setAvatarUrl(dto.avatarUrl());
        }

        // Always update the timestamp using BaseEntity method
        project.preUpdate();
    }

    /**
     * Alternative method for updating entity from update request
     * Provides same functionality as updateEntity for backward compatibility
     *
     * @param project the target project entity to update
     * @param request the update DTO with new values
     */
    public void updateEntityFromUpdateRequest(Project project, ProjectUpdateDto request) {
        updateEntity(project, request); // Delegate to main update method
    }

    /**
     * Converts Project entity to ProjectMemberDto
     *
     * @param project the project context
     * @param user the user to convert
     * @return ProjectMemberDto or null if input is null
     */
    public ProjectMemberDto toMemberDto(Project project, User user) {
        if (project == null || user == null) {
            return null;
        }

        LocalDateTime joinedAt = getJoinedAt(project, user);
        boolean isOwner = isOwner(project, user);

        return new ProjectMemberDto(
                userMapper.toDetailsDto(user),
                joinedAt,
                isOwner
        );
    }

    /**
     * Converts list of Projects to list of ProjectListDto
     *
     * @param projects the list of project entities
     * @return List of ProjectListDto
     */
    public List<ProjectListDto> toListDtoList(List<Project> projects) {
        if (projects == null) {
            return null;
        }

        return projects.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts list of Projects to list of ProjectDetailsDto
     *
     * @param projects the list of project entities
     * @return List of ProjectDetailsDto
     */
    public List<ProjectDetailsDto> toDetailsDtoList(List<Project> projects) {
        if (projects == null) {
            return null;
        }

        return projects.stream()
                .map(this::toDetailsDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts list of Users to list of ProjectMemberDto for a given project
     *
     * @param users the list of user entities
     * @param project the project context
     * @return List of ProjectMemberDto
     */
    public List<ProjectMemberDto> toMemberDtoList(List<User> users, Project project) {
        if (users == null || project == null) {
            return null;
        }

        return users.stream()
                .map(user -> toMemberDto(project, user))
                .collect(Collectors.toList());
    }

    /**
     * Creates ProjectStatsDto from individual counts
     *
     * @param totalProjects total number of projects
     * @param activeProjects number of active projects
     * @param completedProjects number of completed projects
     * @param draftProjects number of draft projects
     * @param ownedProjects number of owned projects
     * @param memberProjects number of projects where user is member
     * @return ProjectStatsDto
     */
    public ProjectStatsDto toStatsDto(long totalProjects, long activeProjects, long completedProjects,
                                      long draftProjects, long ownedProjects, long memberProjects) {
        return new ProjectStatsDto(totalProjects, activeProjects, completedProjects,
                draftProjects, ownedProjects, memberProjects);
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
    private LocalDateTime getJoinedAt(Project project, User user) {
        // This would typically come from a separate membership table
        // For now, returning project creation date for owner, current time for members
        if (isOwner(project, user)) {
            return project.getCreatedAt();
        }
        return LocalDateTime.now(); // In real implementation, get from membership table
    }

    private boolean isOwner(Project project, User user) {
        return project.getOwnerId() != null && project.getOwnerId().equals(user.getIdAsString());
    }
}
