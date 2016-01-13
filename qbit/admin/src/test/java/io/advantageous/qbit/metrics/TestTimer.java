package io.advantageous.qbit.metrics;

import io.advantageous.qbit.util.Timer;


/**
 * Timer for testing
 * created by rhightower on 3/19/15.
 */
public class TestTimer extends Timer {

    private long time = 0;

    @Override
    public long time() {
        return (time == 0) ? Timer.timer().time() : time;
    }

    @Override
    public long now() {
        return time();
    }

    public void setTime() {
        time = Timer.timer().time();
    }

    public TestTimer useRealTime() {
        time = 0;
        return this;
    }

    public TestTimer minutes(int minutes) {
        long delta = minutes * 60 * 1000;

        time = time + delta;
        return this;
    }


    public TestTimer days(int days) {
        long delta = days * 60 * 60 * 1000 * 24;

        time = time + delta;
        return this;
    }


    public TestTimer hours(int hours) {
        long delta = hours * 60 * 60 * 1000;

        time = time + delta;
        return this;
    }


    public TestTimer seconds(int seconds) {
        long delta = seconds * 1000;

        time = time + delta;
        return this;
    }


    public TestTimer ms(int delta) {
        time = time + delta;
        return this;
    }

    public int unixTime() {
        long t = time();
        return (int) (t / 1000);
    }

}
