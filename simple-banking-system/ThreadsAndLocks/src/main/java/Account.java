import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
  private int id;
  private int balance;

  private final Lock lock = new ReentrantLock();

  public Account(int id, int balance) {
    this.id = id;
    this.balance = balance;
  }

  public void deposit(int amount) {
    lock.lock();
    try {
      balance += amount;
      System.out.println("Deposited " + amount + " to account " + id);
    } finally {
      lock.unlock();
    }
  }

  public void withdraw(int amount) {
    lock.lock();
    try {
      if (balance >= amount) {
        balance -= amount;
        System.out.println("Withdrew " + amount + " from account " + id);
      }
    } finally {
      lock.unlock();
    }
  }

  public int getAccountID() {
    return id;
  }

  public int getBalance() {
    return balance;
  }
}
