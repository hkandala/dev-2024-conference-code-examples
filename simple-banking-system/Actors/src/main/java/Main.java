import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import messages.Deposit;
import messages.GetBalance;
import messages.Withdraw;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;

public class Main {
  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("AccountSystem");
    final ActorRef[] accountActors = new ActorRef[5];
    for (int i = 0; i < accountActors.length; i++) {
      accountActors[i] = system.actorOf(AccountActor.props(i, 100));
    }

    ExecutorService executor = Executors.newFixedThreadPool(20);
    for (int i = 0; i < 1000; i++) {
      final int idx = (int) (Math.random() * 5);
      final int amount = (int) (Math.random() * 91) + 10;
      if (Math.random() > 0.5) {
        executor.execute(() -> {
          accountActors[idx].tell(new Deposit(amount), ActorRef.noSender());
        });
      } else {
        executor.execute(() -> {
          accountActors[idx].tell(new Withdraw(amount), ActorRef.noSender());
        });
      }
    }

    shutdownAndAwaitTermination(executor);

    final List<CompletableFuture<Object>> futureList = new ArrayList<>();
    for (int i = 0; i < accountActors.length; i++) {
      Future<Object> balanceFuture = Patterns.ask(accountActors[i], new GetBalance(), 300000);
      CompletableFuture<Object> completableFuture = FutureConverters.toJava(balanceFuture).toCompletableFuture();
      futureList.add(completableFuture);
    }
    CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));

    allDoneFuture.thenRun(() -> {
      System.out.println("\nFinal Balances:");
      for (int i = 0; i < futureList.size(); i++) {
        final int accountIndex = i;
        futureList.get(i).thenAccept(balance -> System.out.println("Account " + accountIndex + ": " + balance));
      }
    });

    system.terminate();
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
