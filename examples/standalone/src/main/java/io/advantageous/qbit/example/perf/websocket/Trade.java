package io.advantageous.qbit.example.perf.websocket;

public class Trade {

    private final String name;
    private final long amount;

    public Trade(String name, long amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public long getAmount() {
        return amount;
    }
}
