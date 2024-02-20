package main

import (
	"bufio"
	"fmt"
	"net/http"
	"sort"
	"strings"
	"sync"
)

func main() {
	urls := getURLs()

	responseCh := make(chan string)
	wordCountCh := make(chan map[string]int)

	var wg sync.WaitGroup

	go countWords(responseCh, wordCountCh)

	for _, url := range urls {
		wg.Add(1)
		go fetchURL(url, responseCh, &wg)
	}

	wg.Wait()

	close(responseCh)

	wordCount := <-wordCountCh

	printMostUsedWords(wordCount)
}

func fetchURL(url string, responseCh chan<- string, wg *sync.WaitGroup) {
	defer wg.Done()
	resp, err := http.Get(url)
	if err != nil {
		fmt.Printf("Failed to fetch %s: %v\n", url, err)
		return
	}
	defer resp.Body.Close()
	scanner := bufio.NewScanner(resp.Body)
	for scanner.Scan() {
		responseCh <- scanner.Text()
	}
}

func countWords(responseCh <-chan string, wordCountCh chan<- map[string]int) {
	wordCount := make(map[string]int)
	for text := range responseCh {
		words := strings.Fields(text)
		for _, word := range words {
			word = strings.ToLower(word)
			wordCount[word]++
		}
	}
	wordCountCh <- wordCount
}

func getURLs() []string {
	var urls []string
	for i := 1; i <= 135; i++ {
		url := fmt.Sprintf("https://dev-2024-conference-code-examples.vercel.app/sessions/%d", i)
		urls = append(urls, url)
	}
	return urls
}

func printMostUsedWords(wordCount map[string]int) {
	var ignoredWords = map[string]bool{
		"the":     true,
		"and":     true,
		"to":      true,
		"of":      true,
		"a":       true,
		"in":      true,
		"for":     true,
		"this":    true,
		"is":      true,
		"we":      true,
		"how":     true,
		"you":     true,
		"with":    true,
		"will":    true,
		"on":      true,
		"that":    true,
		"your":    true,
		"as":      true,
		"can":     true,
		"it":      true,
		"are":     true,
		"into":    true,
		"from":    true,
		"our":     true,
		"an":      true,
		"by":      true,
		"at":      true,
		"-":       true,
		"but":     true,
		"what":    true,
		"or":      true,
		"have":    true,
		"be":      true,
		"their":   true,
		"about":   true,
		"we'll":   true,
		"all":     true,
		"using":   true,
		"through": true,
		"use":     true,
		"not":     true,
		"these":   true,
		"they":    true,
		"also":    true,
		"if":      true,
		"up":      true,
		"more":    true,
		"i":       true,
		"like":    true,
		"has":     true,
		"where":   true,
	}

	for word := range wordCount {
		if ignoredWords[word] {
			delete(wordCount, word)
		}
	}

	var words []struct {
		Word  string
		Count int
	}
	for word, count := range wordCount {
		words = append(words, struct {
			Word  string
			Count int
		}{Word: word, Count: count})
	}

	sort.Slice(words, func(i, j int) bool {
		return words[i].Count > words[j].Count
	})

	for i := 0; i < len(words) && i < 10; i++ {
		fmt.Printf("%s: %d\n", words[i].Word, words[i].Count)
	}
}
