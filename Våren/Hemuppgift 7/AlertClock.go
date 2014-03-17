package main

import (
	"fmt"
	"time"
)

func Remind(text string, paus time.Duration) {
	for {
		fmt.Println("Klockan är", time.Now().Format("15:04"), text)
		time.Sleep(paus)
	}

}

func main() {
	go Remind("Dags att äta", 3*time.Hour)
	go Remind("Dags att arbeta", 8*time.Hour)
	go Remind("Dags att sova", 24*time.Hour)
	select {}
}
