package ru.molchanov.demo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "bank_account")
public class BankAccount {

    @Id
    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "balance", nullable = false)
    private double balance;

    public BankAccount() {}

    public BankAccount(String accountNumber, double balance){
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public String getAccountNumber(){
        return accountNumber;
    }

    public double getBalance(){
        return balance;
    }

    public void setBalance(double balance){
        this.balance = balance;
    }

    @Override
    public String toString(){
        return "BankAccount{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                '}';
    }
}