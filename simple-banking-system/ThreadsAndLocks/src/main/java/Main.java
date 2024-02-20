import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    final Account[] accounts = new Account[5];
    for (int i = 0; i < accounts.length; i++) {
      accounts[i] = new Account(i, 100);
    }

    ExecutorService executor = Executors.newFixedThreadPool(20);
    for (int i = 0; i < 1000; i++) {
      final int idx = (int) (Math.random() * 5);
      final int amount = (int) (Math.random() * 91) + 10;
      if (Math.random() > 0.5) {
        executor.execute(() -> {
          accounts[idx].deposit(amount);
        });
      } else {
        executor.execute(() -> {
          accounts[idx].withdraw(amount);
        });
      }
    }

    shutdownAndAwaitTermination(executor);

    System.out.println("\nFinal balances:");
    for (int i = 0; i < accounts.length; i++) {
      System.out.println("Account " + i + ": " + accounts[i].getBalance());
    }
  }

  public static void shutdownAndAwaitTermination(ExecutorService pool) {
    pool.shutdown();
    try {
      if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
        pool.shutdownNow();
        if (!pool.awaitTermination(60, TimeUnit.SECONDS))
          System.err.println("Pool did not terminate");
      }
    } catch (InterruptedException ex) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
