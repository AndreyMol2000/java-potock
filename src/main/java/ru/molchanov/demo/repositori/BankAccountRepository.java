package ru.molchanov.demo.repositori;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.molchanov.demo.domain.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

}