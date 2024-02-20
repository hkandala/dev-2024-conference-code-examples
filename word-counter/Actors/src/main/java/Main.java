import akka.actor.ActorSystem;
import akka.actor.Props;

public class Main {
  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("WordCountSystem");
    system.actorOf(Props.create(OrchestratorActor.class), "orchestratorActor");
  }
}