package sn.noreyni.common.enums;

import lombok.Getter;

@Getter
public enum ProjectType {
    SOFTWARE("Software Development"),
    BUSINESS("Business Project"),
    SERVICE_DESK("Service Desk"),
    OPERATIONS("Operations"),
    MARKETING("Marketing"),
    HR("Human Resources"),
    FINANCE("Finance"),
    LEGAL("Legal"),
    OTHER("Other");

    private final String displayName;

    ProjectType(String displayName) {
        this.displayName = displayName;
    }

    // Optional: From string value
    public static ProjectType fromString(String text) {
        for (ProjectType type : ProjectType.values()) {
            if (type.displayName.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return OTHER;
    }
}