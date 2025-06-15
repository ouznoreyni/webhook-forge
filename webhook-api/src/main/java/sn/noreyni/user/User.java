package sn.noreyni.user;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;
import sn.noreyni.common.entity.BaseEntity;
import sn.noreyni.common.enums.UserRole;

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
}