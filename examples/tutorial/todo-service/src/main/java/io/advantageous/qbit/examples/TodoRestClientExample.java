package io.advantageous.qbit.examples;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpResponse;

import java.util.Date;

import static io.advantageous.boon.core.IO.puts;

public class TodoRestClientExample {

    public static void main(String... args) {
        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder().setPort(8080).build();
        httpClient.startClient();

        final HttpResponse httpResponse = httpClient.get("/services/todo-service/todo/count");
        puts("COUNT BEFORE", httpResponse.body());

        httpClient.postJson(
                "/services/todo-service/todo",
                JsonFactory.toJson(new TodoItem("description", "name", new Date())));


        httpClient.postJson(
                "/services/todo-service/todo",
                JsonFactory.toJson(new TodoItem("description1", "name1", new Date())));


        httpClient.postJson(
                "/services/todo-service/todo",
                JsonFactory.toJson(new TodoItem("description2", "name2", new Date())));

        final HttpResponse after = httpClient.get("/services/todo-service/todo/count");
        puts("COUNT After", after.body());

        final HttpResponse listFromJson = httpClient.get("/services/todo-service/todo/");

        puts("LIST After", listFromJson.body());


        puts("LIST After", JsonFactory.fromJsonArray(listFromJson.body(), TodoItem.class));

    }
}
