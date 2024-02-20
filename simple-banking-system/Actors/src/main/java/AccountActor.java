import akka.actor.AbstractActor;
import akka.actor.Props;
import messages.Deposit;
import messages.GetAccountID;
import messages.GetBalance;
import messages.Withdraw;

public class AccountActor extends AbstractActor {
  private int id;
  private int balance;

  public AccountActor(int id, int balance) {
    this.id = id;
    this.balance = balance;
  }

  static public Props props(int id, int balance) {
    return Props.create(AccountActor.class, () -> new AccountActor(id, balance));
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Deposit.class, deposit -> {
          balance += deposit.amount;
          System.out.println("Deposited " + deposit.amount + " to account " + id);
        })
        .match(Withdraw.class, withdraw -> {
          if (balance >= withdraw.amount) {
            balance -= withdraw.amount;
            System.out.println("Withdrew " + withdraw.amount + " from account " + id);
          }
        })
        .match(GetAccountID.class, getAccountID -> {
          getSender().tell(id, getSelf());
        })
        .match(GetBalance.class, getBalance -> {
          getSender().tell(balance, getSelf());
        })
        .build();
  }
}
