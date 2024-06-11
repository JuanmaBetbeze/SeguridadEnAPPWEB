package Seguridad_en_APP_Web.demo.repository;

import Seguridad_en_APP_Web.demo.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    CreditCard findByCustomUserId(int userId);
}
