package main

import "fmt"

// fibonacci is a function that returns
// a function that returns an int.
func fibonacci() func() int {
	curr := 0
	prev := 0
	return func() int {
		if curr == 0 {
			curr = 1
			return 1
		}
		res := curr + prev
		prev = curr
		curr = res
		return res
	}
}

// print 10 first fibonacci numbers.
func main() {
	f := fibonacci()
	for i := 0; i < 10; i++ {
		fmt.Println(f())
	}
}
