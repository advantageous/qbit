package io.advantageous.qbit.example.event.bus;

public class MyEvent {

    private String id;
    private String message;


    public String getId() {
        return id;
    }

    public MyEvent setId(String id) {
        this.id = id;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MyEvent setMessage(String message) {
        this.message = message;
        return this;
    }
}
