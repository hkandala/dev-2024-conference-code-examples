import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.actor.AbstractActor.Receive;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

public class OrchestratorActor extends AbstractActor {

  private final ActorRef wordCounterActor;
  private final List<ActorRef> fetcherActors = new ArrayList<>();

  private final int totalURLs;
  private int completed = 0;

  public OrchestratorActor() {
    List<String> urls = getURLs();
    totalURLs = urls.size();

    wordCounterActor = getContext().actorOf(Props.create(WordCounterActor.class), "wordCounter");

    urls.forEach(url -> {
      ActorRef fetcher = getContext().actorOf(Props.create(URLFetcherActor.class));
      fetcherActors.add(fetcher);
      fetcher.tell(new URLFetcherActor.Fetch(url, wordCounterActor), getSelf());
    });
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(WordCounterActor.ProcessComplete.class, this::onProcessComplete)
        .build();
  }

  private void onProcessComplete(WordCounterActor.ProcessComplete message) {
    completed++;
    if (completed == totalURLs) {
      wordCounterActor.tell(new WordCounterActor.PrintResults(), getSelf());
    }
  }

  private static List<String> getURLs() {
    List<String> urls = new ArrayList<>();
    for (int i = 1; i <= 135; i++) {
      urls.add("https://dev-2024-conference-code-examples.vercel.app/sessions/" + i);
    }
    return urls;
  }
}