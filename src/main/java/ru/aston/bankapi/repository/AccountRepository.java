package ru.aston.bankapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aston.bankapi.model.Account;

import java.util.List;
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findAll();
}
