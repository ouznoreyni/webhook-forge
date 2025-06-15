package sn.noreyni.user.integration;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * Integration tests for UserMapper
 * Tests the complete mapping workflow with real dependencies
 * Focuses on end-to-end scenarios and data integrity
 */
@QuarkusTest
@DisplayName("UserMapper Integration Tests")
class UserMapperIntegrationTest {

    @Inject
    UserMapper userMapper;

    private User sampleUser;
    private UserCreateDto sampleCreateDto;

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
    }

    @Test
    @DisplayName("Should perform complete User lifecycle mapping")
    void shouldPerformCompleteUserLifecycleMapping() {
        // 1. Create User from DTO
        User user = userMapper.toEntity(sampleCreateDto);
        assertNotNull(user);
        assertEquals(sampleCreateDto.email(), user.getEmail());
        assertEquals(sampleCreateDto.firstName(), user.getFirstName());
        assertEquals(sampleCreateDto.role(), user.getRole());

        // 2. Set ID and audit fields (simulating persistence)
        user.id = new ObjectId();
        user.prePersist("admin");

        // 3. Convert to ListDto
        UserListDto listDto = userMapper.toListDto(user);
        assertNotNull(listDto);
        assertEquals(user.getIdAsString(), listDto.id());
        assertEquals(user.getEmail(), listDto.email());
        assertEquals(user.getFirstName(), listDto.firstName());
        assertEquals(user.getRole(), listDto.role());

        // 4. Convert to DetailsDto
        UserDetailsDto detailsDto = userMapper.toDetailsDto(user);
        assertNotNull(detailsDto);
        assertEquals(user.getIdAsString(), detailsDto.id());
        assertEquals(user.getCreatedBy(), detailsDto.createdBy());
        assertEquals(user.getCreatedAt(), detailsDto.createdAt());

        // 5. Update User
        UserUpdateDto updateDto = new UserUpdateDto(
                "Updated Name",
                null,
                null,
                UserRole.ADMIN
        );

        LocalDateTime beforeUpdate = user.getUpdatedAt();
        userMapper.updateEntity(updateDto, user);

        assertEquals("Updated Name", user.getFirstName());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertNotEquals(beforeUpdate, user.getUpdatedAt()); // preUpdate() was called
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
        assertTrue(userMapper.isValidObjectId(originalId));
        assertTrue(userMapper.isValidObjectId(convertedBack));
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

    @Test
    @DisplayName("Should handle complex update scenarios with ModelMapper")
    void shouldHandleComplexUpdateScenariosWithModelMapper() {
        // Given - Create user via mapper (tests ModelMapper integration)
        User user = userMapper.toEntity(sampleCreateDto);
        user.id = new ObjectId();
        user.preUpdate("admin");

        // When - Perform multiple partial updates
        UserUpdateDto firstUpdate = new UserUpdateDto(
                "First Update",
                null,
                "updated1@example.com",
                null
        );

        UserUpdateDto secondUpdate = new UserUpdateDto(
                null,
                "Second Update",
                null,
                UserRole.ADMIN
        );

        String originalPassword = user.getPassword();
        LocalDateTime firstUpdateTime = user.getUpdatedAt();

        userMapper.updateEntity(firstUpdate, user);
        LocalDateTime secondUpdateTime = user.getUpdatedAt();

        userMapper.updateEntity(secondUpdate, user);

        // Then - Verify cumulative updates
        assertEquals("First Update", user.getFirstName()); // From first update
        assertEquals("Second Update", user.getLastName()); // From second update
        assertEquals("updated1@example.com", user.getEmail()); // From first update
        assertEquals(UserRole.ADMIN, user.getRole()); // From second update
        assertEquals(originalPassword, user.getPassword()); // Never updated

        // Verify timestamp updates
        assertNotEquals(firstUpdateTime, secondUpdateTime);
        assertNotEquals(secondUpdateTime, user.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle edge cases in real environment")
    void shouldHandleEdgeCasesInRealEnvironment() {
        // Test with minimal user data
        User minimalUser = new User();
        minimalUser.id = new ObjectId();
        minimalUser.setEmail("minimal@test.com");
        minimalUser.setRole(UserRole.MEMBER);

        // Should convert to DTOs without issues
        UserListDto listDto = userMapper.toListDto(minimalUser);
        UserDetailsDto detailsDto = userMapper.toDetailsDto(minimalUser);

        assertNotNull(listDto);
        assertNotNull(detailsDto);
        assertEquals(minimalUser.getIdAsString(), listDto.id());
        assertEquals(minimalUser.getIdAsString(), detailsDto.id());
        assertNull(listDto.firstName()); // Should handle null fields
        assertNull(detailsDto.createdBy()); // Should handle null audit fields

        // Test update with all null values (should do nothing)
        UserUpdateDto nullUpdate = new UserUpdateDto(null, null, null, null);
        String originalEmail = minimalUser.getEmail();
        LocalDateTime originalUpdatedAt = minimalUser.getUpdatedAt();

        userMapper.updateEntity(nullUpdate, minimalUser);

        assertEquals(originalEmail, minimalUser.getEmail());
        assertNotEquals(originalUpdatedAt, minimalUser.getUpdatedAt()); // preUpdate() still called
    }

    @Test
    @DisplayName("Should validate ObjectId operations in real environment")
    void shouldValidateObjectIdOperationsInRealEnvironment() {
        // Test various ObjectId formats that might come from real MongoDB
        String[] validIds = {
                "507f1f77bcf86cd799439011",
                "507f191e810c19729de860ea",
                "000000000000000000000000",
                "ffffffffffffffffffffffff"
        };

        for (String validId : validIds) {
            assertTrue(userMapper.isValidObjectId(validId),
                    "Should validate ObjectId: " + validId);

            ObjectId converted = userMapper.toObjectId(validId);
            assertNotNull(converted);
            assertEquals(validId, converted.toString());

            String backToString = userMapper.toStringId(converted);
            assertEquals(validId, backToString);
        }

        // Test invalid formats that might come from user input
        String[] invalidIds = {
                "invalid",
                "123",
                "507f1f77bcf86cd79943901", // Too short
                "507f1f77bcf86cd799439011z", // Invalid character
                "507f1f77bcf86cd799439011a1", // Too long
                "",
                " ",
                null
        };

        for (String invalidId : invalidIds) {
            assertFalse(userMapper.isValidObjectId(invalidId),
                    "Should reject invalid ObjectId: " + invalidId);
        }
    }
}