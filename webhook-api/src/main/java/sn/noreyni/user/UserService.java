package sn.noreyni.user;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import sn.noreyni.common.exception.ApiException;
import sn.noreyni.common.response.PaginationMeta;
import sn.noreyni.user.dto.UserCreateDto;
import sn.noreyni.user.dto.UserDetailsDto;
import sn.noreyni.user.dto.UserListDto;
import sn.noreyni.user.dto.UserUpdateDto;

import java.time.Duration;
import java.time.Instant;
import java.util.List;


/**
 * Service class for managing user operations
 * Provides comprehensive CRUD operations with audit logging and error handling
 *
 */
@ApplicationScoped
@Slf4j
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    /**
     * Retrieves a paginated list of users
     *
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @return Uni containing list of UserListDto with pagination metadata
     * @apiNote This method logs the operation duration for monitoring purposes
     */
    public Uni<List<UserListDto>> findAll(int page, int size) {
        Instant start = Instant.now();

        log.info("user.findAll.start - Fetching users page={}, size={}", page, size);

        return userRepository.findAll()
                .page(Page.of(page, size))
                .list()
                .map(users -> {
                    List<UserListDto> result = users.stream()
                            .map(userMapper::toListDto)
                            .toList();

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.findAll.success - Retrieved {} users in {}ms, page={}, size={}",
                            result.size(), duration.toMillis(), page, size);

                    return result;
                })
                .onFailure().invoke(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("user.findAll.error - Failed to fetch users after {}ms, page={}, size={}, error={}",
                            duration.toMillis(), page, size, throwable.getMessage(), throwable);
                });
    }

    /**
     * Retrieves pagination metadata for user listing
     *
     * @param page the page number
     * @param size the page size
     * @return Uni containing pagination metadata
     */
    public Uni<PaginationMeta> getPaginationMeta(int page, int size) {
        Instant start = Instant.now();

        log.debug("user.pagination.start - Computing pagination meta for page={}, size={}", page, size);

        return userRepository.count()
                .map(totalElements -> {
                    PaginationMeta meta = PaginationMeta.of(page, size, totalElements);

                    Duration duration = Duration.between(start, Instant.now());
                    log.debug("user.pagination.success - Computed pagination meta in {}ms, totalElements={}, totalPages={}",
                            duration.toMillis(), totalElements, meta.totalPages());

                    return meta;
                })
                .onFailure().invoke(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("user.pagination.error - Failed to compute pagination meta after {}ms, error={}",
                            duration.toMillis(), throwable.getMessage(), throwable);
                });
    }

    /**
     * Finds a user by their unique identifier
     *
     * @param id the user ID as string
     * @return Uni containing UserDetailsDto if found
     * @throws ApiException with 404 status if user not found
     */
    public Uni<UserDetailsDto> findById(String id) {
        Instant start = Instant.now();

        log.info("user.findById.start - Searching user with id={}", id);

        return validateObjectId(id)
                .chain(objectId -> userRepository.findById(objectId))
                .onItem().ifNull().failWith(Unchecked.supplier(() -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.warn("user.findById.notFound - User not found after {}ms, id={}", duration.toMillis(), id);
                    throw new ApiException("Utilisateur non trouvé avec l'id: " + id, 404);
                }))
                .map(user -> {
                    UserDetailsDto result = userMapper.toDetailsDto(user);

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.findById.success - User found in {}ms, id={}, email={}",
                            duration.toMillis(), id, result.email());

                    return result;
                })
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("user.findById.error - Unexpected error after {}ms, id={}, error={}",
                                duration.toMillis(), id, throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * Creates a new user in the system
     *
     * @param createDto     the user creation data
     * @return Uni containing the created UserDetailsDto
     * @throws ApiException with 409 status if email already exists
     */
    public Uni<UserDetailsDto> create(UserCreateDto createDto) {
        Instant start = Instant.now();

        log.info("user.create.start - Creating user with email={}, role={}, createdBy={}",
                createDto.email(), createDto.role());

        return userRepository.existsByEmail(createDto.email())
                .chain(Unchecked.function(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.warn("user.create.conflict - Email already exists after {}ms, email={}",
                                duration.toMillis(), createDto.email());
                        throw new ApiException("Un utilisateur existe déjà avec l'email: " + createDto.email(), 409);
                    }

                    User user = userMapper.toEntity(createDto);

                    // Set audit fields using BaseEntity method
                    user.prePersist();

                    // TODO: Hash password in production
                    // user.setPassword(passwordEncoder.encode(user.getPassword()));

                    log.debug("user.create.persisting - Persisting user entity, email={}", createDto.email());

                    return user.persist();
                }))
                .map(user -> {
                    UserDetailsDto result = userMapper.toDetailsDto((User) user);

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.create.success - User created in {}ms, id={}, email={}, role={}",
                            duration.toMillis(), result.id(), result.email(), result.role());

                    return result;
                })
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("user.create.error - Failed to create user after {}ms, email={}, error={}",
                                duration.toMillis(), createDto.email(), throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * Updates an existing user
     *
     * @param id            the user ID to update
     * @param updateDto     the user update data
     * @param currentUserId the ID of the user performing the update
     * @return Uni containing the updated UserDetailsDto
     * @throws ApiException with 404 status if user not found, 409 if email conflict
     */
    public Uni<UserDetailsDto> update(String id, UserUpdateDto updateDto, String currentUserId) {
        Instant start = Instant.now();

        log.info("user.update.start - Updating user id={}, updatedBy={}, hasEmailChange={}",
                id, currentUserId, updateDto.email() != null);

        return validateObjectId(id)
                .chain(objectId -> userRepository.findById(objectId))
                .onItem().ifNull().failWith(Unchecked.supplier(() -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.warn("user.update.notFound - User not found after {}ms, id={}", duration.toMillis(), id);
                    throw new ApiException("Utilisateur non trouvé avec l'id: " + id, 404);
                }))
                .chain(userEntity -> {
                    String originalEmail = userEntity.getEmail();

                    // Check email uniqueness if email is being changed
                    if (updateDto.email() != null && !updateDto.email().equals(originalEmail)) {
                        log.debug("user.update.emailCheck - Checking email uniqueness, newEmail={}, originalEmail={}",
                                updateDto.email(), originalEmail);

                        return userRepository.existsByEmailAndIdNot(updateDto.email(), id)
                                .chain(Unchecked.function(exists -> {
                                    if (Boolean.TRUE.equals(exists)) {
                                        Duration duration = Duration.between(start, Instant.now());
                                        log.warn("user.update.emailConflict - Email conflict after {}ms, email={}, id={}",
                                                duration.toMillis(), updateDto.email(), id);
                                        throw new ApiException("Un utilisateur existe déjà avec l'email: " + updateDto.email(), 409);
                                    }
                                    return performUpdate(userEntity, updateDto, currentUserId, start, originalEmail);
                                }));
                    }
                    return performUpdate(userEntity, updateDto, currentUserId, start, originalEmail);
                })
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("user.update.error - Unexpected error after {}ms, id={}, error={}",
                                duration.toMillis(), id, throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * Performs the actual user update operation
     *
     * @param user          the user entity to update
     * @param updateDto     the update data
     * @param currentUserId the user performing the update
     * @param start         the operation start time
     * @param originalEmail the original email for logging
     * @return Uni containing the updated UserDetailsDto
     */
    private Uni<UserDetailsDto> performUpdate(User user, UserUpdateDto updateDto, String currentUserId,
                                              Instant start, String originalEmail) {
        log.debug("user.update.mapping - Applying updates to user entity, id={}", user.getIdAsString());

        // Apply updates using mapper
        userMapper.updateEntity(user, updateDto);

        // Set audit fields using BaseEntity method
       // user.setUpdateAudit(currentUserId);
        user.preUpdate();

        return user.update()
                .map(v -> {
                    UserDetailsDto result = userMapper.toDetailsDto(user);

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.update.success - User updated in {}ms, id={}, originalEmail={}, newEmail={}, role={}",
                            duration.toMillis(), result.id(), originalEmail, result.email(), result.role());

                    return result;
                });
    }

    /**
     * Soft deletes a user by ID
     * Note: Consider implementing soft delete instead of hard delete for audit purposes
     *
     * @param id the user ID to delete
     * @return Uni<Void> when deletion is complete
     * @throws ApiException with 404 status if user not found
     */
    public Uni<Void> delete(String id) {
        Instant start = Instant.now();

        log.info("user.delete.start - Deleting user id={}", id);

        return validateObjectId(id)
                .chain(objectId -> userRepository.findById(objectId))
                .onItem().ifNull().failWith(Unchecked.supplier(() -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.warn("user.delete.notFound - User not found after {}ms, id={}", duration.toMillis(), id);
                    throw new ApiException("Utilisateur non trouvé avec l'id: " + id, 404);
                }))
                .chain(userEntity -> {
                    String userEmail = (userEntity).getEmail();

                    log.debug("user.delete.executing - Executing delete operation, id={}, email={}", id, userEmail);

                    return (userEntity).delete()
                            .invoke(() -> {
                                Duration duration = Duration.between(start, Instant.now());
                                log.info("user.delete.success - User deleted in {}ms, id={}, email={}",
                                        duration.toMillis(), id, userEmail);
                            });
                })
                .replaceWithVoid()
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("user.delete.error - Unexpected error after {}ms, id={}, error={}",
                                duration.toMillis(), id, throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * Finds a user by their email address
     *
     * @param email the email address to search for
     * @return Uni containing UserDetailsDto if found
     * @throws ApiException with 404 status if user not found
     */
    private Uni<UserDetailsDto> findByEmail(String email) {
        Instant start = Instant.now();

        log.info("user.findByEmail.start - Searching user with email={}", email);

        return userRepository.findByEmail(email)
                .onItem().ifNull().failWith(Unchecked.supplier(() -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.warn("user.findByEmail.notFound - User not found after {}ms, email={}", duration.toMillis(), email);
                    throw new ApiException("Utilisateur non trouvé avec l'email: " + email, 404);
                }))
                .map(user -> {
                    UserDetailsDto result = userMapper.toDetailsDto(user);

                    Duration duration = Duration.between(start, Instant.now());
                    log.info("user.findByEmail.success - User found in {}ms, email={}, id={}",
                            duration.toMillis(), email, result.id());

                    return result;
                })
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof ApiException)) {
                        Duration duration = Duration.between(start, Instant.now());
                        log.error("user.findByEmail.error - Unexpected error after {}ms, email={}, error={}",
                                duration.toMillis(), email, throwable.getMessage(), throwable);
                    }
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
            log.warn("user.validateId.invalid - Invalid ObjectId format, id={}, error={}", id, e.getMessage());
            throw new ApiException("Format d'ID invalide: " + id, 400);
        }
    }

    /**
     * Checks if a user exists by email
     *
     * @param email the email to check
     * @return Uni containing boolean result
     */
    Uni<Boolean> existsByEmail(String email) {
        Instant start = Instant.now();

        log.debug("user.existsByEmail.start - Checking email existence, email={}", email);

        return userRepository.existsByEmail(email)
                .invoke(exists -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.debug("user.existsByEmail.result - Email check completed in {}ms, email={}, exists={}",
                            duration.toMillis(), email, exists);
                })
                .onFailure().invoke(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("user.existsByEmail.error - Error checking email after {}ms, email={}, error={}",
                            duration.toMillis(), email, throwable.getMessage(), throwable);
                });
    }

    /**
     * Gets the total count of active users
     *
     * @return Uni containing the count of active users
     */
    Uni<Long> getActiveUserCount() {
        Instant start = Instant.now();

        log.debug("user.activeCount.start - Counting active users");

        return userRepository.countActive()
                .invoke(count -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.debug("user.activeCount.success - Active user count retrieved in {}ms, count={}",
                            duration.toMillis(), count);
                })
                .onFailure().invoke(throwable -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("user.activeCount.error - Error counting active users after {}ms, error={}",
                            duration.toMillis(), throwable.getMessage(), throwable);
                });
    }
}