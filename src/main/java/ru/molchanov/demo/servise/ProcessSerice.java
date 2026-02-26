package ru.molchanov.demo.servise;


import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import ru.molchanov.demo.repositori.BankAccountRepository;
import ru.molchanov.demo.task.LoggerTask;
import ru.molchanov.demo.task.TransferTask;

@Service
public class ProcessSerice {
    private final BankAccountService bankAccountService;
    private final BankAccountRepository bankAccountRepository;
    private final ConfigurableApplicationContext context;

    int demonCount = 0;

    public ProcessSerice(BankAccountService bankAccountService, BankAccountRepository bankAccountRepository, ConfigurableApplicationContext context) {
        this.bankAccountService = bankAccountService;
        this.bankAccountRepository = bankAccountRepository;
        this.context = context;
    }

    public  String process(){
        Thread.setDefaultUncaughtExceptionHandler((thread , throwable)->{
            System.out.println("не перехваченная ошибка потока" + thread.getName() + " " + throwable.getMessage());
            System.out.println("завершение программы");
            context.close();
        });



        LoggerTask loggerTask = new LoggerTask(bankAccountRepository);
        Thread loggerThread = new Thread(loggerTask , "loggerTasl-Demon" + demonCount++);
        loggerThread.setDaemon(true);
        loggerThread.start();

        Thread thread1 = new Thread(
                new TransferTask("ACC001", "ACC002", 100, bankAccountService),
                "Transfer-Standard-1"
        );

        Thread thread2 = new Thread(
                new TransferTask("ACC003", "ACC004", 10.0, bankAccountService),
                "Transfer-Standard-2"
        );

        thread1.setPriority(Thread.MAX_PRIORITY);
        thread2.setPriority(Thread.MIN_PRIORITY);
        thread1.start();
        thread2.start();
        return "ok";
    }
}
