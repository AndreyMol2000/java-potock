package dz3;

public class TransferService {

    public void transfer(BankAccount from, BankAccount to, long amount) {
        if (from == to) return;

        BankAccount first = from.getId() < to.getId() ? from : to;
        BankAccount second = from.getId() < to.getId() ? to : from;

        synchronized (first) {
            synchronized (second) {
                // ВАЖНО: логика внутри двойной блокировки
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }
}