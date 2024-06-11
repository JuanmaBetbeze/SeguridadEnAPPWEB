package Seguridad_en_APP_Web.demo.repository;

import Seguridad_en_APP_Web.demo.model.CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomUserRepository extends JpaRepository<CustomUser, Long> {
    CustomUser findByUsername(String username);
    CustomUser findById(int id);
}
