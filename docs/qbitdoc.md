QBit

QBit is a queuing library for services.
It is similar to many other projects.
QBit is just a library not a platform.
QBit has libraries to put a service behind a queue.
You can use QBit queues directly or you can create a service.
A service is a Java class whose methods are executed via queues.
QBit implements apartment model threading and is similar to the Actor.
QBit does not use disruptor. It uses regular Java Queues.
QBit can do 100 million ping pong calls per second.
QBit also supports calling services via REST, and WebSocket.

#QBit Overview 1

* library for services
* library not a platform or framework
* allows putting service behind a queue
* services are only accessed by one thread
* No thread sync is typically needed in services

#QBit Overview 2

* You can use QBit queues directly
* or you can create a service
* Embeddable (can work in Tomcat or Vertx or Spring Boot)
* Service is a Java class whose methods are executed via queues

#QBit Overview 3


* implements apartment model threading and is similar to Actors
* Does not use disruptor
* Uses regular Java Queues
* Fast 100 million ping pong calls per second

#QBit Overview 4


* Supports calling services via REST, and WebSocket
* Uses batching to reduce thread hand off to queues
* Items to be processed are collected and sent in batches not one at a time
* Batching reduces thread sync time and accessing shared variables (volatile)

#QBit queue example

```java

        BasicQueue<Integer> queue = BasicQueue.create(1000);

/* In another thread */

        SendQueue<Integer> sendQueue = queue.sendQueue();
        sendQueue.send(index); /* send an item but sends them in batches */

        //Flush sends every so often (in timer or ...)
        sendQueue.flushSends();
        //Send and do an immediate flush
        sendQueue.sendAndFlush(code);

/* In another thread */
        ReceiveQueue<Integer> receiveQueue = queue.receiveQueue();
        Integer item = receiveQueue.take();


```


#QBit Flush/Batch

* There is automatic flush support at some layers
* More is being added

#QBit Service Example

Todo list.

#Todo Item

```java


 public class TodoItem {


    private final String description;
    private final String name;
    private final Date due;

    public TodoItem(final String description, final String name,
                    final Date due) {
        this.description = description;
        this.name = name;
        this.due = due;
    }

    public String getDescription() { return description; }

    public String getName() { return name; }

    public Date getDue() { return due; }
 }


```

#Todo Service Class

```java

@RequestMapping("/todo-manager")
public class TodoService {

    private final TodoRepository todoRepository =
               new ListTodoRepository();

    @RequestMapping("/todo/list")
    public List<TodoItem> list() {

        return todoRepository.list();
    }

    @RequestMapping(value = "/todo",
                  method = RequestMethod.POST)
    public void add(TodoItem item) {

        todoRepository.add(item);
    }
}

```

#Todo Service Class

* Exposes service under URI `/todo-manager`
* exposes method list under `/todo-manager/list`
* exposes add under `/todo-manager/todo`

#Server code

```java

public class TodoServerMain {

    public static void main(String... args) {
        ServiceServer server =
                  new ServiceServerBuilder().build();
        server.initServices(new TodoService());
        server.start();

    }
}

```

#ServiceServer Builder

```java

public class ServiceServerBuilder {

    private String host = "localhost";
    private int port = 8080;
    private boolean manageQueues = true;
    private int pollTime = 100;
    private int requestBatchSize = 10;
    private int flushInterval = 100;
    private String uri = "/services";


    public ServiceServer build() {...

```


#ServiceServer Builder

* ServiceServer Builder builds a service server.
* `flushInterval` is how often you want it to flush queue batches
* `requestBatchSize` is how large you would like the batch to the queue
* `uri` is the root URI
* `pollTime` is a low level on how long you would it to park between queue polls
*  More params will be exposed. (pipelining, HTTP compression,
websocket buffer size)


#Client Code REST POST Todo Items

```java

        TodoItem todoItem = new TodoItem("Go to work",
                "Get on ACE train and go to Cupertino",
                new Date());

        final String addTodoURL =
                "http://" + host + ":" + port + "/services/todo-manager/todo";

        final String readTodoListURL
                = "http://" + host + ":" + port + "/services/todo-manager/todo/list";

        //HTTP POST
        HTTP.postJSON(addTodoURL, Boon.toJson(todoItem));

        todoItem = new TodoItem("Call Jefe", "Call Jefe", new Date());

        //HTTP POST
        HTTP.postJSON(addTodoURL, Boon.toJson(todoItem));

```


#REST Client Code read TODO items

```java

        //HTTP GET
        final String todoJsonList =
                HTTP.getJSON(readTodoListURL, null);

        final List<TodoItem> todoItems =
                Boon.fromJsonArray(todoJsonList, TodoItem.class);

        for (TodoItem todo : todoItems) {
            puts(todo.getName(), todo.getDescription(), todo.getDue());
        }


```

#Websocket client

```java


        Client client = new Client(host, port, "/services");
        TodoServiceClient todoService =
          client.createProxy(TodoServiceClient.class, "todo-manager");

        client.run();

        /* Add a new item. */
        todoService.add(new TodoItem("Buy Milk", ...);
        todoService.add(new TodoItem("Buy Hot dogs", ...);

        /* Read the items back. */
        todoService.list(todoItems -> { //LAMBDA EXPRESSION Java 8

            for (TodoItem item : todoItems) {
                puts (item.getDescription(), item.getName(), item.getDue());
            }
        });


```


#Websocket client

* Needs builder like ServiceServer.
* ClientServiceBuilder will build ServiceClient
* Creates proxy
* Proxy allows async callbacks

#Websocket Client Proxy Interface

```java


public interface TodoServiceClient {

        void list(Callback<List<TodoItem>> handler);

        void add(TodoItem todoItem);


}


```

#Callback

```
public interface Callback<T> extends Consumer<T> {

    default void onError(Throwable error) {

        LoggerFactory.getLogger(Callback.class)
                .error(error.getMessage(), error);
    }
}


```

#QBit designed to be pluggable

* Could be used with Spring Boot or Spring MVC
* Can be used in Tomcat
* Can be used in Vertx
* Can be run standalone
* Can be run without websocket REST

#QBit Works with any class no annotations needed

```java

        SomeInterface myService = new SomeInterface() ...

        final Factory factory  = QBit.factory();
        final ServiceBundle bundle = factory.createServiceBundle("/root");


        bundle.addService(myService);


        final SomeInterface myServiceProxy =
              bundle.createLocalProxy(SomeInterface.class, "myService");

        myServiceProxy.method2("hi", 5);


```

#QBit series of factories, interfaces and builders allow plug-ability


```

public interface Factory {

    JsonMapper createJsonMapper();

    HttpServer createHttpServer(...);

    HttpClient createHttpClient(...);

    ServiceServer createServiceServer(...);

```

#Factory SPI

```java


public class FactorySPI {

    public static Factory getFactory() { ... }
    public static void setFactory(Factory factory) { ... }
    public static HttpServerFactory getHttpServerFactory() { ... }
    public static void setHttpServerFactory(HttpServerFactory factory) { ... }
    public static void setHttpClientFactory(HttpClientFactory factory) { ... }
    public static HttpClientFactory getHttpClientFactory() { ... }

```
Discovery mechanism finds factories and implementations.


#Complex REST mappings

```java

    @RequestMapping("/boo/baz")
    class Foo {

        @RequestMapping("/some/uri/with-uri-params/{0}/{1}/")
        public void someMethod(String a, int b) {

            methodCalled = true;
            puts("called a", a, "b", b);
        }
    }


```


#Internals

* Service is a queue system for a service
* ServiceBundle is a collection of Services
* You can work with Service directly w/o a proxy


#Example working with Service Directly (INTERNAL)

```java

    public static class Adder {
        int add(int a, int b) { ... } //your implementation
        void queueIdle() { ... } //optional callback
        void queueEmpty() { ... } //optional callback
        void queueShutdown() { ... } //optional callback
        void queueLimit() { ... } //optional callback
    }

```


#Using a Service (INTERNAL)

```java


        Service service = Services.regularService("test", adder, 1000,
                       TimeUnit.MILLISECONDS, 10);
        SendQueue<MethodCall<Object>> requests = service.requests();
        ReceiveQueue<Response<Object>> responses = service.responses();

        requests.send(MethodCallImpl.method("add", Lists.list(1, 2)));

        requests.sendAndFlush(MethodCallImpl.methodWithArgs("add", 4, 5));

        Response<Object> response = responses.take();
        Object o = response.body();


```


#Batching method calls (INTERNAL)

```java

        Service service = Services.regularService("test", adder, ...);
        SendQueue<MethodCall<Object>> requests = service.requests();
        ReceiveQueue<Response<Object>> responses = service.responses();

        List<MethodCall<Object>> methods = new ArrayList<>();

        for (int index = 0; index < 1000; index++) {
            methods.add(MethodCallImpl.method("add", Lists.list(1, 2)));
        }

        requests.sendBatch(methods);
```

#Using JSON From Service (INTERNAL)

```java


        Adder adder = new Adder();
        Service service = Services.jsonService("test", adder, ...;

        ReceiveQueue<Response<Object>> responses = service.responses();
        SendQueue<MethodCall<Object>> requests = service.requests();



        requests.send(MethodCallImpl.method("add", "[1,2]"));

        requests.send(MethodCallImpl.method("add", "[4,5]"));
        requests.flushSends();

```


#Using JSON from Service Bundle (Internal)

```

    ServiceBundle serviceBundle = QBit.factory().createServiceBundle("/services");
    serviceBundle.addService(new TodoService());

    Todo todoItem = new Todo("call mom", "give mom a call",
                new Date());

    MethodCall<Object> addMethod = QBit.factory()
                .createMethodCallByAddress("/services/todo-manager/add", "client1",
                todoItem, null);

    serviceBundle.call(addMethod);

    MethodCall<Object> listMethod = QBit.factory()
                .createMethodCallByAddress("/services/todo-manager/list", "client1",
                null, null);
    serviceBundle.call(listMethod);
    serviceBundle.flushSends();
    //Handle returns
    ReceiveQueue<Response<Object>> responses = serviceBundle.responses().receiveQueue();
    Response<Object> response = responses.take();

```



#HTTP Client fast Async part of QBIT

```java

                    final HttpClient client = new HttpClientBuilder().setPort(port)
                            .setHost(host)
                            .setPoolSize(poolSize).setRequestBatchSize(batchSize).
                                    setPollTime(pollTime).build();
                    client.run();


                    client.sendHttpRequest(perfRequest);

                    client.flush();

                    client.stop();


```


#HTTP Request Builder

```java

        final HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder();

        final HttpRequest perfRequest = httpRequestBuilder
                                        .setContentType("application/json")
                                        .setMethod("GET").setUri("/perf/")
                                        .setResponse((code, mimeType, body) -> {
                                            if (code != 200 || !body.equals("\"ok\"")) {
                                                errorCount.increment();
                                                return;
                                            }

                                            receivedCount.increment();


                                        })
                                        .build();

        client.sendHttpRequest(perfRequest);


```


#HTTP Client Builder

```java

public class HttpClientBuilder {


    private String host = "localhost";
    private int port = 8080;
    private int poolSize = 5;
    private int pollTime = 10;
    private int requestBatchSize = 10;
    private int timeOutInMilliseconds=3000;
    private boolean autoFlush = true;

    public HttpClient build(){...}


```


#HTTP Server

```java

        final HttpServer server = new HttpServerBuilder()
                                    .setPort(port)
                                    .setHost(host)
                                    .setPollTime(pollTime)
                                    .setRequestBatchSize(batchSize)
                                    .setFlushInterval(flushRate)

                                    .setHttpRequestConsumer(request -> {

                                        if (request.getUri().equals("/perf/")) {
                                            request.getResponse()
                                            .response(200, "application/json",
                                            "\"ok\"");
                                        }
                                    }).build();


        server.start();


```


#HTTP Server

* Implementations in Vertx and Netty
* Faster than Tomcat and Jetty (on benchmark tests I wrote)
* Faster than Vertx alone on some tests


#HTTP Server Builder

```java


public class HttpServerBuilder {

    private String host = "localhost";
    private int port = 8080;
    private boolean manageQueues = true;
    private int pollTime = 100;
    private int requestBatchSize = 10;
    private int flushInterval = 100;

    public HttpServer build(){...}
    
```

#Using callbacks 1

```java

    public static interface MyServiceInterfaceForClient {

        void method1();

        void method2(String hi, int amount);

        void method3(Callback<String> handler, String hi, int amount);
    }

```



#Using callbacks 2

```java

        @RequestMapping("myService")
        class MyServiceClass implements SomeInterface {
            @Override
            public void method1() {

            }

            @Override
            public void method2(String hi, int amount) {

            }

            @Override
            public String method3(String hi, int amount) {
                return "Hi" + hi + " " + amount;
            }
        }

```



#Using callbacks 3

```java

       SomeInterface myService = new MyServiceClass();


        final Factory factory  = QBit.factory();
        final ServiceBundle bundle = factory.createServiceBundle("/root");


        bundle.addService(myService);
        bundle.startReturnHandlerProcessor();



        final MyServiceInterfaceForClient myServiceProxy = bundle.createLocalProxy(
                MyServiceInterfaceForClient.class,
                "myService");

```



#Using callbacks 4

```java

       Callback<String> returnHandler = new Callback<String>() {
            @Override
            public void accept(String returnValue) {

                puts("We got", returnValue);

                ok = "Hihi 5".equals(returnValue);

            }
        };
        myServiceProxy.method3(returnHandler, "hi", 5);
        bundle.flushSends();


```
