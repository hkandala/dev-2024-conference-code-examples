import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

  private static final int NUM_PRODUCERS = 20;
  private static final int NUM_CONSUMERS = 20;

  private static final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
  private static final Lock lock = new ReentrantLock();

  private static final Map<String, Integer> wordCount = new HashMap<>();

  public static void main(String[] args) {
    final HttpClient client = HttpClient.newHttpClient();

    List<String> urls = getURLs();

    ExecutorService producerService = Executors.newFixedThreadPool(NUM_PRODUCERS);
    ExecutorService consumerService = Executors.newFixedThreadPool(NUM_CONSUMERS);

    // Producer Threads
    urls.forEach(url -> producerService.submit(() -> {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .GET()
          .build();
      try {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        responseQueue.put(response.body());
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }));

    // Consumer Threads
    for (int i = 0; i < NUM_CONSUMERS; i++) {
      consumerService.submit(() -> {
        try {
          while (true) {
            String response = responseQueue.take();
            StringTokenizer tokenizer = new StringTokenizer(response);
            while (tokenizer.hasMoreTokens()) {
              String word = tokenizer.nextToken().toLowerCase();
              lock.lock();
              try {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
              } finally {
                lock.unlock();
              }
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    shutdownAndAwaitTermination(producerService);
    shutdownAndAwaitTermination(consumerService);

    printMostUsedWords(wordCount);
  }

  private static List<String> getURLs() {
    List<String> urls = new ArrayList<>();
    for (int i = 1; i <= 135; i++) {
      urls.add("https://dev-2024-conference-code-examples.vercel.app/sessions/" + i);
    }
    return urls;
  }

  private static void printMostUsedWords(Map<String, Integer> wordCount) {
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
  }

  private static void shutdownAndAwaitTermination(ExecutorService pool) {
    pool.shutdown();
    try {
      if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
        pool.shutdownNow();
        if (!pool.awaitTermination(30, TimeUnit.SECONDS))
          System.err.println("Pool did not terminate");
      }
    } catch (InterruptedException ex) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}