package qbit.support;

import java.util.Arrays;

/**
 * Created by rhightower on 1/28/15.
 */
public class MinuteStat {

    private final String name;
    private final long startTime;
    private long endTime;
    private final int [] secondCounts;
    private int totalCount;

    public MinuteStat(long now, String name) {
        startTime = now;

        secondCounts = new int[60];
        this.name = name;

    }


    public int countLastSecond(long now) {
        int secondIndex = secondIndex(now);


        if (secondIndex -1 >= secondCounts.length) {
            return Integer.MIN_VALUE;
        }

        return secondCounts[secondIndex-1];
    }


    public int countThisSecond(long now) {
        int secondIndex = secondIndex(now);


        if (secondIndex >= secondCounts.length) {
            return Integer.MIN_VALUE;
        }

        return secondCounts[secondIndex];
    }

    public int changeBy(int count, long now) {
        totalCount += count;

        int secondIndex = secondIndex(now);


        if (secondIndex >= secondCounts.length) {
            return -1;
        }

        secondCounts[secondIndex]+=count;

        endTime = now;
        return totalCount;
    }

    private int secondIndex(long now) {

        if (now >= startTime && now < (startTime + 60*1000)) {
            return (int) ((now - startTime) / 1000);
        } else  {
             return Integer.MAX_VALUE;
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int[] getSecondCounts() {
        return secondCounts;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "MinuteMeasurement{" +
                "name='" + getName() + '\'' +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", secondCounts=" + Arrays.toString(getSecondCounts()) +
                ", totalCount=" + getTotalCount() +
                '}';
    }

    public int getTotalCount() {
        return totalCount;
    }
}
