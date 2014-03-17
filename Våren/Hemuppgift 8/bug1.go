package main

import "fmt"

// I want this program to print "Hello world!", but it doesn't work.
func main() {
	ch := make(chan string)
	go func() { fmt.Println(<-ch) }()
	ch <- "Hello world!"

}
