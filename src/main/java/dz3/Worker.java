package dz3;

import java.util.List;
import java.util.Random;

public class Worker implements Runnable {
    private final int workerId;
    private final DataCollector collector;
    private final List<BankAccount> accounts;
    private final TransferService transferService;
    private final Random rnd = new Random();

    public Worker(int workerId,
                  DataCollector collector,
                  List<BankAccount> accounts,
                  TransferService transferService) {
        this.workerId = workerId;
        this.collector = collector;
        this.accounts = accounts;
        this.transferService = transferService;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Item item = collector.takeItem();
                if (item == null) break; // collector закрыт и очередь пуста

                // антидубликат
                if (!collector.markProcessedIfNew(item.key())) {
                    continue;
                }

                // выбираем 2 счета случайно
                int a = rnd.nextInt(accounts.size());
                int b = rnd.nextInt(accounts.size());
                while (b == a) b = rnd.nextInt(accounts.size());

                BankAccount from = accounts.get(a);
                BankAccount to = accounts.get(b);

                long amount = Math.max(1, item.amount());

                // перевод
                try {
                    transferService.transfer(from, to, amount);
                } catch (IllegalStateException ignored) {
                    // недостаточно средств - пропустим, в реальной системе логировали бы
                }

                collector.incrementProcessed();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Worker " + workerId + " interrupted");
        }
    }
}