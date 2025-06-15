package sn.noreyni.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import sn.noreyni.user.dto.UserCreateDto;
import sn.noreyni.user.dto.UserDetailsDto;
import sn.noreyni.user.dto.UserListDto;
import sn.noreyni.user.dto.UserUpdateDto;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User mapper for converting between entities and DTOs
 * Uses manual mapping for records and ModelMapper for entities
 *
 * @author Noreyni Team
 * @version 1.0
 * @since 2025
 */
@ApplicationScoped
public class UserMapper {

    @Inject
    ModelMapper modelMapper;

    /**
     * Converts User entity to UserListDto
     * Manual mapping because records don't have no-arg constructors
     *
     * @param user the user entity
     * @return UserListDto or null if input is null
     */
    public UserListDto toListDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserListDto(
                user.getIdAsString(), // Use BaseEntity helper method
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }

    /**
     * Converts User entity to UserDetailsDto
     * Manual mapping because records don't have no-arg constructors
     *
     * @param user the user entity
     * @return UserDetailsDto or null if input is null
     */
    public UserDetailsDto toDetailsDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDetailsDto(
                user.getIdAsString(), // Use BaseEntity helper method
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedBy(),
                user.getCreatedAt(),
                user.getUpdatedBy(),
                user.getUpdatedAt()
        );
    }

    /**
     * Converts UserCreateDto to User entity
     * Uses ModelMapper since User is a regular class
     *
     * @param dto the creation DTO
     * @return User entity or null if input is null
     */
    public User toEntity(UserCreateDto dto) {
        if (dto == null) {
            return null;
        }

        // ModelMapper works fine for mapping TO regular classes
        User user = modelMapper.map(dto, User.class);

        // Set defaults for fields not in DTO
        user.setActive(true);
        user.setOwnedProjectIds(new HashSet<>());
        user.setMemberProjectIds(new HashSet<>());
        user.setInvitedProjectIds(new HashSet<>());

        return user;
    }

    /**
     * Updates User entity with data from UserUpdateDto
     * Uses manual mapping to handle null values properly
     *
     * @param user the target user entity to update
     * @param dto  the update DTO with new values
     */
    public void updateEntity(UserUpdateDto dto, User user) {
        if (dto == null || user == null) {
            return;
        }

        // Manual mapping to handle null values correctly
        if (dto.firstName() != null) {
            user.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            user.setLastName(dto.lastName());
        }
        if (dto.email() != null) {
            user.setEmail(dto.email());
        }
        if (dto.role() != null) {
            user.setRole(dto.role());
        }

        // Always update the timestamp using BaseEntity method
        user.preUpdate();
    }

    /**
     * Alternative method for updating entity from update request
     * Provides same functionality as updateEntity for backward compatibility
     *
     * @param user    the target user entity to update
     * @param request the update DTO with new values
     */
    public void updateEntityFromUpdateRequest(User user, UserUpdateDto request) {
        updateEntity(request, user); // Delegate to main update method
    }

    /**
     * Converts list of User entities to list of UserListDto
     *
     * @param users the list of user entities
     * @return List of UserListDto or null if input is null
     */
    public List<UserListDto> toListDtoList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts list of User entities to list of UserDetailsDto
     *
     * @param users the list of user entities
     * @return List of UserDetailsDto or null if input is null
     */
    public List<UserDetailsDto> toDetailsDtoList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toDetailsDto)
                .collect(Collectors.toList());
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
}