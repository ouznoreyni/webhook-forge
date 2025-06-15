package sn.noreyni.project;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import sn.noreyni.common.enums.InvitationStatus;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ProjectInvitationRepository implements ReactivePanacheMongoRepository<ProjectInvitation> {

    public Uni<List<ProjectInvitation>> findByProjectId(String projectId) {
        return find("projectId", projectId).list();
    }

    public Uni<List<ProjectInvitation>> findByInviterId(String inviterId) {
        return find("inviterId", inviterId).list();
    }

    public Uni<List<ProjectInvitation>> findByInviteeId(String inviteeId) {
        return find("inviteeId", inviteeId).list();
    }

    public Uni<List<ProjectInvitation>> findByStatus(InvitationStatus status) {
        return find("status", status).list();
    }

    public Uni<List<ProjectInvitation>> findByProjectIdAndStatus(String projectId, InvitationStatus status) {
        return find("projectId = ?1 and status = ?2", projectId, status).list();
    }

    public Uni<List<ProjectInvitation>> findByInviteeIdAndStatus(String inviteeId, InvitationStatus status) {
        return find("inviteeId = ?1 and status = ?2", inviteeId, status).list();
    }

    public Uni<ProjectInvitation> findByProjectIdAndInviteeId(String projectId, String inviteeId) {
        return find("projectId = ?1 and inviteeId = ?2", projectId, inviteeId).firstResult();
    }

    public Uni<Boolean> existsByProjectIdAndInviteeId(String projectId, String inviteeId) {
        return find("projectId = ?1 and inviteeId = ?2", projectId, inviteeId).count()
                .map(count -> count > 0);
    }

    public Uni<Boolean> existsPendingInvitation(String projectId, String inviteeId) {
        return find("projectId = ?1 and inviteeId = ?2 and status = ?3",
                projectId, inviteeId, InvitationStatus.PENDING).count()
                .map(count -> count > 0);
    }

    // Count methods
    public Uni<Long> countByProjectId(String projectId) {
        return find("projectId", projectId).count();
    }

    public Uni<Long> countByProjectIdAndStatus(String projectId, InvitationStatus status) {
        return find("projectId = ?1 and status = ?2", projectId, status).count();
    }

    public Uni<Long> countByInviteeId(String inviteeId) {
        return find("inviteeId", inviteeId).count();
    }

    public Uni<Long> countByInviteeIdAndStatus(String inviteeId, InvitationStatus status) {
        return find("inviteeId = ?1 and status = ?2", inviteeId, status).count();
    }

    public Uni<Long> countByInviterId(String inviterId) {
        return find("inviterId", inviterId).count();
    }

    public Uni<Long> countByStatus(InvitationStatus status) {
        return find("status", status).count();
    }

    // Expired invitations
    public Uni<List<ProjectInvitation>> findExpiredInvitations() {
        return find("expiresAt < ?1 and status = ?2",
                LocalDateTime.now(), InvitationStatus.PENDING).list();
    }

    public Uni<List<ProjectInvitation>> findExpiredInvitationsByProjectId(String projectId) {
        return find("projectId = ?1 and expiresAt < ?2 and status = ?3",
                projectId, LocalDateTime.now(), InvitationStatus.PENDING).list();
    }

    public Uni<Long> countExpiredInvitations() {
        return find("expiresAt < ?1 and status = ?2",
                LocalDateTime.now(), InvitationStatus.PENDING).count();
    }

    // Recent invitations
    public Uni<List<ProjectInvitation>> findRecentInvitations(LocalDateTime since) {
        return find("sentAt >= ?1", since).list();
    }

    public Uni<List<ProjectInvitation>> findRecentInvitationsByInviteeId(String inviteeId, LocalDateTime since) {
        return find("inviteeId = ?1 and sentAt >= ?2", inviteeId, since).list();
    }

    public Uni<List<ProjectInvitation>> findRecentInvitationsByProjectId(String projectId, LocalDateTime since) {
        return find("projectId = ?1 and sentAt >= ?2", projectId, since).list();
    }

    // Batch operations
    public Uni<Long> updateExpiredInvitations() {
        return update("status = ?1", InvitationStatus.EXPIRED)
                .where("expiresAt < ?1 and status = ?2", LocalDateTime.now(), InvitationStatus.PENDING);
    }

    public Uni<Long> deleteExpiredInvitations(LocalDateTime olderThan) {
        return delete("expiresAt < ?1 and status = ?2", olderThan, InvitationStatus.EXPIRED);
    }

    // User-specific invitation queries
    public Uni<List<ProjectInvitation>> findUserInvitations(String userId) {
        return find("inviteeId = ?1 or inviterId = ?2", userId, userId).list();
    }

    public Uni<List<ProjectInvitation>> findPendingInvitationsForUser(String userId) {
        return find("inviteeId = ?1 and status = ?2", userId, InvitationStatus.PENDING).list();
    }

    public Uni<List<ProjectInvitation>> findSentInvitationsByUser(String userId) {
        return find("inviterId = ?1", userId).list();
    }
}