package io.advantageous.qbit.perf;

/**
 * Created by Richard on 12/7/14.
 */

public class AdderService implements Adder {

    int sum = 0;

    @Override
    public void add(String name, int value) {

        sum += value;
    }

    @Override
    public int sum() {
        return sum;
    }
}
