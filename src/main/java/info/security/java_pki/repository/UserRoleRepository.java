package info.security.java_pki.repository;

import info.security.java_pki.model.User;
import info.security.java_pki.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole,Long> {
    public List<UserRole> findAllByUser(User user);
}
