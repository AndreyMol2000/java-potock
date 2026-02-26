package ru.molchanov.demo.servise;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.molchanov.demo.domain.BankAccount;
import ru.molchanov.demo.repositori.BankAccountRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BankAccountService {
    private final BankAccountRepository repository;

    public BankAccountService(BankAccountRepository repository){
        this.repository = repository;
    }

    public List<BankAccount> getAllAccounts(){
        return  repository.findAll();
    }
    public BankAccount getAccoutn(String accountNumber){
        Optional<BankAccount> bankAccountOpt = repository.findById(accountNumber);
        return  bankAccountOpt.orElseThrow(()-> new RuntimeException("счет не найден " + accountNumber));


    }

    public BankAccount seveAccount(BankAccount bankAccount){
        return repository.save(bankAccount);
    }

    @Transactional
    public void tranfer(String fromNumber, String ToNumber , double amount){
        BankAccount fromAcc = getAccoutn(fromNumber);
        BankAccount toAcc = getAccoutn(ToNumber);

        if(fromAcc.getBalance() < amount){
            throw new RuntimeException("недостаточно средств " + fromNumber);
        }

        fromAcc.setBalance(fromAcc.getBalance() - amount);
        toAcc.setBalance(toAcc.getBalance() + amount);

        seveAccount(fromAcc);
        seveAccount(toAcc);

        System.out.println("перевод" + amount +"со счета " + fromNumber + "еа счет" + ToNumber + "выполнен");
    }
}
