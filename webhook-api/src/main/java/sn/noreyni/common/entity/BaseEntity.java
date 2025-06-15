package sn.noreyni.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.LocalDateTime;

/**
 * Base entity that provides common auditing fields for all entities
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseEntity extends ReactivePanacheMongoEntity {

    @JsonIgnore
    @BsonProperty("created_by")
    public String createdBy;

    @JsonIgnore
    @BsonProperty("created_at")
    public LocalDateTime createdAt;

    @JsonIgnore
    @BsonProperty("updated_by")
    public String updatedBy;

    @JsonIgnore
    @BsonProperty("updated_at")
    public LocalDateTime updatedAt;

    /**
     * Called before persisting the entity
     */
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;

    }

    /**
     * Called before updating the entity
     */
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Set audit fields for creation
     */
    @BsonIgnore
    public void prePersist(String createdBy) {
        prePersist();
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    /**
     * Set audit fields for update
     */
    public void preUpdate(String updatedBy) {
        preUpdate();
        this.updatedBy = updatedBy;
    }

    /**
     * Get the entity ID as string
     */
    public String getIdAsString() {
        return this.id != null ? this.id.toString() : null;
    }
}