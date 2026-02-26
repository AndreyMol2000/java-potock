package ru.molchanov.demo.task;

import ru.molchanov.demo.repositori.BankAccountRepository;

import java.time.LocalDate;

public class LoggerTask implements Runnable {

    private BankAccountRepository bankAccountRepository;
    private boolean running = true;

    public LoggerTask(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public void run(){
        while (running){
            try {
                long count = bankAccountRepository.count();
                System.out.printf(LocalDate.now() + ": info поток nameThread " + Thread.currentThread().getName()+"] колличество счетов " + count);
                Thread.sleep(30000);
            } catch (InterruptedException e){
                System.out.println("демон логгер остановлен" + Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                break;
            }catch (Exception e){
                System.err.println("Демон ошибка" + e.getMessage());
                break;
            }
        }
    }
}
