package sn.noreyni.user;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
import sn.noreyni.common.response.ApiResponse;
import sn.noreyni.user.dto.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * REST Resource for User management operations
 * Provides comprehensive CRUD operations with reactive programming support
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management", description = "Operations related to user management")
@Slf4j
public class UserResource {

    @Inject
    UserService userService;

    /**
     * Retrieves a paginated list of users
     *
     * @param page the page number (0-based, default: 0)
     * @param size the number of items per page (default: 10, max: 100)
     * @return ApiResponse containing paginated list of users
     */
    @GET
    @Operation(
            summary = "Get paginated list of users",
            description = "Retrieves a paginated list of users with optional filtering capabilities"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
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
    public Uni<ApiResponse<List<UserListDto>>> getAllUsers(
            @Parameter(
                    description = "Page number (1-based)",
                    schema = @Schema(type = SchemaType.INTEGER, minimum = "1", defaultValue = "1")
            )
            @QueryParam("page") @DefaultValue("1") @Min(value = 1, message = "Le numéro de page doit être supérieur ou égal à 1") int page,

            @Parameter(
                    description = "Number of items per page",
                    schema = @Schema(type = SchemaType.INTEGER, minimum = "1", maximum = "100", defaultValue = "10")
            )
            @QueryParam("size") @DefaultValue("10") @Min(value = 1, message = "La taille de page doit être supérieure à 0") int size) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("user.resource.findAll.start - requestId={}, page={}, size={}", requestId, page, size);

        // Validate page size limit
        if (size > 100) {
            Duration duration = Duration.between(start, Instant.now());
            log.warn("user.resource.findAll.invalidSize - requestId={}, size={}, duration={}ms",
                    requestId, size, duration.toMillis());
            return Uni.createFrom().item(
                    ApiResponse.error("La taille de page ne peut pas dépasser 100 éléments")
            );
        }
        // Convert 1-based page to 0-based for internal processing
        int zeroBasedPage = page - 1;

        return Uni.combine().all()
                .unis(
                        userService.findAll(zeroBasedPage, size),
                        userService.getPaginationMeta(page, size)
                )
                .asTuple()
                .map(tuple -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.resource.findAll.success - requestId={}, count={}, duration={}ms",
                            requestId, tuple.getItem1().size(), duration.toMillis());

                    return ApiResponse.success(tuple.getItem1(), tuple.getItem2());
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("user.resource.findAll.error - requestId={}, duration={}ms, error={}",
                            requestId, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération des utilisateurs");
                });
    }

    /**
     * Retrieves a user by their unique identifier
     *
     * @param id the user ID
     * @return ApiResponse containing user details if found
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves detailed information about a specific user"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User found successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = UserDetailsDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid user ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<UserDetailsDto>> getUserById(
            @Parameter(
                    description = "User unique identifier",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, pattern = "^[0-9a-fA-F]{24}$")
            )
            @PathParam("id") @NotBlank(message = "L'ID utilisateur est requis") String id) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("user.resource.findById.start - requestId={}, userId={}", requestId, id);

        return userService.findById(id)
                .map(user -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.resource.findById.success - requestId={}, userId={}, email={}, duration={}ms",
                            requestId, id, user.email(), duration.toMillis());

                    return ApiResponse.success(user);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());

                    if (throwable instanceof sn.noreyni.common.exception.ApiException apiEx) {
                        log.warn("user.resource.findById.apiError - requestId={}, userId={}, statusCode={}, duration={}ms, message={}",
                                requestId, id, apiEx.getStatusCode(), duration.toMillis(), apiEx.getMessage());
                        return ApiResponse.error(apiEx.getMessage());
                    }

                    log.error("user.resource.findById.error - requestId={}, userId={}, duration={}ms, error={}",
                            requestId, id, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération de l'utilisateur");
                });
    }

    /**
     * Creates a new user in the system
     *
     * @param createDto the user creation data
     * @return ApiResponse containing the created user details
     */
    @POST
    @Operation(
            summary = "Create new user",
            description = "Creates a new user account with the provided information"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = UserDetailsDto.class)
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
                    description = "User with email already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<Response> createUser(
            @Parameter(
                    description = "User creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserCreateDto.class))
            )
            @Valid UserCreateDto createDto) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("user.resource.create.start - requestId={}, email={}, role={}",
                requestId, createDto.email(), createDto.role());


        return userService.create(createDto)
                .map(user -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.resource.create.success - requestId={}, userId={}, email={}, duration={}ms",
                            requestId, user.id(), user.email(), duration.toMillis());

                    ApiResponse<UserDetailsDto> response = ApiResponse.success("Utilisateur créé avec succès", user);
                    return Response.status(Response.Status.CREATED).entity(response).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());

                    if (throwable instanceof sn.noreyni.common.exception.ApiException apiEx) {
                        log.warn("user.resource.create.apiError - requestId={}, email={}, statusCode={}, duration={}ms, message={}",
                                requestId, createDto.email(), apiEx.getStatusCode(), duration.toMillis(), apiEx.getMessage());

                        Response.Status status = apiEx.getStatusCode() == 409 ?
                                Response.Status.CONFLICT : Response.Status.BAD_REQUEST;

                        return Response.status(status)
                                .entity(ApiResponse.error(apiEx.getMessage()))
                                .build();
                    }

                    log.error("user.resource.create.error - requestId={}, email={}, duration={}ms, error={}",
                            requestId, createDto.email(), duration.toMillis(), throwable.getMessage(), throwable);

                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(ApiResponse.error("Erreur lors de la création de l'utilisateur"))
                            .build();
                });
    }

    /**
     * Updates an existing user
     *
     * @param id the user ID to update
     * @param updateDto the user update data
     * @return ApiResponse containing the updated user details
     */
    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Update user",
            description = "Updates an existing user with the provided information"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = UserDetailsDto.class)
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
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "Email already exists for another user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<UserDetailsDto>> updateUser(
            @Parameter(
                    description = "User unique identifier",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, pattern = "^[0-9a-fA-F]{24}$")
            )
            @PathParam("id") @NotBlank(message = "L'ID utilisateur est requis") String id,

            @Parameter(
                    description = "User update data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserUpdateDto.class))
            )
            @Valid UserUpdateDto updateDto) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("user.resource.update.start - requestId={}, userId={}, hasEmailChange={}",
                requestId, id, updateDto.email() != null);

        // In production, get current user from security context
        String currentUserId = getCurrentUserId();

        return userService.update(id, updateDto, currentUserId)
                .map(user -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.resource.update.success - requestId={}, userId={}, email={}, duration={}ms",
                            requestId, id, user.email(), duration.toMillis());

                    return ApiResponse.success("Utilisateur mis à jour avec succès", user);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());

                    if (throwable instanceof sn.noreyni.common.exception.ApiException apiEx) {
                        log.warn("user.resource.update.apiError - requestId={}, userId={}, statusCode={}, duration={}ms, message={}",
                                requestId, id, apiEx.getStatusCode(), duration.toMillis(), apiEx.getMessage());
                        return ApiResponse.error(apiEx.getMessage());
                    }

                    log.error("user.resource.update.error - requestId={}, userId={}, duration={}ms, error={}",
                            requestId, id, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la mise à jour de l'utilisateur");
                });
    }

    /**
     * Deletes a user by ID
     *
     * @param id the user ID to delete
     * @return ApiResponse confirming deletion
     */
    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Delete user",
            description = "Deletes a user from the system. This operation is irreversible."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User deleted successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid user ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<Void>> deleteUser(
            @Parameter(
                    description = "User unique identifier",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, pattern = "^[0-9a-fA-F]{24}$")
            )
            @PathParam("id") @NotBlank(message = "L'ID utilisateur est requis") String id) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("user.resource.delete.start - requestId={}, userId={}", requestId, id);

        return userService.delete(id)
                .map(v -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.resource.delete.success - requestId={}, userId={}, duration={}ms",
                            requestId, id, duration.toMillis());

                    return ApiResponse.success("Utilisateur supprimé avec succès", (Void) null);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());

                    if (throwable instanceof sn.noreyni.common.exception.ApiException apiEx) {
                        log.warn("user.resource.delete.apiError - requestId={}, userId={}, statusCode={}, duration={}ms, message={}",
                                requestId, id, apiEx.getStatusCode(), duration.toMillis(), apiEx.getMessage());
                        return ApiResponse.error(apiEx.getMessage());
                    }

                    log.error("user.resource.delete.error - requestId={}, userId={}, duration={}ms, error={}",
                            requestId, id, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la suppression de l'utilisateur");
                });
    }



    /**
     * Checks if an email address is available
     *
     * @param email the email address to check
     * @return ApiResponse containing availability status
     */
    @GET
    @Path("/check-email")
    @Operation(
            summary = "Check email availability",
            description = "Checks if an email address is available for registration"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Email availability check completed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid email format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public Uni<ApiResponse<Boolean>> checkEmailAvailability(
            @Parameter(
                    description = "Email address to check",
                    required = true,
                    schema = @Schema(type = SchemaType.STRING, format = "email")
            )
            @QueryParam("email") @NotBlank(message = "L'email est requis") @Email(message = "Format d'email invalide") String email) {

        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("user.resource.checkEmail.start - requestId={}, email={}", requestId, email);

        return userService.existsByEmail(email)
                .map(exists -> {
                    Duration duration = Duration.between(start, Instant.now());
                    boolean available = !exists;

                    log.info("user.resource.checkEmail.success - requestId={}, email={}, available={}, duration={}ms",
                            requestId, email, available, duration.toMillis());

                    String message = available ? "Email disponible" : "Email déjà utilisé";
                    return ApiResponse.success(message, available);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("user.resource.checkEmail.error - requestId={}, email={}, duration={}ms, error={}",
                            requestId, email, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la vérification de l'email");
                });
    }

    /**
     * Gets user statistics
     *
     * @return ApiResponse containing user statistics
     */
    @GET
    @Path("/stats")
    @Operation(
            summary = "Get user statistics",
            description = "Retrieves various user statistics including active user count"
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
    public Uni<ApiResponse<UserStatsDto>> getUserStats() {
        Instant start = Instant.now();
        String requestId = generateRequestId();

        log.info("user.resource.stats.start - requestId={}", requestId);

        return userService.getActiveUserCount()
                .map(activeCount -> {
                    Duration duration = Duration.between(start, Instant.now());

                    UserStatsDto stats = new UserStatsDto(activeCount);

                    log.info("user.resource.stats.success - requestId={}, activeCount={}, duration={}ms",
                            requestId, activeCount, duration.toMillis());

                    return ApiResponse.success(stats);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("user.resource.stats.error - requestId={}, duration={}ms, error={}",
                            requestId, duration.toMillis(), throwable.getMessage(), throwable);

                    return ApiResponse.error("Erreur lors de la récupération des statistiques");
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