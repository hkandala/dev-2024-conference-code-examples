main().catch(console.error);

async function main() {
  const urls = getURLs();
  const responses = await fetchURLContent(urls);

  const wordCount = new Map();
  responses.forEach((response) => updateWordCount(response, wordCount));

  printMostUsedWords(wordCount);
}

async function fetchURLContent(urls) {
  const fetch = (await import("node-fetch")).default;
  const fetchPromises = urls.map((url) => fetch(url).then((res) => res.text()));
  return Promise.all(fetchPromises);
}

function updateWordCount(text, wordCount) {
  const words = text.split(/[ \t\n\r\f]+/).map((word) => word.toLowerCase());
  words.forEach((word) => {
    wordCount.set(word, (wordCount.get(word) || 0) + 1);
  });
}

function printMostUsedWords(wordCount) {
  const sortedWords = Array.from(wordCount.entries())
    .filter((entry) => !IGNORED_WORDS.has(entry[0]))
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10);
  sortedWords.forEach(([word, count]) => {
    console.log(`${word}: ${count}`);
  });
}

function getURLs() {
  const urls = [];
  for (let i = 1; i <= 135; i++) {
    urls.push(
      `https://dev-2024-conference-code-examples.vercel.app/sessions/${i}`,
    );
  }
  return urls;
}

const IGNORED_WORDS = new Set([
  "the",
  "and",
  "to",
  "of",
  "a",
  "in",
  "for",
  "this",
  "is",
  "we",
  "how",
  "you",
  "with",
  "will",
  "on",
  "that",
  "your",
  "as",
  "can",
  "it",
  "are",
  "into",
  "from",
  "our",
  "an",
  "by",
  "at",
  "-",
  "but",
  "what",
  "or",
  "have",
  "be",
  "their",
  "about",
  "we'll",
  "all",
  "using",
  "through",
  "use",
  "not",
  "these",
  "they",
  "also",
  "if",
  "up",
  "more",
  "i",
  "like",
  "has",
  "where",
]);
