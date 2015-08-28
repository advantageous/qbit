package io.advantageous.qbit.service.stats;


public class Stats {
    private final float mean;
    private final float stdDev;
    private final float variance;
    private final long sum;
    private final long max;
    private final long min;
    private final long median;

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

    public long getSum() {
        return sum;
    }

    public long getMax() {
        return max;
    }

    public long getMin() {
        return min;
    }

    public long getMedian() {
        return median;
    }

    public int getReadingCount() {
        return readingCount;
    }
}
