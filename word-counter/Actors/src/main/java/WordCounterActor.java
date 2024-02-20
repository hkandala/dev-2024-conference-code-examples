import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class WordCounterActor extends AbstractActor {

  static class CountWords {
    public final String content;
    public final ActorRef orchestratorRef;

    public CountWords(String content, ActorRef orchestratorRef) {
      this.content = content;
      this.orchestratorRef = orchestratorRef;
    }
  }

  static class PrintResults {
  }

  static class ProcessComplete {
  }

  private final Map<String, Integer> wordCount = new HashMap<>();

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(CountWords.class, this::onCountWords)
        .match(PrintResults.class, this::onPrintResults)
        .build();
  }

  private void onCountWords(CountWords message) {
    StringTokenizer tokenizer = new StringTokenizer(message.content);
    while (tokenizer.hasMoreTokens()) {
      String word = tokenizer.nextToken().toLowerCase();
      wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
    }
    message.orchestratorRef.tell(new ProcessComplete(), getSelf());
  }

  private void onPrintResults(PrintResults message) {
    Set<String> IGNORED_WORDS = Set.of(
        "the", "and", "to", "of", "a", "in", "for", "this", "is", "we", "how", "you", "with", "will", "on", "that",
        "your", "as", "can", "it", "are", "into", "from", "our", "an", "by", "at", "-", "but", "what", "or", "have",
        "be", "their", "about", "we'll", "all", "using", "through", "use", "not", "these", "they", "also", "if", "up",
        "more", "i", "like", "has", "where");

    wordCount.entrySet().stream()
        .filter(entry -> !IGNORED_WORDS.contains(entry.getKey()))
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(10)
        .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

    getContext().getSystem().terminate();
  }
}