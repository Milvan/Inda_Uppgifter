package main

import (
	"fmt"
)

// Add adds the numbers in a and sends the result on res.
func Add(a []int, res chan<- int) {
	sum := 0
	for _, t := range a {
		sum += t
	}
	res <- sum

}

// Calculates the sum of all numbers in an array with parrallell programming.
func main() {
	a := []int{1, 2, 3, 4, 5, 6, 7}

	n := len(a)
	ch := make(chan int)
	go Add(a[:n/2], ch)
	go Add(a[n/2:], ch)

	x, y := <-ch, <-ch

	fmt.Println("Sum is:", x+y)

}
