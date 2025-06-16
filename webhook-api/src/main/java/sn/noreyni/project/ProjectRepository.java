package sn.noreyni.project;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import sn.noreyni.common.enums.ProjectStatus;
import sn.noreyni.common.enums.ProjectType;
import sn.noreyni.common.enums.Visibility;
import sn.noreyni.project.dto.ProjectStats;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProjectRepository implements ReactivePanacheMongoRepository<Project> {

    /**
     * Check if a project with the given name exists
     */
    public Uni<Boolean> existsByName(String name) {
        return find("name", name).count()
                .map(count -> count > 0);
    }

    /**
     * Find project by name
     */
    public Uni<Project> findByName(String name) {
        return find("name", name).firstResult();
    }

    /**
     * Find all projects owned by a specific user
     */
    public Uni<List<Project>> findByOwnerId(String ownerId) {
        return find("ownerId", ownerId).list();
    }

    /**
     * Find projects by status
     */
    public Uni<List<Project>> findByStatus(ProjectStatus status) {
        return find("status", status).list();
    }

    /**
     * Find projects by visibility
     */
    public Uni<List<Project>> findByVisibility(Visibility visibility) {
        return find("visibility", visibility).list();
    }

    /**
     * Find projects by type
     */
    public Uni<List<Project>> findByType(ProjectType type) {
        return find("type", type).list();
    }

    /**
     * Check if a project with the given name exists, excluding a specific project ID
     */
    public Uni<Boolean> existsByNameAndIdNot(String name, String excludeId) {
        return find("name = ?1 and id != ?2", name, excludeId).count()
                .map(count -> count > 0);
    }

    /**
     * Find projects where user is a member
     */
    public Uni<List<Project>> findByMemberId(String memberId) {
        return find("memberIds", memberId).list();
    }

    /**
     * Find projects where user is invited
     */
    public Uni<List<Project>> findByInvitedUserId(String invitedUserId) {
        return find("invitedUserIds", invitedUserId).list();
    }

    /**
     * Count projects by owner
     */
    public Uni<Long> countByOwnerId(String ownerId) {
        return find("ownerId", ownerId).count();
    }

    /**
     * Count projects by status
     */
    public Uni<Long> countByStatus(ProjectStatus status) {
        return find("status", status).count();
    }

    /**
     * Find public projects (for discovery)
     */
    public Uni<List<Project>> findPublicProjects() {
        return find("visibility", Visibility.PUBLIC).list();
    }

    /**
     * Search projects with pagination and multiple filters
     *
     * @param name Filter by project name (partial match, case-insensitive)
     * @param status Filter by project status
     * @param visibility Filter by project visibility
     * @param type Filter by project type
     * @param ownerId Filter by owner ID
     * @param page Page number (0-based)
     * @param size Page size
     * @param sortBy Field to sort by (default: "name")
     * @param sortDirection Sort direction ("asc" or "desc", default: "asc")
     * @return Paginated list of projects
     */
    public Uni<List<Project>> searchProjects(
            String name,
            ProjectStatus status,
            Visibility visibility,
            ProjectType type,
            String ownerId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        StringBuilder queryBuilder = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        int paramIndex = 1;

        // Build dynamic query
        if (name != null && !name.trim().isEmpty()) {
            queryBuilder.append("name like ?").append(paramIndex++);
            parameters.add(".*" + name.trim() + ".*");
        }

        if (status != null) {
            if (!queryBuilder.isEmpty()) queryBuilder.append(" and ");
            queryBuilder.append("status = ?").append(paramIndex++);
            parameters.add(status);
        }

        if (visibility != null) {
            if (!queryBuilder.isEmpty()) queryBuilder.append(" and ");
            queryBuilder.append("visibility = ?").append(paramIndex++);
            parameters.add(visibility);
        }

        if (type != null) {
            if (!queryBuilder.isEmpty()) queryBuilder.append(" and ");
            queryBuilder.append("type = ?").append(paramIndex++);
            parameters.add(type);
        }

        if (ownerId != null && !ownerId.trim().isEmpty()) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("ownerId = ?").append(paramIndex++);
            parameters.add(ownerId.trim());
        }

        // Build sort
        Sort sort = Sort.by(sortBy != null ? sortBy : "name");
        if ("desc".equalsIgnoreCase(sortDirection)) {
            sort = sort.descending();
        }

        // Execute query
        String query = queryBuilder.length() > 0 ? queryBuilder.toString() : "";
        Object[] params = parameters.toArray();

        if (query.isEmpty()) {
            return findAll(sort).page(Page.of(page, size)).list();
        } else {
            return find(query, sort, params).page(Page.of(page, size)).list();
        }
    }

    /**
     * Count total results for search query (for pagination metadata)
     */
    public Uni<Long> countSearchResults(
            String name,
            ProjectStatus status,
            Visibility visibility,
            ProjectType type,
            String ownerId) {

        StringBuilder queryBuilder = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        int paramIndex = 1;

        // Build same query as search method
        if (name != null && !name.trim().isEmpty()) {
            queryBuilder.append("name like ?").append(paramIndex++);
            parameters.add(".*" + name.trim() + ".*");
        }

        if (status != null) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("status = ?").append(paramIndex++);
            parameters.add(status);
        }

        if (visibility != null) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("visibility = ?").append(paramIndex++);
            parameters.add(visibility);
        }

        if (type != null) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("type = ?").append(paramIndex++);
            parameters.add(type);
        }

        if (ownerId != null && !ownerId.trim().isEmpty()) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("ownerId = ?").append(paramIndex++);
            parameters.add(ownerId.trim());
        }

        String query = queryBuilder.length() > 0 ? queryBuilder.toString() : "";
        Object[] params = parameters.toArray();

        if (query.isEmpty()) {
            return count();
        } else {
            return find(query, params).count();
        }
    }

    /**
     * Find projects accessible by a user (owned, member, or public)
     */
    public Uni<List<Project>> findAccessibleProjects(String userId) {
        return find("ownerId = ?1 or memberIds = ?2 or visibility = ?3",
                userId, userId, Visibility.PUBLIC).list();
    }

    /**
     * Find recent projects by owner (last 30 days)
     */
    public Uni<List<Project>> findRecentProjectsByOwner(String ownerId, int limit) {
        return find("ownerId = ?1", Sort.by("createdAt").descending(), ownerId)
                .page(Page.ofSize(limit))
                .list();
    }

    /**
     * Find projects by multiple owners
     */
    public Uni<List<Project>> findByOwnerIds(List<String> ownerIds) {
        return find("ownerId in ?1", ownerIds).list();
    }

    /**
     * Get project statistics by owner
     */
    public Uni<ProjectStats> getProjectStatsByOwner(String ownerId) {
        return find("ownerId", ownerId).list()
                .map(projects -> {
                    long totalProjects = projects.size();
                    long activeProjects = projects.stream()
                            .filter(p -> p.getStatus() == ProjectStatus.ACTIVE)
                            .count();
                    long draftProjects = projects.stream()
                            .filter(p -> p.getStatus() == ProjectStatus.DRAFT)
                            .count();
                    long completedProjects = projects.stream()
                            .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                            .count();

                    return new ProjectStats(totalProjects, activeProjects, draftProjects, completedProjects);
                });
    }

}