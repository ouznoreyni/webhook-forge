package sn.noreyni.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;

import org.modelmapper.ModelMapper;
import sn.noreyni.project.dto.ProjectCreateDto;
import sn.noreyni.project.dto.ProjectDetailsDto;
import sn.noreyni.project.dto.ProjectListDto;
import sn.noreyni.project.dto.ProjectUpdateDto;
import sn.noreyni.user.UserMapper;

import java.util.List;

@ApplicationScoped
public class ProjectMapper {

    @Inject
    ModelMapper modelMapper;

    @Inject
    UserMapper userMapper;

    /**
     * Convert Project entity to ProjectListDto
     */
    public ProjectListDto toListDto(Project project) {
        if (project == null) {
            return null;
        }

        return new ProjectListDto(
                project.getIdAsString(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getVisibility(),
                project.getType(),
                project.getAvatarUrl(),
                project.getOwnerId(),
                project.getOwner() != null ? project.getOwner().getFirstName() : null,
                project.getOwner() != null ? project.getOwner().getLastName() : null,
                project.getMemberIds() != null ? project.getMemberIds().size() : 0,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    /**
     * Convert list of Project entities to list of ProjectListDto
     */
    public List<ProjectListDto> toListDto(List<Project> projects) {
        if (projects == null) {
            return Collections.emptyList();
        }
        return projects.stream()
                .map(this::toListDto)
                .toList();
    }

    /**
     * Convert Project entity to ProjectDetailsDto
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
                project.getOwnerId(),
                project.getOwner() != null ? userMapper.toListDto(project.getOwner()) : null,
                project.getMembers() != null ?
                        project.getMembers().stream().map(userMapper::toListDto).toList() : null,
                project.getInvitedUsers() != null ?
                        project.getInvitedUsers().stream().map(userMapper::toListDto).toList() : null,
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getCreatedBy(),
                project.getUpdatedBy()
        );
    }

    /**
     * Convert ProjectCreateDto to Project entity
     */
    public Project toEntity(ProjectCreateDto createDto) {
        if (createDto == null) {
            return null;
        }

        Project project = new Project();
        project.setName(createDto.name());
        project.setDescription(createDto.description());
        project.setType(createDto.type());
        project.setVisibility(createDto.visibility());
        project.setStatus(createDto.status());
        project.setAvatarUrl(createDto.avatarUrl());

        return project;
    }

    /**
     * Update Project entity from ProjectUpdateDto
     */
    public void updateEntity(Project project, ProjectUpdateDto updateDto) {
        if (project == null || updateDto == null) {
            return;
        }

        if (updateDto.name() != null) {
            project.setName(updateDto.name());
        }
        if (updateDto.description() != null) {
            project.setDescription(updateDto.description());
        }
        if (updateDto.type() != null) {
            project.setType(updateDto.type());
        }
        if (updateDto.visibility() != null) {
            project.setVisibility(updateDto.visibility());
        }
        if (updateDto.status() != null) {
            project.setStatus(updateDto.status());
        }
        if (updateDto.avatarUrl() != null) {
            project.setAvatarUrl(updateDto.avatarUrl());
        }
    }


}
