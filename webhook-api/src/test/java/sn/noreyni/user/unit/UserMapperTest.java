package sn.noreyni.user.unit;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import sn.noreyni.common.enums.UserRole;
import sn.noreyni.user.User;
import sn.noreyni.user.UserMapper;
import sn.noreyni.user.dto.UserCreateDto;
import sn.noreyni.user.dto.UserDetailsDto;
import sn.noreyni.user.dto.UserListDto;
import sn.noreyni.user.dto.UserUpdateDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for UserMapper
 * Tests all mapping scenarios including edge cases and error conditions
 */
@QuarkusTest
@DisplayName("UserMapper Tests")
class UserMapperTest {

    @Inject
    UserMapper userMapper;

    private User sampleUser;
    private UserCreateDto sampleCreateDto;
    private UserUpdateDto sampleUpdateDto;

    @BeforeEach
    void setUp() {
        LocalDateTime testTime = LocalDateTime.now();

        // Create sample User entity
        sampleUser = new User();
        sampleUser.id = new ObjectId();
        sampleUser.setFirstName("Jean");
        sampleUser.setLastName("Dupont");
        sampleUser.setEmail("jean.dupont@example.com");
        sampleUser.setPassword("hashedPassword123");
        sampleUser.setRole(UserRole.MEMBER);
        sampleUser.setActive(true);
        sampleUser.setCreatedBy("admin");
        sampleUser.setCreatedAt(testTime.minusDays(1));
        sampleUser.setUpdatedBy("admin");
        sampleUser.setUpdatedAt(testTime);

        // Create sample CreateDto
        sampleCreateDto = new UserCreateDto(
                "Marie",
                "Martin",
                "marie.martin@example.com",
                "password123",
                UserRole.ADMIN
        );

        // Create sample UpdateDto
        sampleUpdateDto = new UserUpdateDto(
                "Marie Updated",
                "Martin Updated",
                "marie.updated@example.com",
                UserRole.ADMIN
        );
    }

    @Nested
    @DisplayName("toListDto Tests")
    class ToListDtoTests {

        @Test
        @DisplayName("Should successfully convert User to UserListDto")
        void shouldConvertUserToUserListDto() {
            // When
            UserListDto result = userMapper.toListDto(sampleUser);

            // Then
            assertNotNull(result);
            assertEquals(sampleUser.getIdAsString(), result.id());
            assertEquals(sampleUser.getFirstName(), result.firstName());
            assertEquals(sampleUser.getLastName(), result.lastName());
            assertEquals(sampleUser.getEmail(), result.email());
            assertEquals(sampleUser.getRole(), result.role());
            assertEquals(sampleUser.isActive(), result.active());
        }

        @Test
        @DisplayName("Should return null when User is null")
        void shouldReturnNullWhenUserIsNull() {
            // When
            UserListDto result = userMapper.toListDto(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should handle User with null ID")
        void shouldHandleUserWithNullId() {
            // Given
            sampleUser.id = null;

            // When
            UserListDto result = userMapper.toListDto(sampleUser);

            // Then
            assertNotNull(result);
            assertNull(result.id());
            assertEquals(sampleUser.getFirstName(), result.firstName());
        }

        @Test
        @DisplayName("Should handle User with minimal data")
        void shouldHandleUserWithMinimalData() {
            // Given
            User minimalUser = new User();
            minimalUser.id = new ObjectId();
            minimalUser.setEmail("test@test.com");
            minimalUser.setRole(UserRole.MEMBER);

            // When
            UserListDto result = userMapper.toListDto(minimalUser);

            // Then
            assertNotNull(result);
            assertEquals(minimalUser.getIdAsString(), result.id());
            assertEquals(minimalUser.getEmail(), result.email());
            assertEquals(minimalUser.getRole(), result.role());
        }
    }

    @Nested
    @DisplayName("toDetailsDto Tests")
    class ToDetailsDtoTests {

        @Test
        @DisplayName("Should successfully convert User to UserDetailsDto")
        void shouldConvertUserToUserDetailsDto() {
            // When
            UserDetailsDto result = userMapper.toDetailsDto(sampleUser);

            // Then
            assertNotNull(result);
            assertEquals(sampleUser.getIdAsString(), result.id());
            assertEquals(sampleUser.getFirstName(), result.firstName());
            assertEquals(sampleUser.getLastName(), result.lastName());
            assertEquals(sampleUser.getEmail(), result.email());
            assertEquals(sampleUser.getRole(), result.role());
            assertEquals(sampleUser.isActive(), result.active());
            assertEquals(sampleUser.getCreatedBy(), result.createdBy());
            assertEquals(sampleUser.getCreatedAt(), result.createdAt());
            assertEquals(sampleUser.getUpdatedBy(), result.updatedBy());
            assertEquals(sampleUser.getUpdatedAt(), result.updatedAt());
        }

        @Test
        @DisplayName("Should return null when User is null")
        void shouldReturnNullWhenUserIsNull() {
            // When
            UserDetailsDto result = userMapper.toDetailsDto(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should handle User with null audit fields")
        void shouldHandleUserWithNullAuditFields() {
            // Given
            sampleUser.setCreatedBy(null);
            sampleUser.setCreatedAt(null);
            sampleUser.setUpdatedBy(null);
            sampleUser.setUpdatedAt(null);

            // When
            UserDetailsDto result = userMapper.toDetailsDto(sampleUser);

            // Then
            assertNotNull(result);
            assertEquals(sampleUser.getFirstName(), result.firstName());
            assertNull(result.createdBy());
            assertNull(result.createdAt());
            assertNull(result.updatedBy());
            assertNull(result.updatedAt());
        }
    }

    @Nested
    @DisplayName("toEntity Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should successfully convert UserCreateDto to User")
        void shouldConvertUserCreateDtoToUser() {
            // When
            User result = userMapper.toEntity(sampleCreateDto);

            // Then
            assertNotNull(result);
            assertEquals(sampleCreateDto.firstName(), result.getFirstName());
            assertEquals(sampleCreateDto.lastName(), result.getLastName());
            assertEquals(sampleCreateDto.email(), result.getEmail());
            assertEquals(sampleCreateDto.password(), result.getPassword());
            assertEquals(sampleCreateDto.role(), result.getRole());

            // Audit fields should be null (set elsewhere)
            assertNull(result.getCreatedBy());
            assertNull(result.getCreatedAt());
        }

        @Test
        @DisplayName("Should return null when UserCreateDto is null")
        void shouldReturnNullWhenCreateDtoIsNull() {
            // When
            User result = userMapper.toEntity(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should handle UserCreateDto with minimal data")
        void shouldHandleCreateDtoWithMinimalData() {
            // Given
            UserCreateDto minimalDto = new UserCreateDto(
                    "Test",
                    "User",
                    "test@test.com",
                    "password",
                    UserRole.MEMBER
            );

            // When
            User result = userMapper.toEntity(minimalDto);

            // Then
            assertNotNull(result);
            assertEquals(minimalDto.firstName(), result.getFirstName());
            assertEquals(minimalDto.email(), result.getEmail());
            assertEquals(minimalDto.role(), result.getRole());
        }
    }

    @Nested
    @DisplayName("updateEntity Tests")
    class UpdateEntityTests {

        @Test
        @DisplayName("Should successfully update User with UserUpdateDto")
        void shouldUpdateUserWithUserUpdateDto() {
            // Given
            LocalDateTime originalUpdatedAt = sampleUser.getUpdatedAt();

            // When
            userMapper.updateEntity(sampleUser, sampleUpdateDto);

            // Then
            assertEquals(sampleUpdateDto.firstName(), sampleUser.getFirstName());
            assertEquals(sampleUpdateDto.lastName(), sampleUser.getLastName());
            assertEquals(sampleUpdateDto.email(), sampleUser.getEmail());
            assertEquals(sampleUpdateDto.role(), sampleUser.getRole());

            // Updated timestamp should be changed by preUpdate()
            assertNotEquals(originalUpdatedAt, sampleUser.getUpdatedAt());
        }

        @Test
        @DisplayName("Should skip null fields during update")
        void shouldSkipNullFieldsDuringUpdate() {
            // Given
            String originalFirstName = sampleUser.getFirstName();
            String originalEmail = sampleUser.getEmail();

            UserUpdateDto partialUpdate = new UserUpdateDto(
                    null, // firstName should not be updated
                    "New Last Name",
                    null, // email should not be updated
                    UserRole.ADMIN
            );

            // When
            userMapper.updateEntity(sampleUser, partialUpdate);

            // Then
            assertEquals(originalFirstName, sampleUser.getFirstName()); // Unchanged
            assertEquals("New Last Name", sampleUser.getLastName()); // Updated
            assertEquals(originalEmail, sampleUser.getEmail()); // Unchanged
            assertEquals(UserRole.ADMIN, sampleUser.getRole()); // Updated
        }

        @Test
        @DisplayName("Should do nothing when UserUpdateDto is null")
        void shouldDoNothingWhenUpdateDtoIsNull() {
            // Given
            String originalFirstName = sampleUser.getFirstName();
            LocalDateTime originalUpdatedAt = sampleUser.getUpdatedAt();

            // When
            userMapper.updateEntity(sampleUser, null);

            // Then
            assertEquals(originalFirstName, sampleUser.getFirstName());
            assertEquals(originalUpdatedAt, sampleUser.getUpdatedAt());
        }

        @Test
        @DisplayName("Should do nothing when User is null")
        void shouldDoNothingWhenUserIsNull() {
            // When & Then - Should not throw exception
            assertDoesNotThrow(() -> userMapper.updateEntity(null, sampleUpdateDto));
        }

        @Test
        @DisplayName("Should do nothing when both User and UpdateDto are null")
        void shouldDoNothingWhenBothAreNull() {
            // When & Then - Should not throw exception
            assertDoesNotThrow(() -> userMapper.updateEntity(null, null));
        }
    }

    @Nested
    @DisplayName("updateEntityFromUpdateRequest Tests")
    class UpdateEntityFromUpdateRequestTests {

        @Test
        @DisplayName("Should delegate to updateEntity method")
        void shouldDelegateToUpdateEntityMethod() {
            // Given
            String originalFirstName = sampleUser.getFirstName();

            // When
            userMapper.updateEntityFromUpdateRequest(sampleUser, sampleUpdateDto);

            // Then
            assertEquals(sampleUpdateDto.firstName(), sampleUser.getFirstName());
            assertNotEquals(originalFirstName, sampleUser.getFirstName());
        }

        @Test
        @DisplayName("Should handle null inputs gracefully")
        void shouldHandleNullInputsGracefully() {
            // When & Then - Should not throw exception
            assertDoesNotThrow(() -> userMapper.updateEntityFromUpdateRequest(null, null));
            assertDoesNotThrow(() -> userMapper.updateEntityFromUpdateRequest(sampleUser, null));
            assertDoesNotThrow(() -> userMapper.updateEntityFromUpdateRequest(null, sampleUpdateDto));
        }
    }

    @Nested
    @DisplayName("ObjectId Conversion Tests")
    class ObjectIdConversionTests {

        @Test
        @DisplayName("Should successfully convert valid string to ObjectId")
        void shouldConvertValidStringToObjectId() {
            // Given
            String validId = "507f1f77bcf86cd799439011";

            // When
            ObjectId result = userMapper.toObjectId(validId);

            // Then
            assertNotNull(result);
            assertEquals(validId, result.toString());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should return null for null, empty, or whitespace strings")
        void shouldReturnNullForInvalidInputs(String input) {
            // When
            ObjectId result = userMapper.toObjectId(input);

            // Then
            assertNull(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "123",
                "507f1f77bcf86cd79943901", // Too short
                "507f1f77bcf86cd799439011z", // Invalid character
                "507f1f77bcf86cd799439011a1" // Too long
        })
        @DisplayName("Should throw IllegalArgumentException for invalid ObjectId formats")
        void shouldThrowExceptionForInvalidFormats(String invalidId) {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userMapper.toObjectId(invalidId)
            );

            assertTrue(exception.getMessage().contains("Invalid ObjectId format"));
            assertTrue(exception.getMessage().contains(invalidId));
        }

        @Test
        @DisplayName("Should convert ObjectId to string")
        void shouldConvertObjectIdToString() {
            // Given
            ObjectId objectId = new ObjectId();

            // When
            String result = userMapper.toStringId(objectId);

            // Then
            assertNotNull(result);
            assertEquals(objectId.toString(), result);
        }

        @Test
        @DisplayName("Should return null when ObjectId is null")
        void shouldReturnNullWhenObjectIdIsNull() {
            // When
            String result = userMapper.toStringId(null);

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("ObjectId Validation Tests")
    class ObjectIdValidationTests {

        @Test
        @DisplayName("Should validate correct ObjectId format")
        void shouldValidateCorrectObjectIdFormat() {
            // Given
            String validId = "507f1f77bcf86cd799439011";

            // When
            boolean result = userMapper.isValidObjectId(validId);

            // Then
            assertTrue(result);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should return false for null, empty, or whitespace strings")
        void shouldReturnFalseForInvalidInputs(String input) {
            // When
            boolean result = userMapper.isValidObjectId(input);

            // Then
            assertFalse(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "123",
                "507f1f77bcf86cd79943901", // Too short
                "507f1f77bcf86cd799439011z", // Invalid character
                "507f1f77bcf86cd799439011a1" // Too long
        })
        @DisplayName("Should return false for invalid ObjectId formats")
        void shouldReturnFalseForInvalidFormats(String invalidId) {
            // When
            boolean result = userMapper.isValidObjectId(invalidId);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should validate multiple valid ObjectId formats")
        void shouldValidateMultipleValidObjectIdFormats() {
            // Given
            String[] validIds = {
                    "507f1f77bcf86cd799439011",
                    "507f191e810c19729de860ea",
                    "000000000000000000000000",
                    "ffffffffffffffffffffffff"
            };

            // When & Then
            for (String validId : validIds) {
                assertTrue(userMapper.isValidObjectId(validId),
                        "Should validate ObjectId: " + validId);
            }
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should perform complete User lifecycle mapping")
        void shouldPerformCompleteUserLifecycleMapping() {
            // 1. Create User from DTO
            User user = userMapper.toEntity(sampleCreateDto);
            assertNotNull(user);
            assertEquals(sampleCreateDto.email(), user.getEmail());

            // 2. Set ID and audit fields (simulating persistence)
            user.id = new ObjectId();
            user.prePersist("admin");

            // 3. Convert to ListDto
            UserListDto listDto = userMapper.toListDto(user);
            assertNotNull(listDto);
            assertEquals(user.getIdAsString(), listDto.id());
            assertEquals(user.getEmail(), listDto.email());

            // 4. Convert to DetailsDto
            UserDetailsDto detailsDto = userMapper.toDetailsDto(user);
            assertNotNull(detailsDto);
            assertEquals(user.getIdAsString(), detailsDto.id());
            assertEquals(user.getCreatedBy(), detailsDto.createdBy());

            // 5. Update User
            UserUpdateDto updateDto = new UserUpdateDto(
                    "Updated Name", null, null, UserRole.ADMIN
            );
            userMapper.updateEntity(user, updateDto);
            assertEquals("Updated Name", user.getFirstName());
            assertEquals(UserRole.ADMIN, user.getRole());
        }

        @Test
        @DisplayName("Should handle ObjectId roundtrip conversion")
        void shouldHandleObjectIdRoundtripConversion() {
            // Given
            String originalId = "507f1f77bcf86cd799439011";

            // When
            ObjectId objectId = userMapper.toObjectId(originalId);
            String convertedBack = userMapper.toStringId(objectId);

            // Then
            assertEquals(originalId, convertedBack);
        }

        @Test
        @DisplayName("Should maintain data integrity during multiple mappings")
        void shouldMaintainDataIntegrityDuringMultipleMappings() {
            // Given
            sampleUser.id = new ObjectId();

            // When - Multiple conversions
            UserDetailsDto detailsDto = userMapper.toDetailsDto(sampleUser);
            UserListDto listDto = userMapper.toListDto(sampleUser);

            // Then - Data should be consistent
            assertEquals(detailsDto.id(), listDto.id());
            assertEquals(detailsDto.email(), listDto.email());
            assertEquals(detailsDto.firstName(), listDto.firstName());
            assertEquals(detailsDto.lastName(), listDto.lastName());
            assertEquals(detailsDto.role(), listDto.role());
            assertEquals(detailsDto.active(), listDto.active());
        }
    }
}