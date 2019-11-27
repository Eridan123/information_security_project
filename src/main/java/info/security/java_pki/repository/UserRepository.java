package info.security.java_pki.repository;

import info.security.java_pki.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    public User findUserByUsername(String username);
}
