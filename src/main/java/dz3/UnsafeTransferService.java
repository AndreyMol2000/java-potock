package dz3;

public class UnsafeTransferService {
    public void transfer(BankAccount from, BankAccount to, long amount) {
        // НИКАКИХ synchronized -> возможны гонки
        if (from == to) return;
        from.withdraw(amount);
        to.deposit(amount);
    }
}