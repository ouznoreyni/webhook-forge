package sn.noreyni.project;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;
import sn.noreyni.common.exception.ApiException;
import sn.noreyni.common.response.PaginationMeta;
import sn.noreyni.project.dto.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Service class for managing project operations
 * Provides comprehensive CRUD operations with audit logging and error handling
 */
@ApplicationScoped
@Slf4j
public class ProjectService {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProjectMapper projectMapper;

    /**
     * Retrieves a paginated list of projects with optional filters
     *
     * @param name Filter by project name (partial match)
     * @param status Filter by project status
     * @param visibility Filter by project visibility
     * @param type Filter by project type
     * @param ownerId Filter by owner ID
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy Field to sort by (default: "name")
     * @param sortDirection Sort direction ("asc" or "desc", default: "asc")
     * @return Uni containing list of ProjectListDto
     */
    public Uni<List<ProjectListDto>> findAll(
            String name,
            ProjectStatus status,
            Visibility visibility,
            ProjectType type,
            String ownerId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Instant start = Instant.now();

        log.info("project.findAll.start - Fetching projects page={}, size={}, filters=[name={}, status={}, visibility={}, type={}, ownerId={}]",
                page, size, name, status, visibility, type, ownerId);

        return projectRepository.searchProjects(name, status, visibility, type, ownerId, page, size, sortBy, sortDirection)
                .map(projects -> {
                    List<ProjectListDto> result = projects.stream()
                            .map(projectMapper::toListDto)
                            .toList();

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.findAll.success - Retrieved {} projects in {}ms, page={}, size={}",
                            result.size(), duration.toMillis(), page, size);

                    return result;
                })
                .onFailure().invoke(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.findAll.error - Failed to fetch projects after {}ms, page={}, size={}, error={}",
                            duration.toMillis(), page, size, throwable.getMessage(), throwable);
                });
    }

    /**
     * Retrieves pagination metadata for project listing
     *
     * @param name Filter by project name
     * @param status Filter by project status
     * @param visibility Filter by project visibility
     * @param type Filter by project type
     * @param ownerId Filter by owner ID
     * @param page the page number
     * @param size the page size
     * @return Uni containing pagination metadata
     */
    public Uni<PaginationMeta> getPaginationMeta(
            String name,
            ProjectStatus status,
            Visibility visibility,
            ProjectType type,
            String ownerId,
            int page,
            int size) {

        Instant start = Instant.now();

        log.debug("project.pagination.start - Computing pagination meta for page={}, size={}, with filters", page, size);

        return projectRepository.countSearchResults(name, status, visibility, type, ownerId)
                .map(totalElements -> {
                    PaginationMeta meta = PaginationMeta.of(page, size, totalElements);

                    Duration duration = Duration.between(start, Instant.now());
                    log.debug("project.pagination.success - Computed pagination meta in {}ms, totalElements={}, totalPages={}",
                            duration.toMillis(), totalElements, meta.totalPages());

                    return meta;
                })
                .onFailure().invoke(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.pagination.error - Failed to compute pagination meta after {}ms, error={}",
                            duration.toMillis(), throwable.getMessage(), throwable);
                });
    }

    /**
     * Finds a project by its unique identifier
     *
     * @param id the project ID as string
     * @return Uni containing ProjectDetailsDto if found
     * @throws ApiException with 404 status if project not found
     */
    public Uni<ProjectDetailsDto> findById(String id) {
        Instant start = Instant.now();

        log.info("project.findById.start - Searching project with id={}", id);

        return validateObjectId(id)
                .chain(objectId -> projectRepository.findById(objectId))
                .onItem().ifNull().failWith(Unchecked.supplier(() -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.warn("project.findById.notFound - Project not found after {}ms, id={}", duration.toMillis(), id);
                    throw new ApiException("Projet non trouvé avec l'id: " + id, 404);
                }))
                .map(project -> {
                    ProjectDetailsDto result = projectMapper.toDetailsDto(project);

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.findById.success - Project found in {}ms, id={}, name={}",
                            duration.toMillis(), id, result.name());

                    return result;
                })
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("project.findById.error - Unexpected error after {}ms, id={}, error={}",
                                duration.toMillis(), id, throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * Creates a new project in the system
     *
     * @param createDto the project creation data
     * @param currentUserId the ID of the user creating the project
     * @return Uni containing the created ProjectDetailsDto
     * @throws ApiException with 409 status if project name already exists
     */
    public Uni<ProjectDetailsDto> create(ProjectCreateDto createDto, String currentUserId) {
        Instant start = Instant.now();

        log.info("project.create.start - Creating project with name={}, type={}, createdBy={}",
                createDto.name(), createDto.type(), currentUserId);

        return projectRepository.existsByName(createDto.name())
                .chain(Unchecked.function(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.warn("project.create.conflict - Project name already exists after {}ms, name={}",
                                duration.toMillis(), createDto.name());
                        throw new ApiException("Un projet existe déjà avec le nom: " + createDto.name(), 409);
                    }

                    Project project = projectMapper.toEntity(createDto);

                    // Set owner and audit fields
                    project.setOwnerId(currentUserId);
                    project.prePersist(currentUserId);

                    // Set default values if not provided
                    if (project.getStatus() == null) {
                        project.setStatus(ProjectStatus.DRAFT);
                    }
                    if (project.getVisibility() == null) {
                        project.setVisibility(Visibility.PRIVATE);
                    }

                    log.debug("project.create.persisting - Persisting project entity, name={}", createDto.name());

                    return project.persist();
                }))
                .map(project -> {
                    ProjectDetailsDto result = projectMapper.toDetailsDto((Project) project);

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.create.success - Project created in {}ms, id={}, name={}, type={}",
                            duration.toMillis(), result.id(), result.name(), result.type());

                    return result;
                })
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("project.create.error - Failed to create project after {}ms, name={}, error={}",
                                duration.toMillis(), createDto.name(), throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * Updates an existing project
     *
     * @param id the project ID to update
     * @param updateDto the project update data
     * @param currentUserId the ID of the user performing the update
     * @return Uni containing the updated ProjectDetailsDto
     * @throws ApiException with 404 status if project not found, 409 if name conflict, 403 if not owner
     */
    public Uni<ProjectDetailsDto> update(String id, ProjectUpdateDto updateDto, String currentUserId) {
        Instant start = Instant.now();

        log.info("project.update.start - Updating project id={}, updatedBy={}, hasNameChange={}",
                id, currentUserId, updateDto.name() != null);

        return validateObjectId(id)
                .chain(objectId -> projectRepository.findById(objectId))
                .onItem().ifNull().failWith(Unchecked.supplier(() -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.warn("project.update.notFound - Project not found after {}ms, id={}", duration.toMillis(), id);
                    throw new ApiException("Projet non trouvé avec l'id: " + id, 404);
                }))
                .chain(projectEntity -> {
                    // Check if user is owner or has permission to update
                    if (!projectEntity.getOwnerId().equals(currentUserId)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.warn("project.update.forbidden - User not authorized after {}ms, id={}, ownerId={}, currentUser={}",
                                duration.toMillis(), id, projectEntity.getOwnerId(), currentUserId);
                        throw new ApiException("Vous n'êtes pas autorisé à modifier ce projet", 403);
                    }

                    String originalName = projectEntity.getName();

                    // Check name uniqueness if name is being changed
                    if (updateDto.name() != null && !updateDto.name().equals(originalName)) {
                        log.debug("project.update.nameCheck - Checking name uniqueness, newName={}, originalName={}",
                                updateDto.name(), originalName);

                        return projectRepository.existsByNameAndIdNot(updateDto.name(), id)
                                .chain(Unchecked.function(exists -> {
                                    if (Boolean.TRUE.equals(exists)) {
                                        Duration duration = Duration.between(start, Instant.now());
                                        log.warn("project.update.nameConflict - Name conflict after {}ms, name={}, id={}",
                                                duration.toMillis(), updateDto.name(), id);
                                        throw new ApiException("Un projet existe déjà avec le nom: " + updateDto.name(), 409);
                                    }
                                    return performUpdate(projectEntity, updateDto, currentUserId, start, originalName);
                                }));
                    }
                    return performUpdate(projectEntity, updateDto, currentUserId, start, originalName);
                })
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("project.update.error - Unexpected error after {}ms, id={}, error={}",
                                duration.toMillis(), id, throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * Performs the actual project update operation
     */
    private Uni<ProjectDetailsDto> performUpdate(Project project, ProjectUpdateDto updateDto, String currentUserId,
                                                 Instant start, String originalName) {
        log.debug("project.update.mapping - Applying updates to project entity, id={}", project.getIdAsString());

        // Apply updates using mapper
        projectMapper.updateEntity(project, updateDto);

        // Set audit fields
        project.preUpdate(currentUserId);

        return project.update()
                .map(v -> {
                    ProjectDetailsDto result = projectMapper.toDetailsDto(project);

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.update.success - Project updated in {}ms, id={}, originalName={}, newName={}, status={}",
                            duration.toMillis(), result.id(), originalName, result.name(), result.status());

                    return result;
                });
    }

    /**
     * Changes the status of a project
     *
     * @param id the project ID
     * @param newStatus the new status
     * @param currentUserId the ID of the user performing the change
     * @return Uni containing the updated ProjectDetailsDto
     * @throws ApiException with 404 if project not found, 403 if not authorized
     */
    public Uni<ProjectDetailsDto> changeStatus(String id, ProjectStatus newStatus, String currentUserId) {
        Instant start = Instant.now();

        log.info("project.changeStatus.start - Changing project status id={}, newStatus={}, changedBy={}",
                id, newStatus, currentUserId);

        return validateObjectId(id)
                .chain(objectId -> projectRepository.findById(objectId))
                .onItem().ifNull().failWith(Unchecked.supplier(() -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.warn("project.changeStatus.notFound - Project not found after {}ms, id={}", duration.toMillis(), id);
                    throw new ApiException("Projet non trouvé avec l'id: " + id, 404);
                }))
                .chain(projectEntity -> {
                    // Check if user is owner or has permission
                    if (!projectEntity.getOwnerId().equals(currentUserId)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.warn("project.changeStatus.forbidden - User not authorized after {}ms, id={}, ownerId={}, currentUser={}",
                                duration.toMillis(), id, projectEntity.getOwnerId(), currentUserId);
                        throw new ApiException("Vous n'êtes pas autorisé à modifier le statut de ce projet", 403);
                    }

                    ProjectStatus oldStatus = projectEntity.getStatus();
                    projectEntity.setStatus(newStatus);
                    projectEntity.preUpdate(currentUserId);

                    log.debug("project.changeStatus.updating - Updating project status, id={}, oldStatus={}, newStatus={}",
                            id, oldStatus, newStatus);

                    return projectEntity.update()
                            .map(v -> {
                                ProjectDetailsDto result = projectMapper.toDetailsDto(projectEntity);

                                Duration duration = Duration.between(start, Instant.now());
                                log.info("project.changeStatus.success - Status changed in {}ms, id={}, oldStatus={}, newStatus={}",
                                        duration.toMillis(), id, oldStatus, newStatus);

                                return result;
                            });
                })
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("project.changeStatus.error - Unexpected error after {}ms, id={}, newStatus={}, error={}",
                                duration.toMillis(), id, newStatus, throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * Deletes a project by ID
     *
     * @param id the project ID to delete
     * @param currentUserId the ID of the user performing the deletion
     * @return Uni<Void> when deletion is complete
     * @throws ApiException with 404 status if project not found, 403 if not owner
     */
    public Uni<Void> delete(String id, String currentUserId) {
        Instant start = Instant.now();

        log.info("project.delete.start - Deleting project id={}, deletedBy={}", id, currentUserId);

        return validateObjectId(id)
                .chain(objectId -> projectRepository.findById(objectId))
                .onItem().ifNull().failWith(Unchecked.supplier(() -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.warn("project.delete.notFound - Project not found after {}ms, id={}", duration.toMillis(), id);
                    throw new ApiException("Projet non trouvé avec l'id: " + id, 404);
                }))
                .chain(projectEntity -> {
                    // Check if user is owner
                    if (!projectEntity.getOwnerId().equals(currentUserId)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.warn("project.delete.forbidden - User not authorized after {}ms, id={}, ownerId={}, currentUser={}",
                                duration.toMillis(), id, projectEntity.getOwnerId(), currentUserId);
                        throw new ApiException("Vous n'êtes pas autorisé à supprimer ce projet", 403);
                    }

                    String projectName = projectEntity.getName();

                    log.debug("project.delete.executing - Executing delete operation, id={}, name={}", id, projectName);

                    return projectEntity.delete()
                            .invoke(() -> {
                                Duration duration = Duration.between(start, Instant.now());
                                log.info("project.delete.success - Project deleted in {}ms, id={}, name={}",
                                        duration.toMillis(), id, projectName);
                            });
                })
                .replaceWithVoid()
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("project.delete.error - Unexpected error after {}ms, id={}, error={}",
                                duration.toMillis(), id, throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * Finds projects owned by a specific user
     *
     * @param ownerId the owner ID
     * @return Uni containing list of ProjectListDto
     */
    public Uni<List<ProjectListDto>> findByOwnerId(String ownerId) {
        Instant start = Instant.now();

        log.info("project.findByOwnerId.start - Searching projects for owner={}", ownerId);

        return projectRepository.findByOwnerId(ownerId)
                .map(projects -> {
                    List<ProjectListDto> result = projects.stream()
                            .map(projectMapper::toListDto)
                            .toList();

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.findByOwnerId.success - Found {} projects in {}ms for owner={}",
                            result.size(), duration.toMillis(), ownerId);

                    return result;
                })
                .onFailure().invoke(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.findByOwnerId.error - Error after {}ms, ownerId={}, error={}",
                            duration.toMillis(), ownerId, throwable.getMessage(), throwable);
                });
    }

    /**
     * Gets project statistics for an owner
     *
     * @param ownerId the owner ID
     * @return Uni containing project statistics
     */
    public Uni<ProjectStats> getProjectStats(String ownerId) {
        Instant start = Instant.now();

        log.info("project.getStats.start - Computing project statistics for owner={}", ownerId);

        return projectRepository.getProjectStatsByOwner(ownerId)
                .invoke(stats -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.getStats.success - Statistics computed in {}ms for owner={}, total={}, active={}, draft={}, completed={}",
                            duration.toMillis(), ownerId, stats.getTotalProjects(), stats.getActiveProjects(),
                            stats.getDraftProjects(), stats.getCompletedProjects());
                })
                .onFailure().invoke(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.getStats.error - Error after {}ms, ownerId={}, error={}",
                            duration.toMillis(), ownerId, throwable.getMessage(), throwable);
                });
    }

    /**
     * Validates and converts string ID to ObjectId
     *
     * @param id the string ID to validate
     * @return Uni containing valid ObjectId
     * @throws ApiException with 400 status if ID format is invalid
     */
    private Uni<ObjectId> validateObjectId(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            return Uni.createFrom().item(objectId);
        } catch (IllegalArgumentException e) {
            log.warn("project.validateId.invalid - Invalid ObjectId format, id={}, error={}", id, e.getMessage());
            throw new ApiException("Format d'ID invalide: " + id, 400);
        }
    }

    /**
     * Checks if a project exists by name
     *
     * @param name the name to check
     * @return Uni containing boolean result
     */
    public Uni<Boolean> existsByName(String name) {
        Instant start = Instant.now();

        log.debug("project.existsByName.start - Checking name existence, name={}", name);

        return projectRepository.existsByName(name)
                .invoke(exists -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.debug("project.existsByName.result - Name check completed in {}ms, name={}, exists={}",
                            duration.toMillis(), name, exists);
                })
                .onFailure().invoke(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.existsByName.error - Error checking name after {}ms, name={}, error={}",
                            duration.toMillis(), name, throwable.getMessage(), throwable);
                });
    }
}