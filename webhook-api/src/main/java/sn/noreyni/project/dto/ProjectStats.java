package sn.noreyni.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectStats {
    private   long totalProjects;
    private  long activeProjects;
    private  long draftProjects;
    private  long completedProjects;
}
