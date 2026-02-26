package ru.molchanov.demo.task;

import ru.molchanov.demo.servise.BankAccountService;

import static ru.molchanov.demo.utils.TaskSimulateWork.simulatecpuWork;

public class TransferTask implements Runnable{
    private final String fromAccountNumber;
    private final String  toAccountNumber;
    private final double ammount;
    private final BankAccountService bankAccountService;

    public TransferTask(String fromAccountNumber, String toAccountNumber, double ammount, BankAccountService bankAccountService) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.ammount = ammount;
        this.bankAccountService = bankAccountService;
    }

    @Override
    public void run(){
        try {
            System.out.println("поток" + Thread.currentThread().getName()  +
                    "стартовал приоритет " + Thread.currentThread().getPriority());
            simulatecpuWork("easy task " , 2000);
            bankAccountService.tranfer(fromAccountNumber,toAccountNumber , ammount);
        }catch (Exception e){
            System.err.println("ошибка в потоке" + Thread.currentThread().getName() + ":" +e.getMessage());
            throw  new RuntimeException(e);
        }
    }

}
