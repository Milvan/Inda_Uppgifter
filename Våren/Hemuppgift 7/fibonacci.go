package main

import "fmt"

// fibonacci is a function that returns
// a function that returns an int.
func fibonacci() func() int {
	curr, prev := 0,1
	return func() int {
		prev, curr = curr, prev+curr
		return curr
	}
}

// print 10 first fibonacci numbers.
func main() {
	f := fibonacci()
	for i := 0; i < 10; i++ {
		fmt.Println(f())
	}
}
