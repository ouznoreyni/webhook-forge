package sn.noreyni.user;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import sn.noreyni.common.entity.BaseEntity;
import sn.noreyni.common.enums.UserRole;
import sn.noreyni.project.Project;
import sn.noreyni.project.ProjectInvitation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "users")
public class User extends BaseEntity {

    @NotBlank(message = "Le prénom est requis")
    @BsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Le nom de famille est requis")
    @BsonProperty("last_name")
    private String lastName;

    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    @BsonProperty("email")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @BsonProperty("password")
    private String password;

    @NotNull(message = "Le rôle est requis")
    @BsonProperty("role")
    private UserRole role;

    @BsonProperty("active")
    private boolean active = true;

    @BsonProperty("owned_project_ids")
    private Set<String> ownedProjectIds = new HashSet<>();

    @BsonProperty("member_project_ids")
    private Set<String> memberProjectIds = new HashSet<>();

    @BsonProperty("invited_project_ids")
    private Set<String> invitedProjectIds = new HashSet<>();

    // Transient field to populated  when fetched
    @BsonIgnore
    private List<Project> projects;

    @BsonIgnore
    private List<Project> ownedProjects;

    @BsonIgnore
    private List<ProjectInvitation> projectInvitations;
}