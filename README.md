#qbit - The Microservice Lib for Java - JSON, REST, WebSocket, Speed! [![Build Status](https://travis-ci.org/advantageous/qbit.svg)](https://travis-ci.org/advantageous/qbit)

Got a question? Ask here: [QBit Google Group](https://groups.google.com/forum/#!forum/qbit-microservice).

Everything is a queue. You have a choice. You can embrace it and control it. You can optimize for it. 
Or you can hide behind abstractions. QBit opens you up to peeking into what is going on, and allows you to pull some levers without selling your soul.

QBit is a library not a framework. You can mix and match QBit with Spring, Guice, etc.

QBit is FAST!

![QBit the microservice framework for java](https://docs.google.com/spreadsheets/d/1kd3gjyyz1MyTJvNLJ-BC0YIkzIU-8YYLLrxpjUl0TBQ/pubchart?oid=781959089&format=image)

Status
=====
Lot's of progress. More people are helping out. 
QBit now works with Vertx (standalone or embedded), Jetty (standalone) or just plain Java Servlets.


License
=====
Apache 2


QBit philosiphy:
====
At the end of the day QBit is a simpley library not a framework. 
Your app is not a QBit app but a Java app that uses the QBit lib.
QBit allows you to work with Java UTIL concurrent, and does not endeavor to hide it from you.
Just trying to take the sting out of it. 

Does it work
=====
We have used techniques in Boon and QBit with great success in high-end, high-performance, high-scalable apps. 
We helped clients handle 10x the load with 1/10th the servers of their competitors using techniques in QBit.
QBit is us being sick of hand tuning queue access and threads.



Single Writer, Mulit Write with CPU bound writer do this, this and this.
Single Writer, Multi Writer with IO bound writer do this, this and this.
and so on and so on...


Boon and QBit humility policy
=====
Ideas for Boon and QBit often come from all over the web. We make mistakes. Point them out. 
As a developer of Boon and QBit, we are fellow travelers. 
If you have an idea or technique you want to share, we listen.


Inspiration
====

A big inspireation for Boon/QBit was Akka, Go Channels, Active Objects, Apartment Model Threading, Actor, and Mechnical Sympathy papers.

"I have read the AKKA in Action Book. It was inpsiring, but not the only inspiration for QBit.".
"I have written apps where I promised a lot of performance and the techniques from QBit is how I got it."
 - Rick Hightower
 
QBit has ideas that are similar to many frameworks. We are all reading the same papers. 
Most of the inpiration for QBit was the LMAX disruptor papers and this blog http://php.sabscape.com/blog/?p=557.
We had some theories about queues that this blog post (http://php.sabscape.com/blog/?p=557) inprired us to try out. Some of these theories are deployed at some of the biggest middleware backends and whose name brands are known around the world. 
 
Does QBit compete with...
====
Spring Disruptor: No. You could use QBit to write plugins for Spring Disruptor I suppose, but QBit does not compete with Spring Disruptor.
Akka: No. Ditto
LMAX Disruptor: No.

(Early benchmarks have been removed. QBit got a lot faster.)


Code Examples


## Basic Queue example:

====

```java

     BasicQueue<Integer> queue =  BasicQueue.create(Integer.class, 1000);
    
    //Sending threads
     
     SendQueue<Integer> sendQueue = queue.sendQueue();
     for (int index = 0; index < amount; index++) {
           sendQueue.send(index);
     }
     sendQueue.flushSends();
     ...
     sendQueue.sendAndFlush(code);
     //other methods for sendQueue, writeBatch, writeMany


     //Recieving Threads
     ReceiveQueue<Integer> receiveQueue = queue.receiveQueue();
     Integer item = receiveQueue.take(); 
     //other methods poll(), pollWait(), readBatch(), readBatch(count)
```


### What is QBit again?

QBit is a queuing library for microservices. It is similar to many other projects like Akka, Spring Reactor, etc. QBit is just a library not a platform. QBit has libraries to put a service behind a queue. You can use QBit queues directly or you can create a service. QBit services can be exposed by WebSocket, HTTP, HTTP pipeline, and other types of remoting. A service in QBit is a Java class whose methods are executed behind service queues. QBit implements apartment model threading and is similar to the Actor model or a better description would be Active Objects. QBit does not use a disruptor. It uses regular Java Queues. QBit can do north of 100 million ping pong calls per second which is an amazing speed (seen as high as 200M). QBit also supports calling services via REST, and WebSocket. QBit is microservices in the pure Web sense: JSON, HTTP, WebSocket, etc. 

### QBit lingo

QBit is a Java microservice lib supporting REST, JSON and WebSocket. It is written in Java but I might one day write a version in Rust or Go or C# (but that would require a large payday).

**Service** 
POJO (plain old Java object) behind a queue that can receive method calls via proxy calls or events (May have one thread managing events, method calls, and responses or two one for method calls and events and the other for responses so response handlers do not block service. One is faster unless responses block). Services can use Spring MVC style REST annotations to expose themselves to the outside world via REST and WebSocket. 

**ServiceBundle** 
Many POJOs behind one response queue and many receive queues. There may be one thread for all responses or not. They also can be one receive queue. 

**Queue**
A thread managing a queue. It supports batching. It has events for empty, reachedLimit, startedBatch, idle. You can listen to these events from services that sit behind a queue. You don't have to use Services. You can use Queue's direct.

**ServiceServer**
ServiceBundle that is exposed to REST and WebSocket communication

**EventBus**
EventBus is a way to send a lot of messages to services that may be loosely coupled

**ClientProxy** 
Way to invoke service through async interface, service can be inproc (same process) or remoted over WebSocket.

**Non-blocking**
QBit is a non-blocking lib. You use CallBacks via Java 8 Lambdas. You can also send event messages and get replies. Messaging is built into the system so you can easily coordinate complex tasks. 


**Speed**
There is a lot of room for improvement with Speed. But already QBit is VERY fast.
 200M+ TPS inproc ping pong, 10M-20M+ TPS event bus, 500K TPS RPC calls over WebSocket/JSON, etc.
More work needs to be done to improve speed, but now it is fast enough where I am working more with usability.


### QBit CURLable REST services example

Talk is cheap. Let's look at some code. You can get a detailed walk through in the Wiki.
We have a lot of documentation already.

To query the size of the todo list:

```bash
curl localhost:8080/services/todo-service/todo/count
```

To add a new TODO item.

```bash
curl -X POST -H "Content-Type: application/json" -d '{"name":"xyz","decription":"xyz"}' http://localhost:8080/services/todo-service/todo 
```

To get a list of TODO items
```bash
curl http://localhost:8080/services/todo-service/todo/
```

#### Todo POJO sans getter

```java
package io.advantageous.qbit.examples;

import java.util.Date;


public class TodoItem {


    private final String description;
    private final String name;
    private final Date due;

```

#### Todo Service
```java

@RequestMapping("/todo-service")
public class TodoService {


    private List<TodoItem> todoItemList = new ArrayList<>();


    @RequestMapping("/todo/count")
    public int size() {

        return todoItemList.size();
    }

    @RequestMapping("/todo/")
    public List<TodoItem> list() {

        return todoItemList;
    }

    @RequestMapping(value = "/todo", method = RequestMethod.POST)
    public void add(TodoItem item) {

        todoItemList.add(item);
    }

}

```


Now just start it up.

```java

    public static void main(String... args) {
        ServiceServer server = new ServiceServerBuilder().build();
        server.initServices(new TodoService());
        server.start();
    }
```

That is it. There is also out of the box WebSocket support with client side proxy generation so you can call into services at the rate of millions of calls per second.

See this full example here: [QBit microservice getting started tutorial](https://github.com/advantageous/qbit/wiki/%5BDetailed-Tutorial%5D-------------QBit-microservice-example).


## Working with WebSocket, HttpClient etc.

QBit has a library for working with and writing async microservices that is lightweight and fun to use.

#### WebSocket server and client.


#### Create an HTTP server
```java

        /* Create an HTTP server. */
        HttpServer httpServer = httpServerBuilder()
                .setPort(8080).build();

```

#### Setup server WebSocket support
```java
        /* Setup WebSocket Server support. */
        httpServer.setWebSocketOnOpenConsumer(webSocket -> {
            webSocket.setTextMessageConsumer(message -> {
                webSocket.sendText("ECHO " + message);
            });
        });

```

#### Start the server
```java

        /* Start the server. */
        httpServer.start();
```

#### Setup the WebSocket client
```java

        /** CLIENT. */

        /* Setup an httpClient. */
        HttpClient httpClient = httpClientBuilder()
                .setHost("localhost").setPort(8080).build();
        httpClient.start();
```

#### Client WebSocket

```java

        /* Setup the client websocket. */
        WebSocket webSocket = httpClient
                .createWebSocket("/websocket/rocket");

        /* Setup the text consumer. */
        webSocket.setTextMessageConsumer(message -> {
            System.out.println(message);
        });
        webSocket.openAndWait();

        /* Send some messages. */
        webSocket.sendText("Hi mom");
        webSocket.sendText("Hello World!");

```

#### Output
```output

ECHO Hi mom
ECHO Hello World!

```

Now stop the server and client. Pretty easy eh?

## High-speed HTTP client and server done microservice style


##### Starting up an HTTP server

```java

        /* Create an HTTP server. */
        HttpServer httpServer = httpServerBuilder()
                .setPort(8080).build();

        /* Setting up a request Consumer with Java 8 Lambda expression. */
        httpServer.setHttpRequestConsumer(httpRequest -> {

            Map<String, Object> results = new HashMap<>();
            results.put("method", httpRequest.getMethod());
            results.put("uri", httpRequest.getUri());
            results.put("body", httpRequest.getBodyAsString());
            results.put("headers", httpRequest.getHeaders());
            results.put("params", httpRequest.getParams());
            httpRequest.getReceiver()
                .response(200, "application/json", Boon.toJson(results));
        });


        /* Start the server. */
        httpServer.start();


```

The focus is on ease of use and using Java 8 Lambdas for callbacks so the code is tight and small.

[Find out more about QBit's microservice style websocket support here](https://github.com/advantageous/qbit/wiki/%5BDoc%5D-Using-QBit-microservice-lib's-WebSocket-support)


## Using HTTP Client lib

Now, let's try out our HTTP client. 

##### Starting up an HTTP client

```java

        /* Setup an httpClient. */
        HttpClient httpClient = httpClientBuilder()
                  .setHost("localhost").setPort(8080).build();
        httpClient.start();
```

You just pass the URL, the port and then call start.

## Synchronous HTTP calls

Now you can start sending HTTP requests. 

##### No Param HTTP GET

```java
        /* Send no param get. */
        HttpResponse httpResponse = httpClient.get( "/hello/mom" );
        puts( httpResponse );
```

An HTTP response just contains the results from the server.


##### No Param HTTP Response

```java
public interface HttpResponse {

    MultiMap<String, String> headers();

    int code();

    String contentType();

    String body();

}

```

There are helper methods for sync HTTP GET calls.



##### Helper methods for GET
```java


        /* Send one param get. */
        httpResponse = httpClient.getWith1Param("/hello/singleParam", 
                                        "hi", "mom");
        puts("single param", httpResponse );


        /* Send two param get. */
        httpResponse = httpClient.getWith2Params("/hello/twoParams",
                "hi", "mom", "hello", "dad");
        puts("two params", httpResponse );


        /* Send two param get. */
        httpResponse = httpClient.getWith3Params("/hello/3params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids");
        puts("three params", httpResponse );


        /* Send four param get. */
        httpResponse = httpClient.getWith4Params("/hello/4params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids",
                "yo", "pets");
        puts("4 params", httpResponse );

        /* Send five param get. */
        httpResponse = httpClient.getWith5Params("/hello/5params",
                "hi", "mom",
                "hello", "dad",
                "greetings", "kids",
                "yo", "pets",
                "hola", "neighbors");
        puts("5 params", httpResponse );


```

The puts method is a helper method it does System.out.println more or less by the way.

The first five params are covered. Beyond five, you have to use the HttpBuilder.

```java


        /* Send six params with get. */

        final HttpRequest httpRequest = httpRequestBuilder()
                .addParam("hi", "mom")
                .addParam("hello", "dad")
                .addParam("greetings", "kids")
                .addParam("yo", "pets")
                .addParam("hola", "pets")
                .addParam("salutations", "all").build();

        httpResponse = httpClient.sendRequestAndWait(httpRequest);
        puts("6 params", httpResponse );
```

## Http Async

There are async calls for GET as well.

#### Async calls for HTTP GET using Java 8 lambda

```java

        /* Using Async support with lambda. */
        httpClient.getAsync("/hi/async", (code, contentType, body) -> {
            puts("Async text with lambda", body);
        });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.getAsyncWith1Param("/hi/async", "hi", "mom", (code, contentType, body) -> {
            puts("Async text with lambda 1 param\n", body);
        });

        Sys.sleep(100);



        /* Using Async support with lambda. */
        httpClient.getAsyncWith2Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                (code, contentType, body) -> {
                    puts("Async text with lambda 2 params\n", body);
                });

        Sys.sleep(100);




        /* Using Async support with lambda. */
        httpClient.getAsyncWith3Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                (code, contentType, body) -> {
                    puts("Async text with lambda 3 params\n", body);
                });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.getAsyncWith4Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                "p4", "v4",
                (code, contentType, body) -> {
                    puts("Async text with lambda 4 params\n", body);
                });

        Sys.sleep(100);


        /* Using Async support with lambda. */
        httpClient.getAsyncWith5Params("/hi/async",
                "p1", "v1",
                "p2", "v2",
                "p3", "v3",
                "p4", "v4",
                "p5", "v5",
                (code, contentType, body) -> {
                    puts("Async text with lambda 5 params\n", body);
                });

        Sys.sleep(100);

```

[Find more about the easy to use, fast microservice HTTP client here](https://github.com/advantageous/qbit/wiki/%5BDoc%5D-Using-QBit-microservice-lib's-HttpClient-GET,-POST,-et-al,-JSON,-Java-8-Lambda).




