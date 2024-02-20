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

public class Main {

  public static void main(String[] args) {
    final HttpClient client = HttpClient.newHttpClient();

    List<String> urls = getURLs();

    final List<String> responses = new ArrayList<>();
    final Map<String, Integer> wordCount = new HashMap<>();

    // Fetch each URL content sequentially and store in responses list
    urls.forEach(url -> {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .GET()
          .build();
      try {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        responses.add(response.body());
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    });

    // Loop through each response, tokenize and populate the word count map
    responses.forEach(response -> {
      StringTokenizer tokenizer = new StringTokenizer(response);
      while (tokenizer.hasMoreTokens()) {
        String word = tokenizer.nextToken().toLowerCase();
        wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
      }
    });

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
}