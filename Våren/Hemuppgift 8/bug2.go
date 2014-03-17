package main

import "fmt"

// This program should go to 11, but sometimes it only prints 1 to 10.
func main() {
	ch := make(chan int)
	wait := make(chan struct{}) // added a wait channel that receives a signal when printing is done.
	go Print(ch, wait)
	for i := 1; i <= 11; i++ {
		ch <- i
	}
	close(ch)
	<-wait //wait until printing is done before exiting program.
}

// Print prints all numbers sent on the channel.
// The function returns when the channel is closed.
func Print(ch <-chan int, wait chan struct{}) {
	for n := range ch { // reads from channel until it's closed
		fmt.Println(n)
	}
	close(wait)
}
