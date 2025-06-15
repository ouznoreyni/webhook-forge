package sn.noreyni.user;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements ReactivePanacheMongoRepository<User> {
    public Uni<User> findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public Uni<Boolean> existsByEmail(String email) {
        return find("email", email).count()
                .map(count -> count > 0);
    }

    public Uni<Long> countActive() {
        return find("active", true).count();
    }

    public Uni<Boolean> existsByEmailAndIdNot(String email, String excludeId) {
        return find("email = ?1 and id != ?2", email, excludeId).count()
                .map(count -> count > 0);
    }
}
