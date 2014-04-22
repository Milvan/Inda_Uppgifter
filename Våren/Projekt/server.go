package main

//    import (
//	"fmt"
//	"log"
//	"net/http"
//)
//
//
//// Server
//
//func main() {
//	for _, port := range []string{":8080", ":8081", ":8082"} {
//		s := &http.Server{
//			Addr:    port,
//			Handler: http.HandlerFunc(Serve),
//		}
//		go func() {
//			log.Printf("starting server at localhost%s", s.Addr)
//			err := s.ListenAndServe()
//			if err != nil {
//				log.Fatalf("ListenAndServe: %v", err)
//			}
//		}()
//	}
//	select {} // Block forever.
//}
//
//func Serve(w http.ResponseWriter, r *http.Request) {
//		fmt.Fprintln(w, "Testing the connection")
//	}

import (
    "fmt"
    "log"
    "net"
    "bufio"
    //"io"
    
)

const listenAddr = "localhost:4000"
var (
clients []*Client
joins chan net.Conn
incomming chan string
//outgoing chan string
)

type Client struct{
    incomming chan string
    outgoing chan string
    reader *bufio.Reader
    writer *bufio.Writer
}

func (client *Client) Listen(){
    go client.Read()
    go client.Write()
}

func(client *Client) Read(){
    for {
        //line, err := client.reader.ReadBytes('\n')
        
        line, err := client.reader.ReadString('\n')
        if err != nil { return } //Remove client from clients in here!
        client.incomming <- line
    }
}

func (client *Client) Write() {
    for data := range client.outgoing {
        client.writer.WriteString(data)
        client.writer.Flush()
    }
}

func NewClient(conn net.Conn) *Client{
    read := bufio.NewReader(conn)
    write := bufio.NewWriter(conn)
    client := &Client{
        incomming: make(chan string),
        outgoing: make(chan string),
        reader: read,
        writer: write,
    }
    client.Listen()
    return client
}

func main() {
    clients = make([]*Client, 0)
    joins = make(chan net.Conn)
    incomming = make(chan string)
    //outgoing = make(chan string)

    l, err := net.Listen("tcp", listenAddr)
    if err != nil {
        log.Fatal(err)
    }
    listen()
    for {
        c, err := l.Accept()
        if err != nil {
            log.Fatal(err)
        }
        

        //go handleIncomming(s)
        go func(){joins<-c}()
        
    }  
}

func Broadcast(data string) {
    for _, client := range clients {
        client.outgoing <- data
    }
}

func listen(){
    go func(){
        for{
            select{
            case data := <-incomming:
                log.Println(data)
                Broadcast(data)
            case conn := <-joins:
                join(conn)
            }
        }
    }()
}

func join(conn net.Conn){
    client := NewClient(conn)

    clients = append(clients, client)
    go func(){for{incomming<- <-client.incomming}}()
    fmt.Printf("Client connected:")
    client.outgoing<- "Hello!"
}
