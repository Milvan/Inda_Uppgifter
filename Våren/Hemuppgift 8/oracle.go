// Stefan Nilsson 2013-03-13

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
	timeForProphecy := make(chan int)

	// select{case <-questions(answer), case <-Waithexceed(Prophecy) }
	//go time for prophecy (waitexcced)
	go func() {
		for {
			time.Sleep(50 * time.Second)
			timeForProphecy <- 1
		}
	}()

	go func() {
		for {
			select {
			case s := <-questions:
				go prophecy(s, answers)
			case _ = <-timeForProphecy:
				go prophecy("", answers)

			}
		}
	}()

	// TODO: Answer questions.
	//go func(){
	//			for s := range questions {
	//go answer(s, answers)
	//}
	//}()

	// TODO: Make prophecies.
	//go prophecy("", answers)
	// TODO: Print answers.
	go printAnswers(answers)

	return questions
}

//Wealth... That is greedy. Aim for happiness and you will be wealthy
//rik... That is greedy. Aim for happiness and you will be wealthy
func answer(question string, answers chan<- string) {
	time.Sleep(time.Duration(2+rand.Intn(10)) * time.Second)
	answers <- question
}

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
// TODO: make it better.
func prophecy(question string, answer chan<- string) {
	
	

	// Keep them waiting. Pythia, the original oracle at Delphi,
	// only gave prophecies on the seventh day of each month.
	time.Sleep(time.Duration(5+rand.Intn(20)) * time.Second)

	// Find the longest word.
	longestWord := ""
	words := strings.Fields(question) // Fields extracts the words into a slice.
	for _, w := range words {
		if len(w) > len(longestWord) {
			longestWord = w
		}
	}

	// Cook up some pointless nonsense.
	nonsense := []string{
		"The moon is dark.",
		"The sun is bright.",
		"The cold water is deep",
		"There is a dark soul the sky",
		"The water is clear",
		"Slow sunrise, enjoy your life!", 
	}
	answer <- longestWord + "... " + nonsense[rand.Intn(len(nonsense))]
}

//"Det finns de som får andra att känna sig små, men den som är verkligt stor själv är den som får andra att känna sig stora."
//"Det är inte överraskningen som har betydelse, det är hur du reagerar på den."
//"Ta hand om din kropp. Den är den enda plats du har att leva"
//"Du vet inte vad som kommer att hända i morgon. Livet är en galen åktur, och ingenting är garanterat."
//"Varför jämföra dig med andra? Ingen annan i hela världen kan göra ett bättre jobb på att vara du än du."

func init() { // Functions called "init" are executed before the main function.
	// Use new pseudo random numbers every time.
	rand.Seed(time.Now().Unix())
}
