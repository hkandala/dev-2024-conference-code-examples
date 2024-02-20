import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class URLFetcherActor extends AbstractActor {

  static class Fetch {
    public final String url;
    public final ActorRef counterRef;

    public Fetch(String url, ActorRef counterRef) {
      this.url = url;
      this.counterRef = counterRef;
    }
  }

  private final HttpClient client = HttpClient.newHttpClient();

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Fetch.class, this::onFetch)
        .build();
  }

  private void onFetch(Fetch message) {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(message.url))
          .GET()
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      message.counterRef.tell(new WordCounterActor.CountWords(response.body(), getSender()), getSelf());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
