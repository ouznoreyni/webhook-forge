package sn.noreyni.project;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;
import sn.noreyni.common.response.ApiResponse;
import sn.noreyni.project.dto.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * REST Resource for Project management operations
 * Provides comprehensive CRUD operations with reactive programming support
 */
@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Project Management", description = "Operations related to project management")
@Slf4j
public class ProjectResource {

    @Inject
    ProjectService projectService;

    /**
     * Retrieves a paginated list of projects with optional filters
     *
     * @param name Filter by project name (partial match)
     * @param status Filter by project status
     * @param visibility Filter by project visibility
     * @param type Filter by project type
     * @param ownerId Filter by owner ID
     * @param page the page number (1-based, default: 1)
     * @param size the number of items per page (default: 10, max: 100)
     * @param sortBy Field to sort by (default: "name")
     * @param sortDirection Sort direction ("asc" or "desc", default: "asc")
     * @return ApiResponse containing paginated list of projects
     */
    @GET
    @Operation(
            summary = "Get paginated list of projects",
            description = "Retrieves a paginated list of projects with optional filtering and sorting capabilities"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<List<ProjectListDto>>> getAllProjects(
            @Parameter(description = "Filter by project name (partial match)")
            @QueryParam("name") String name,

            @Parameter(description = "Filter by project status")
            @QueryParam("status") ProjectStatus status,

            @Parameter(description = "Filter by project visibility")
            @QueryParam("visibility") Visibility visibility,

            @Parameter(description = "Filter by project type")
            @QueryParam("type") ProjectType type,

            @Parameter(description = "Filter by owner ID")
            @QueryParam("ownerId") String ownerId,

            @Parameter(
                    description = "Page number (1-based)",
                    schema = @Schema(type = SchemaType.INTEGER, minimum = "1", defaultValue = "1")
            )
            @QueryParam("page") @DefaultValue("1") @Min(value = 1, message = "Le numéro de page doit être supérieur ou égal à 1") int page,

            @Parameter(
                    description = "Number of items per page",
                    schema = @Schema(type = SchemaType.INTEGER, minimum = "1", maximum = "100", defaultValue = "10")
            )
            @QueryParam("size") @DefaultValue("10") @Min(value = 1, message = "La taille de page doit être supérieure à 0") int size,

            @Parameter(description = "Field to sort by")
            @QueryParam("sortBy") @DefaultValue("name") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)")
            @QueryParam("sortDirection") @DefaultValue("asc") String sortDirection) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("project.resource.findAll.start - requestId={}, page={}, size={}, filters=[name={}, status={}, visibility={}, type={}, ownerId={}]",
                requestId, page, size, name, status, visibility, type, ownerId);

        // Validate page size limit
        if (size > 100) {
            Duration duration = Duration.between(start, Instant.now());
            log.warn("project.resource.findAll.invalidSize - requestId={}, size={}, duration={}ms",
                    requestId, size, duration.toMillis());
            return Uni.createFrom().item(
                    ApiResponse.error("La taille de page ne peut pas dépasser 100 éléments")
            );
        }

        // Convert 1-based page to 0-based for internal processing
        int zeroBasedPage = page - 1;

        return Uni.combine().all()
                .unis(
                        projectService.findAll(name, status, visibility, type, ownerId, zeroBasedPage, size, sortBy, sortDirection),
                        projectService.getPaginationMeta(name, status, visibility, type, ownerId, page, size)
                )
                .asTuple()
                .map(tuple -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.findAll.success - requestId={}, count={}, duration={}ms",
                            requestId, tuple.getItem1().size(), duration.toMillis());

                    return ApiResponse.success(tuple.getItem1(), tuple.getItem2());
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.resource.findAll.error - requestId={}, duration={}ms, error={}",
                            requestId, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération des projets");
                });
    }

    /**
     * Retrieves a project by its unique identifier
     *
     * @param id the project ID
     * @return ApiResponse containing project details if found
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get project by ID",
            description = "Retrieves detailed information about a specific project"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Project found successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ProjectDetailsDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid project ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<ProjectDetailsDto>> getProjectById(
            @Parameter(
                    description = "Project unique identifier",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, pattern = "^[0-9a-fA-F]{24}$")
            )
            @PathParam("id") @NotBlank(message = "L'ID du projet est requis") String id) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("project.resource.findById.start - requestId={}, projectId={}", requestId, id);

        return projectService.findById(id)
                .map(project -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.findById.success - requestId={}, projectId={}, name={}, duration={}ms",
                            requestId, id, project.name(), duration.toMillis());

                    return ApiResponse.success(project);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());

                    if (throwable instanceof sn.noreyni.common.exception.ApiException apiEx) {
                        log.warn("project.resource.findById.apiError - requestId={}, projectId={}, statusCode={}, duration={}ms, message={}",
                                requestId, id, apiEx.getStatusCode(), duration.toMillis(), apiEx.getMessage());
                        return ApiResponse.error(apiEx.getMessage());
                    }

                    log.error("project.resource.findById.error - requestId={}, projectId={}, duration={}ms, error={}",
                            requestId, id, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération du projet");
                });
    }

    /**
     * Creates a new project in the system
     *
     * @param createDto the project creation data
     * @return ApiResponse containing the created project details
     */
    @POST
    @Operation(
            summary = "Create new project",
            description = "Creates a new project with the provided information"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Project created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ProjectDetailsDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "Project with name already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<Response> createProject(
            @Parameter(
                    description = "Project creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProjectCreateDto.class))
            )
            @Valid ProjectCreateDto createDto) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("project.resource.create.start - requestId={}, name={}, type={}",
                requestId, createDto.name(), createDto.type());

        String currentUserId = getCurrentUserId();

        return projectService.create(createDto, currentUserId)
                .map(project -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.create.success - requestId={}, projectId={}, name={}, duration={}ms",
                            requestId, project.id(), project.name(), duration.toMillis());

                    ApiResponse<ProjectDetailsDto> response = ApiResponse.success("Projet créé avec succès", project);
                    return Response.status(Response.Status.CREATED).entity(response).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());

                    if (throwable instanceof sn.noreyni.common.exception.ApiException apiEx) {
                        log.warn("project.resource.create.apiError - requestId={}, name={}, statusCode={}, duration={}ms, message={}",
                                requestId, createDto.name(), apiEx.getStatusCode(), duration.toMillis(), apiEx.getMessage());

                        Response.Status status = apiEx.getStatusCode() == 409 ?
                                Response.Status.CONFLICT : Response.Status.BAD_REQUEST;

                        return Response.status(status)
                                .entity(ApiResponse.error(apiEx.getMessage()))
                                .build();
                    }

                    log.error("project.resource.create.error - requestId={}, name={}, duration={}ms, error={}",
                            requestId, createDto.name(), duration.toMillis(), throwable.getMessage(), throwable);

                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(ApiResponse.error("Erreur lors de la création du projet"))
                            .build();
                });
    }

    /**
     * Updates an existing project
     *
     * @param id the project ID to update
     * @param updateDto the project update data
     * @return ApiResponse containing the updated project details
     */
    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Update project",
            description = "Updates an existing project with the provided information"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Project updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ProjectDetailsDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid input data or ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "Not authorized to update this project",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "Project name already exists for another project",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<ProjectDetailsDto>> updateProject(
            @Parameter(
                    description = "Project unique identifier",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, pattern = "^[0-9a-fA-F]{24}$")
            )
            @PathParam("id") @NotBlank(message = "L'ID du projet est requis") String id,

            @Parameter(
                    description = "Project update data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProjectUpdateDto.class))
            )
            @Valid ProjectUpdateDto updateDto) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("project.resource.update.start - requestId={}, projectId={}, hasNameChange={}",
                requestId, id, updateDto.name() != null);

        String currentUserId = getCurrentUserId();

        return projectService.update(id, updateDto, currentUserId)
                .map(project -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.update.success - requestId={}, projectId={}, name={}, duration={}ms",
                            requestId, id, project.name(), duration.toMillis());

                    return ApiResponse.success("Projet mis à jour avec succès", project);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());

                    if (throwable instanceof sn.noreyni.common.exception.ApiException apiEx) {
                        log.warn("project.resource.update.apiError - requestId={}, projectId={}, statusCode={}, duration={}ms, message={}",
                                requestId, id, apiEx.getStatusCode(), duration.toMillis(), apiEx.getMessage());
                        return ApiResponse.error(apiEx.getMessage());
                    }

                    log.error("project.resource.update.error - requestId={}, projectId={}, duration={}ms, error={}",
                            requestId, id, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la mise à jour du projet");
                });
    }

    /**
     * Changes the status of a project
     *
     * @param id the project ID
     * @param newStatus the new status to set
     * @return ApiResponse containing the updated project details
     */
    @PUT
    @Path("/{id}/status")
    @Operation(
            summary = "Change project status",
            description = "Changes the status of an existing project"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Project status changed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ProjectDetailsDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid project ID format or status",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "Not authorized to change status of this project",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<ProjectDetailsDto>> changeProjectStatus(
            @Parameter(
                    description = "Project unique identifier",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, pattern = "^[0-9a-fA-F]{24}$")
            )
            @PathParam("id") @NotBlank(message = "L'ID du projet est requis") String id,

            @Parameter(
                    description = "New project status",
                    required = true
            )
            @QueryParam("status") ProjectStatus newStatus) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("project.resource.changeStatus.start - requestId={}, projectId={}, newStatus={}",
                requestId, id, newStatus);

        if (newStatus == null) {
            Duration duration = Duration.between(start, Instant.now());
            log.warn("project.resource.changeStatus.missingStatus - requestId={}, projectId={}, duration={}ms",
                    requestId, id, duration.toMillis());
            return Uni.createFrom().item(ApiResponse.error("Le nouveau statut est requis"));
        }

        String currentUserId = getCurrentUserId();

        return projectService.changeStatus(id, newStatus, currentUserId)
                .map(project -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.changeStatus.success - requestId={}, projectId={}, newStatus={}, duration={}ms",
                            requestId, id, newStatus, duration.toMillis());

                    return ApiResponse.success("Statut du projet modifié avec succès", project);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());

                    if (throwable instanceof sn.noreyni.common.exception.ApiException apiEx) {
                        log.warn("project.resource.changeStatus.apiError - requestId={}, projectId={}, statusCode={}, duration={}ms, message={}",
                                requestId, id, apiEx.getStatusCode(), duration.toMillis(), apiEx.getMessage());
                        return ApiResponse.error(apiEx.getMessage());
                    }

                    log.error("project.resource.changeStatus.error - requestId={}, projectId={}, duration={}ms, error={}",
                            requestId, id, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors du changement de statut du projet");
                });
    }

    /**
     * Deletes a project by ID
     *
     * @param id the project ID to delete
     * @return ApiResponse confirming deletion
     */
    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Delete project",
            description = "Deletes a project from the system. This operation is irreversible."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Project deleted successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid project ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "Not authorized to delete this project",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<Void>> deleteProject(
            @Parameter(
                    description = "Project unique identifier",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, pattern = "^[0-9a-fA-F]{24}$")
            )
            @PathParam("id") @NotBlank(message = "L'ID du projet est requis") String id) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("project.resource.delete.start - requestId={}, projectId={}", requestId, id);

        String currentUserId = getCurrentUserId();

        return projectService.delete(id, currentUserId)
                .map(v -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.delete.success - requestId={}, projectId={}, duration={}ms",
                            requestId, id, duration.toMillis());

                    return ApiResponse.success("Projet supprimé avec succès", (Void) null);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());

                    if (throwable instanceof sn.noreyni.common.exception.ApiException apiEx) {
                        log.warn("project.resource.delete.apiError - requestId={}, projectId={}, statusCode={}, duration={}ms, message={}",
                                requestId, id, apiEx.getStatusCode(), duration.toMillis(), apiEx.getMessage());
                        return ApiResponse.error(apiEx.getMessage());
                    }

                    log.error("project.resource.delete.error - requestId={}, projectId={}, duration={}ms, error={}",
                            requestId, id, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la suppression du projet");
                });
    }

    /**
     * Gets projects owned by a specific user
     *
     * @param ownerId the owner ID
     * @return ApiResponse containing list of projects
     */
    @GET
    @Path("/owner/{ownerId}")
    @Operation(
            summary = "Get projects by owner",
            description = "Retrieves all projects owned by a specific user"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid owner ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<List<ProjectListDto>>> getProjectsByOwner(
            @Parameter(
                    description = "Owner unique identifier",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, pattern = "^[0-9a-fA-F]{24}$")
            )
            @PathParam("ownerId") @NotBlank(message = "L'ID du propriétaire est requis") String ownerId) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("project.resource.findByOwner.start - requestId={}, ownerId={}", requestId, ownerId);

        return projectService.findByOwnerId(ownerId)
                .map(projects -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.findByOwner.success - requestId={}, ownerId={}, count={}, duration={}ms",
                            requestId, ownerId, projects.size(), duration.toMillis());

                    return ApiResponse.success(projects);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.resource.findByOwner.error - requestId={}, ownerId={}, duration={}ms, error={}",
                            requestId, ownerId, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération des projets");
                });
    }

    /**
     * Checks if a project name is available
     *
     * @param name the project name to check
     * @return ApiResponse containing availability status
     */
    @GET
    @Path("/check-name")
    @Operation(
            summary = "Check project name availability",
            description = "Checks if a project name is available"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Name availability check completed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid name format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<Boolean>> checkNameAvailability(
            @Parameter(
                    description = "Project name to check",
                    required = true
            )
            @QueryParam("name") @NotBlank(message = "Le nom du projet est requis") String name) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("project.resource.checkName.start - requestId={}, name={}", requestId, name);

        return projectService.existsByName(name)
                .map(exists -> {
                    Duration duration = Duration.between(start, Instant.now());
                    boolean available = !exists;

                    log.info("project.resource.checkName.success - requestId={}, name={}, available={}, duration={}ms",
                            requestId, name, available, duration.toMillis());

                    String message = available ? "Nom de projet disponible" : "Nom de projet déjà utilisé";
                    return ApiResponse.success(message, available);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.resource.checkName.error - requestId={}, name={}, duration={}ms, error={}",
                            requestId, name, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la vérification du nom du projet");
                });
    }

    /**
     * Gets project statistics for an owner
     *
     * @param ownerId the owner ID
     * @return ApiResponse containing project statistics
     */
    @GET
    @Path("/stats/{ownerId}")
    @Operation(
            summary = "Get project statistics for owner",
            description = "Retrieves various project statistics for a specific owner"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid owner ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<ProjectStats>> getProjectStats(
            @Parameter(
                    description = "Owner unique identifier",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, pattern = "^[0-9a-fA-F]{24}$")
            )
            @PathParam("ownerId") @NotBlank(message = "L'ID du propriétaire est requis") String ownerId) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("project.resource.stats.start - requestId={}, ownerId={}", requestId, ownerId);

        return projectService.getProjectStats(ownerId)
                .map(stats -> {
                    Duration duration = Duration.between(start, Instant.now());

                    log.info("project.resource.stats.success - requestId={}, ownerId={}, total={}, active={}, draft={}, completed={}, duration={}ms",
                            requestId, ownerId, stats.getTotalProjects(), stats.getActiveProjects(),
                            stats.getDraftProjects(), stats.getCompletedProjects(), duration.toMillis());

                    return ApiResponse.success(stats);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.resource.stats.error - requestId={}, ownerId={}, duration={}ms, error={}",
                            requestId, ownerId, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération des statistiques du projet");
                });
    }

    /**
     * Gets projects accessible by the current user (owned, member, or public)
     *
     * @return ApiResponse containing list of accessible projects
     */
    @GET
    @Path("/accessible")
    @Operation(
            summary = "Get accessible projects",
            description = "Retrieves all projects accessible by the current user (owned, member, or public)"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Accessible projects retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<List<ProjectListDto>>> getAccessibleProjects() {
        Instant start = Instant.now();
        String requestId = generateRequestId();
        String currentUserId = getCurrentUserId();

        log.info("project.resource.accessible.start - requestId={}, userId={}", requestId, currentUserId);

        // Use the search method with ownerId filter to get owned projects
        // In a real implementation, you might want to add a specific method for accessible projects
        return projectService.findByOwnerId(currentUserId)
                .map(projects -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.accessible.success - requestId={}, userId={}, count={}, duration={}ms",
                            requestId, currentUserId, projects.size(), duration.toMillis());

                    return ApiResponse.success(projects);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.resource.accessible.error - requestId={}, userId={}, duration={}ms, error={}",
                            requestId, currentUserId, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération des projets accessibles");
                });
    }

    /**
     * Gets my projects (shortcut for current user's projects)
     *
     * @return ApiResponse containing list of user's projects
     */
    @GET
    @Path("/my")
    @Operation(
            summary = "Get my projects",
            description = "Retrieves all projects owned by the current user"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User projects retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<List<ProjectListDto>>> getMyProjects() {
        Instant start = Instant.now();
        String requestId = generateRequestId();
        String currentUserId = getCurrentUserId();

        log.info("project.resource.my.start - requestId={}, userId={}", requestId, currentUserId);

        return projectService.findByOwnerId(currentUserId)
                .map(projects -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.my.success - requestId={}, userId={}, count={}, duration={}ms",
                            requestId, currentUserId, projects.size(), duration.toMillis());

                    return ApiResponse.success(projects);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.resource.my.error - requestId={}, userId={}, duration={}ms, error={}",
                            requestId, currentUserId, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération de vos projets");
                });
    }

    /**
     * Gets my project statistics
     *
     * @return ApiResponse containing current user's project statistics
     */
    @GET
    @Path("/my/stats")
    @Operation(
            summary = "Get my project statistics",
            description = "Retrieves project statistics for the current user"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<ProjectStats>> getMyProjectStats() {
        Instant start = Instant.now();
        String requestId = generateRequestId();
        String currentUserId = getCurrentUserId();

        log.info("project.resource.myStats.start - requestId={}, userId={}", requestId, currentUserId);

        return projectService.getProjectStats(currentUserId)
                .map(stats -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("project.resource.myStats.success - requestId={}, userId={}, total={}, active={}, draft={}, completed={}, duration={}ms",
                            requestId, currentUserId, stats.getTotalProjects(), stats.getActiveProjects(),
                            stats.getDraftProjects(), stats.getCompletedProjects(), duration.toMillis());

                    return ApiResponse.success(stats);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("project.resource.myStats.error - requestId={}, userId={}, duration={}ms, error={}",
                            requestId, currentUserId, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération de vos statistiques de projet");
                });
    }

    /**
     * Generates a unique request ID for tracing
     *
     * @return unique request identifier
     */
    private String generateRequestId() {
        return "req_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * Gets the current user ID from security context
     * In production, this should extract from JWT token or security context
     *
     * @return current user ID
     */
    private String getCurrentUserId() {
        // TODO: Implement proper security context extraction
        // Example: return SecurityContext.getCurrentUser().getId();
        return "system"; // Placeholder for development
    }
}