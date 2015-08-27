package io.advantageous.qbit.metrics;

import io.advantageous.qbit.metrics.support.StatList;

public class Stats {
    private final float mean;
    private final float stdDev;
    private final float variance;
    private final int sum;
    private final int max;
    private final int min;
    private final int median;

    private final int readingCount;

    public Stats(StatList statList) {
        this.mean = statList.mean();
        this.stdDev = statList.standardDeviation();
        this.variance = statList.variance();
        this.sum = statList.sum();
        this.max = statList.max();
        this.min = statList.min();
        this.median = statList.median();
        this.readingCount = statList.size();
    }

    public float getMean() {
        return mean;
    }

    public float getStdDev() {
        return stdDev;
    }

    public float getVariance() {
        return variance;
    }

    public int getSum() {
        return sum;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public int getMedian() {
        return median;
    }

    public int getReadingCount() {
        return readingCount;
    }
}
