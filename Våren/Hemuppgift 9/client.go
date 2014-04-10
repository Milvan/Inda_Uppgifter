package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"time"
)

func main() {
	server := []string{
		"http://localhost:8080",
		"http://localhost:8081",
		"http://localhost:8082",
	}
	for {
		before := time.Now()
		//res := Get(server[0])
		//res := Read(server[0], time.Second)
		res := MultiRead(server, time.Second)
		after := time.Now()
		fmt.Println("Response:", *res)
		fmt.Println("Time:", after.Sub(before))
		fmt.Println()
		time.Sleep(500 * time.Millisecond)
	}
}
type Response struct {
	Body       string
	StatusCode int
}

// Get makes an HTTP Get request and returns an abbreviated response.
// Status code 200 means that the request was successful.
// The function returns &Response{"", 0} if the request fails
// and it blocks forever if the server doesn't respond.
func Get(url string) *Response {
	res, err := http.Get(url)
	if err != nil {
		return &Response{}
	}
	// res.Body != nil when err == nil
	defer res.Body.Close()
	body, err := ioutil.ReadAll(res.Body)
	if err != nil {
		log.Fatalf("ReadAll: %v", err)
	}
	return &Response{string(body), res.StatusCode}
}

// Read makes an HTTP Get request to the given url and returns the response.
// If the server does not respond within the timeout the response is Gateway timeout
// with code 504
func Read(url string, timeout time.Duration) (res *Response) {
	done := make(chan *Response, 1)
	go func() {
		r := Get(url) //memory leak if server does not respond. Go routine will still exist but be blocked. This bug is in http.Get and I cannot fix it.
		done <- r
	}()
	select {
	case res=<-done:
	case <-time.After(timeout):
		res = &Response{"Gateway timeout\n", 504}
	}
	return
}

// MultiRead makes an HTTP Get request to each url and returns
// the response of the first server to answer with status code 200.
// If none of the servers answer before timeout, the response is
// 503 â€“ Service unavailable.
func MultiRead(urls []string, timeout time.Duration) (res *Response) {
	response := make(chan *Response, len(url))
	for _, url := range urls {

		go func(u string){
				r:=Get(u)
				if r.StatusCode==200{
					response<-r
				}
				
			}(url)
	}
	select {
	case res=<-response:
	case <-time.After(timeout):
		res = &Response{"Service unavailable\n", 503}
	}
	return 
}
