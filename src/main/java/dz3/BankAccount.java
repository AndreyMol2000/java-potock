package dz3;

public class BankAccount {
    private final long id;
    private long balance;

    public BankAccount(long id, long initialBalance) {
        this.id = id;
        this.balance = initialBalance;
    }

    public long getId() { return id; }

    public long getBalance() {
        return balance;
    }

    void deposit(long amount) {
        balance += amount;
    }

    void withdraw(long amount) {
        if (balance < amount) throw new IllegalStateException("Not enough funds");
        balance -= amount;
    }
}