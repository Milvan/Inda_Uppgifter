// Stefan Nilsson 2013-03-13 modified by Marcus Larsson 2014-03-30

// This program implements an ELIZA-like oracle (en.wikipedia.org/wiki/ELIZA).
package main

import (
	"bufio"
	"fmt"
	"math/rand"
	"os"
	"strings"
	"time"
)

const (
	star   = "Pythia"
	venue  = "Delphi"
	prompt = "> "
)

var (
	answerTable    = make(map[string]string)
	wordCategories = make(map[string]string)
	nonsense       = []string{
		"The moon is dark.",
		"The sun is bright.",
		"The cold water is deep",
		"There is a dark soul the sky",
		"The water is clear",
		"Slow sunrise, enjoy your life!",
		"There are great people who makes other feel bad, but the truly great people are those who make others feel great!",
		"It's never about what it is. It's about how you react.",
		"Take care of your body. It's the only place you can't flee from",
		"There is noone in this world who's better than you in beeing you than yourself",
	}
)

func main() {
	fmt.Printf("Welcome to %s, the oracle at %s.\n", star, venue)
	fmt.Println("Your questions will be answered in due time.")

	oracle := Oracle()
	reader := bufio.NewReader(os.Stdin)
	for {
		fmt.Print(prompt)
		line, _ := reader.ReadString('\n')
		line = strings.TrimSpace(line)
		if line == "" {
			continue
		}
		fmt.Printf("%s heard: %s\n", star, line)
		oracle <- line // The channel doesn't block.
	}
}

// Oracle returns a channel on which you can send your questions to the oracle.
// You may send as many questions as you like on this channel, it never blocks.
// The answers arrive on stdout, but only when the oracle so decides.
// The oracle also prints sporadic prophecies to stdout even without being asked.
func Oracle() chan<- string {
	questions := make(chan string)
	answers := make(chan string)
	createAnswerTable()

	go func() {
		for {
			time.Sleep(30 * time.Second)
			generateProphecy(answers)
		}
	}()

	go func() {
		for {
			select {
			case s := <-questions:
				go prophecy(s, answers)
			}
		}
	}()

	go printAnswers(answers)

	return questions
}

// This will map words to categories and answers to categories in maps.
func createAnswerTable() {
	//first create a word list and categorize words.
	wordCategories["wealth"] = "Wealth"
	wordCategories["wealthy"] = "Wealth"
	wordCategories["rich"] = "Wealth"
	wordCategories["rik"] = "Wealth"
	wordCategories["money"] = "Wealth"
	wordCategories["cash"] = "Wealth"

	wordCategories["death"] = "Death"
	wordCategories["die"] = "Death"
	wordCategories["dead"] = "Death"
	wordCategories["död"] = "Death"

	wordCategories["thanks"] = "Thanks"
	wordCategories["thank"] = "Thanks"

	wordCategories["hi"] = "Hi"
	wordCategories["hey"] = "Hi"
	wordCategories["hej"] = "Hi"
	wordCategories["yo"] = "Hi"
	wordCategories["greetings"] = "Hi"

	//answers on categories
	answerTable["Wealth"] = "That is greedy. Aim for happiness and you will be wealthy"
	answerTable["Death"] = "We all will die. Make sure you think it was worth living"
	answerTable["Thanks"] = "You're welcome"
	answerTable["Hi"] = "Hi, what would you like to know?"
}

// Generates a random prophecy
func generateProphecy(answers chan string) {
	answers <- "... " + nonsense[rand.Intn(len(nonsense))]
}

// Prints all the answers. "human like" with a variated delay between typing characters.
func printAnswers(answers chan string) {
	for s := range answers {
		fmt.Printf("\n%s says: ", star)
		for _, c := range s {
			time.Sleep(time.Duration(120*rand.Intn(4)) * time.Millisecond)
			fmt.Print(string(c))
		}

		fmt.Printf("\n%s", prompt)
	}
}

// This is the oracle's secret algorithm.
// It waits for a while and then sends a message on the answer channel.
func prophecy(question string, answer chan<- string) {

	// Keep them waiting. Pythia, the original oracle at Delphi,
	// only gave prophecies on the seventh day of each month.
	time.Sleep(time.Duration(5+rand.Intn(20)) * time.Second)

	//Try to find a smart answer to the question
	smartAnswer := smartAnswer(question)

	if smartAnswer != "" {
		answer <- smartAnswer
	} else {
		// Find the longest word.
		longestWord := ""
		words := strings.Fields(question) // Fields extracts the words into a slice.
		for _, w := range words {
			if len(w) > len(longestWord) {
				longestWord = w
			}
		}
		answer <- longestWord + "... " + nonsense[rand.Intn(len(nonsense))]
	}
}

// Checks sentence in wordlist and gives back an answer corresponding to the category of the first recognised word in the question.
func smartAnswer(question string) string {
	s:=Stripchars(question, ",.=()!?")
	for _, word := range strings.Fields(strings.ToLower(s)) {
		category, ok := wordCategories[word]
		if ok {
			if category!="Hi"{
			return category + "... " + answerTable[category]
			}else {
			return answerTable[category]
			}
		}
	}
	return ""
}

//This method will delete all the charachters in the string chr from the string str
func Stripchars(str, chr string) string {
    return strings.Map(func(r rune) rune {
        if strings.IndexRune(chr, r) < 0 {
            return r
        }
        return -1
    }, str)
}

func init() { // Functions called "init" are executed before the main function.
	// Use new pseudo random numbers every time.
	rand.Seed(time.Now().Unix())
}
