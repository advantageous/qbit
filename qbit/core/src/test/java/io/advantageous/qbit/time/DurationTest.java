package io.advantageous.qbit.time;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DurationTest {

    @Test
    public void test() {

        Duration d = Duration.FIFTY_MILLIS;
        assertTrue(d.getDuration() == 50 && d.getTimeUnit() == TimeUnit.MILLISECONDS);

        d = Duration.HUNDRED_MILLIS;
        assertTrue(d.getDuration() == 100 && d.getTimeUnit() == TimeUnit.MILLISECONDS);


        d = Duration.MILLISECONDS;
        assertTrue(d.getDuration() == 1 && d.getTimeUnit() == TimeUnit.MILLISECONDS);


        d = Duration.TEN_MILLIS;
        assertTrue(d.getDuration() == 10 && d.getTimeUnit() == TimeUnit.MILLISECONDS);


        d = Duration.TWENTY_FIVE_MILLIS;
        assertTrue(d.getDuration() == 25 && d.getTimeUnit() == TimeUnit.MILLISECONDS);

        d = Duration.ONE_SECOND;
        assertTrue(d.getDuration() == 1 && d.getTimeUnit() == TimeUnit.SECONDS);


        d = Duration.FIVE_SECONDS;
        assertTrue(d.getDuration() == 5 && d.getTimeUnit() == TimeUnit.SECONDS);

        d = Duration.TEN_SECONDS;
        assertTrue(d.getDuration() == 10 && d.getTimeUnit() == TimeUnit.SECONDS);


        d = Duration.ONE_HOUR;
        assertTrue(d.getDuration() == 1 && d.getTimeUnit() == TimeUnit.HOURS);


        d = Duration.TWO_HOURS;
        assertTrue(d.getDuration() == 2 && d.getTimeUnit() == TimeUnit.HOURS);


        d = Duration.FOUR_HOURS;
        assertTrue(d.getDuration() == 4 && d.getTimeUnit() == TimeUnit.HOURS);


        d = Duration.ONE_DAY;
        assertTrue(d.getDuration() == 1 && d.getTimeUnit() == TimeUnit.DAYS);


        d = Duration.ONE_WEEK;
        assertTrue(d.getDuration() == 7 && d.getTimeUnit() == TimeUnit.DAYS);


        assertEquals(Duration.TEN_SECONDS, Duration.TEN_SECONDS);
        assertEquals(Duration.TEN_SECONDS, Duration.SECONDS.units(10));
        assertEquals(Duration.TEN_SECONDS.hashCode(), Duration.SECONDS.units(10).hashCode());
        assertEquals(Duration.TEN_SECONDS, Duration.seconds(10));
        assertEquals(Duration.DAYS.multiply(10), Duration.days(10));
        assertEquals(Duration.HOURS.multiply(10), Duration.hours(10));
        assertEquals(Duration.MILLISECONDS.multiply(10), Duration.milliseconds(10));
        assertEquals(Duration.NANOSECONDS.multiply(10), Duration.nanoseconds(10));

        System.out.println(Duration.TEN_SECONDS);

    }

}