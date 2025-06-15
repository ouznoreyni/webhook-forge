package sn.noreyni.project;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;

import java.util.List;

@ApplicationScoped
public class ProjectRepository implements ReactivePanacheMongoRepository<Project> {

    public Uni<Project> findByName(String name) {
        return find("name", name).firstResult();
    }

    public Uni<Boolean> existsByName(String name) {
        return find("name", name).count()
                .map(count -> count > 0);
    }

    public Uni<Boolean> existsByNameAndIdNot(String name, String excludeId) {
        return find("name = ?1 and id != ?2", name, excludeId).count()
                .map(count -> count > 0);
    }

    public Uni<List<Project>> findByOwnerId(String ownerId) {
        return find("ownerId", ownerId).list();
    }

    public Uni<List<Project>> findByMemberId(String memberId) {
        return find("memberIds", new ObjectId(memberId)).list();
    }

    public Uni<List<Project>> findByInvitedUserId(String userId) {
        return find("invitedUserIds", new ObjectId(userId)).list();
    }

    public Uni<List<Project>> findByOwnerIdAndStatus(String ownerId, ProjectStatus status) {
        return find("ownerId = ?1 and status = ?2", ownerId, status).list();
    }

    public Uni<List<Project>> findByMemberIdAndStatus(String memberId, ProjectStatus status) {
        return find("memberIds = ?1 and status = ?2", new ObjectId(memberId), status).list();
    }

    public Uni<List<Project>> findByVisibility(Visibility visibility) {
        return find("visibility", visibility).list();
    }

    public Uni<List<Project>> findByType(ProjectType type) {
        return find("type", type).list();
    }

    public Uni<List<Project>> findByStatus(ProjectStatus status) {
        return find("status", status).list();
    }

    public Uni<Long> countByOwnerId(String ownerId) {
        return find("ownerId", ownerId).count();
    }

    public Uni<Long> countByOwnerIdAndStatus(String ownerId, ProjectStatus status) {
        return find("ownerId = ?1 and status = ?2", ownerId, status).count();
    }

    public Uni<Long> countByMemberId(String memberId) {
        return find("memberIds", new ObjectId(memberId)).count();
    }

    public Uni<Long> countByMemberIdAndStatus(String memberId, ProjectStatus status) {
        return find("memberIds = ?1 and status = ?2", new ObjectId(memberId), status).count();
    }

    public Uni<Long> countByStatus(ProjectStatus status) {
        return find("status", status).count();
    }

    public Uni<Long> countByType(ProjectType type) {
        return find("type", type).count();
    }

    public Uni<Long> countByVisibility(Visibility visibility) {
        return find("visibility", visibility).count();
    }

    public Uni<Boolean> isMember(String projectId, String userId) {
        return find("id = ?1 and memberIds = ?2", new ObjectId(projectId), new ObjectId(userId)).count()
                .map(count -> count > 0);
    }

    public Uni<Boolean> isOwner(String projectId, String userId) {
        return find("id = ?1 and ownerId = ?2", new ObjectId(projectId), userId).count()
                .map(count -> count > 0);
    }

    public Uni<Boolean> isInvited(String projectId, String userId) {
        return find("id = ?1 and invitedUserIds = ?2", new ObjectId(projectId), new ObjectId(userId)).count()
                .map(count -> count > 0);
    }

    // Advanced search methods
    public Uni<List<Project>> findByNameContaining(String nameFragment) {
        return find("name like ?1", ".*" + nameFragment + ".*").list();
    }

    public Uni<List<Project>> findByDescriptionContaining(String descriptionFragment) {
        return find("description like ?1", ".*" + descriptionFragment + ".*").list();
    }

    public Uni<List<Project>> findPublicProjects() {
        return find("visibility", Visibility.PUBLIC).list();
    }

    public Uni<List<Project>> findActiveProjects() {
        return find("status != ?1", ProjectStatus.ARCHIVED).list();
    }

    // User-specific project queries
    public Uni<List<Project>> findUserProjects(String userId) {
        return find("ownerId = ?1 or memberIds = ?2", userId, new ObjectId(userId)).list();
    }

    public Uni<List<Project>> findUserProjectsByStatus(String userId, ProjectStatus status) {
        return find("(ownerId = ?1 or memberIds = ?2) and status = ?3",
                userId, new ObjectId(userId), status).list();
    }
}
