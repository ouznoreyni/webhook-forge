package sn.noreyni.project.dto;

public record ProjectStatsDto(
        long totalProjects,
        long activeProjects,
        long completedProjects,
        long draftProjects,
        long ownedProjects,
        long memberProjects
) {}
