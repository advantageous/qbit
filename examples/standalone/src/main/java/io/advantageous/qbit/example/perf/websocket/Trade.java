package io.advantageous.qbit.example.perf.websocket;

public class Trade {

    private final String nm;
    private final long amt;

    public Trade(String name, long amount) {
        this.nm = name;
        this.amt = amount;
    }

    public String getNm() {
        return nm;
    }

    public long getAmt() {
        return amt;
    }
}
