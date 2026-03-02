package dz3;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== SAFE RUN ✅ ===");
        runTest(true);

        System.out.println("\n=== UNSAFE RUN ❌ ===");
        runTest(false);
    }

    private static void runTest(boolean safe) throws InterruptedException {
        int accountsCount = 20;
        int workersCount = 8;
        int itemsCount = 200_000;

        // аккаунты
        List<BankAccount> accounts = new ArrayList<>();
        for (int i = 0; i < accountsCount; i++) {
            accounts.add(new BankAccount(i + 1, 100_000));
        }

        long initialTotal = totalBalance(accounts);

        DataCollector collector = new DataCollector();

        TransferService safeService = new TransferService();
        UnsafeTransferService unsafeService = new UnsafeTransferService();

        // воркеры
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < workersCount; i++) {
            int id = i;

            Runnable r = () -> {
                try {
                    while (true) {
                        Item item = collector.takeItem();
                        if (item == null) break;

                        if (!collector.markProcessedIfNew(item.key())) continue;

                        // примитив: используем key чтобы не было повторов, amount как сумма
                        int a = (int)(Math.abs(item.key().hashCode()) % accounts.size());
                        int b = (a + 1 + (item.amount() % (accounts.size() - 1))) % accounts.size();

                        BankAccount from = accounts.get(a);
                        BankAccount to = accounts.get(b);

                        long amount = Math.max(1, item.amount());

                        try {
                            if (safe) safeService.transfer(from, to, amount);
                            else unsafeService.transfer(from, to, amount);
                        } catch (Exception ignored) {}

                        collector.incrementProcessed();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            workers.add(new Thread(r, "worker-" + id));
        }

        long start = System.nanoTime();

        // запуск
        for (Thread t : workers) t.start();

        // продюсер: накидываем items
        for (int i = 0; i < itemsCount; i++) {
            // специально добавим дубликаты ключей, чтобы проверка isAlreadyProcessed работала
            String key = "item-" + (i % (itemsCount / 2));
            int amount = (i % 50) + 1;
            collector.collectItem(new Item(key, amount));
        }

        // закрываем collector (данных больше не будет)
        collector.close();

        for (Thread t : workers) t.join();

        long end = System.nanoTime();
        long ms = (end - start) / 1_000_000;

        long finalTotal = totalBalance(accounts);

        System.out.println("processedCount = " + collector.getProcessedCount());
        System.out.println("timeMs = " + ms);
        System.out.println("initialTotal = " + initialTotal);
        System.out.println("finalTotal   = " + finalTotal);
        System.out.println("delta        = " + (finalTotal - initialTotal));
    }

    private static long totalBalance(List<BankAccount> accounts) {
        long sum = 0;
        for (BankAccount a : accounts) sum += a.getBalance();
        return sum;
    }
}