package sn.noreyni.project;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import sn.noreyni.common.entity.BaseEntity;
import sn.noreyni.common.enums.InvitationStatus;
import sn.noreyni.user.User;


import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "project_invitations")
public class ProjectInvitation extends BaseEntity {

    @BsonProperty("project_id")
    private String projectId;

    @BsonProperty("inviter_id")
    private String inviterId;

    @BsonProperty("invitee_id")
    private String inviteeId;

    @BsonProperty("sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    @BsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @BsonProperty("status")
    private InvitationStatus status = InvitationStatus.PENDING;


    // Transient fields
    @BsonIgnore
    private Project project;

    @BsonIgnore
    private User inviter;

    @BsonIgnore
    private User invitee;
}