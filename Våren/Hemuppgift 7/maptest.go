package main

import (
	"code.google.com/p/go-tour/wc"
	"strings"
)

func WordCount(s string) map[string]int {
	m := make(map[string]int)
	for _, a := range strings.Fields(s) {
		_, ok := m[a]
		if ok {
			m[a]++
		} else {
			m[a] = 1
		}
	}
	return m
}

func main() {
	wc.Test(WordCount)
}
