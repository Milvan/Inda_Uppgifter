package main

import (
	"fmt"
	"math"
)

func Sqrt(x float64) float64 {
	z := float64(4)
	diff := 1.0
	for diff > 0.0001 {
		a := z - (math.Pow(z, 2)-x)/(2*z)
		diff = math.Abs(z - a)
		// fmt.Println(diff) //this line was to check how many iterations were made and what the diff was.
		z = a
	}
	return z
}

func main() {
	fmt.Println("Newtons method")
	fmt.Println("Sqrt(2): ", Sqrt(2))
	fmt.Println("Sqrt(4): ", Sqrt(4))
	fmt.Println("Sqrt(5): ", Sqrt(5))
	fmt.Println("Sqrt(7): ", Sqrt(7))
	fmt.Println("Sqrt(16): ", Sqrt(16))
	fmt.Println()
	fmt.Println("Go math.Sqrt")
	fmt.Println("Sqrt(2): ", math.Sqrt(2))
	fmt.Println("Sqrt(4): ", math.Sqrt(4))
	fmt.Println("Sqrt(5): ", math.Sqrt(5))
	fmt.Println("Sqrt(7): ", math.Sqrt(7))
	fmt.Println("Sqrt(16): ", math.Sqrt(16))
}
