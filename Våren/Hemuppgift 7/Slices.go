package main

import "code.google.com/p/go-tour/pic"

// This will generate a two-dimensional slice that indicates the bluescale
// of every pixel in a picture.
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
