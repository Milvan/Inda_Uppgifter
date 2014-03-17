package main

import "code.google.com/p/go-tour/pic"

func Pic(dx, dy int) [][]uint8 {
	a := make([][]uint8, dy, dy)
	for i := 0; i < dy; i++ {
		row := make([]uint8, dx, dx)
		for j := 0; j < dx; j++ {
			row[j] = uint8(i * j)
		}
		a[i] = row
	}
	return a
}

func main() {
	pic.Show(Pic)
}
