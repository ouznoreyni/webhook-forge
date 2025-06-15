package sn.noreyni.project;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import sn.noreyni.common.entity.BaseEntity;
import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;
import sn.noreyni.user.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "projects")
public class Project extends BaseEntity {

    @BsonProperty("avatar_url")
    private String avatarUrl;

    @BsonProperty("name")
    private String name;

    @BsonProperty("description")
    private String description;

    @BsonProperty("status")
    private ProjectStatus status = ProjectStatus.DRAFT;

    @BsonProperty("visibility")
    private Visibility visibility = Visibility.PRIVATE;

    @BsonProperty("type")
    private ProjectType type;

    @BsonProperty("owner_id")
    private String ownerId;

    @BsonProperty("member_ids")
    private Set<String> memberIds = new HashSet<>();

    @BsonProperty("invited_user_ids")
    private Set<String> invitedUserIds = new HashSet<>();

    @BsonIgnore
    private User owner;  // Populated when fetching the project

    @BsonIgnore
    private List<User> members;  // Populated when fetching the project

    @BsonIgnore
    private List<User> invitedUsers;  // Populated when fetching the project
}