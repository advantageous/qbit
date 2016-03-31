package io.advantageous.qbit.time;

import java.util.concurrent.TimeUnit;

/**
 * This class is designed to create less parameters to methods that need durations (long + time unit).
 * Also it captures frequently occurring durations in QBit.
 */
public class Duration {


    public static final Duration NEVER = new Duration(Long.MAX_VALUE, TimeUnit.SECONDS);
    public static final Duration NANOSECONDS = new Duration(1, TimeUnit.NANOSECONDS);

    public static final Duration MINUTE = new Duration(1, TimeUnit.MINUTES);
    public static final Duration ONE_MINUTE = MINUTE;
    public static final Duration MINUTES = MINUTE;

    public static final Duration SECOND = new Duration(1, TimeUnit.SECONDS);
    public static final Duration ONE_SECOND = SECOND;
    public static final Duration SECONDS = SECOND;
    public static final Duration FIVE_SECONDS = SECONDS.multiply(5);
    public static final Duration TEN_SECONDS = SECONDS.multiply(10);
    public static final Duration HOUR = new Duration(1, TimeUnit.HOURS);
    public static final Duration ONE_HOUR = HOUR;
    public static final Duration HOURS = ONE_HOUR;
    public static final Duration TWO_HOURS = HOURS.multiply(2);
    public static final Duration FOUR_HOURS = HOUR.multiply(4);
    public static final Duration DAY = new Duration(1, TimeUnit.DAYS);
    public static final Duration DAYS = DAY;
    public static final Duration ONE_DAY = DAY;
    public static final Duration ONE_WEEK = DAY.units(7);
    public static final Duration MILLISECONDS = new Duration(1, TimeUnit.MILLISECONDS);
    public static final Duration TEN_MILLIS = MILLISECONDS.units(10);
    public static final Duration TWENTY_FIVE_MILLIS = MILLISECONDS.multiply(25);
    public static final Duration FIFTY_MILLIS = MILLISECONDS.multiply(50);
    public static final Duration HUNDRED_MILLIS = MILLISECONDS.units(100);

    /**
     * Holds units of time.
     */
    private final long duration;

    /**
     * Holds the time unit for this duration.
     */
    private final TimeUnit timeUnit;

    /**
     * @param duration duration
     * @param timeUnit time unit (Seconds, Hours, Days, Millis, Nanos)
     */
    public Duration(long duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    /**
     * Used to create second duration.
     *
     * @param units number of seconds
     * @return Duration in seconds
     */
    public static Duration seconds(long units) {
        return SECONDS.units(units);
    }

    /**
     * Used to create minute duration.
     *
     * @param units number of seconds
     * @return Duration in seconds
     */
    public static Duration minutes(long units) {
        return MINUTES.units(units);
    }

    /**
     * Used to create milliseconds duration.
     *
     * @param units number of milliseconds
     * @return Duration in milliseconds
     */
    public static Duration milliseconds(long units) {
        return MILLISECONDS.units(units);
    }

    /**
     * Used to create nanoseconds duration.
     *
     * @param units number of nanoseconds
     * @return Duration in nanoseconds
     */
    public static Duration nanoseconds(long units) {
        return NANOSECONDS.units(units);
    }

    /**
     * Used to create hours duration.
     *
     * @param units number of hours
     * @return Duration in hours
     */
    public static Duration hours(long units) {
        return HOURS.units(units);
    }

    /**
     * Used to create days duration.
     *
     * @param units number of days
     * @return Duration in days
     */
    public static Duration days(long units) {
        return DAYS.units(units);
    }

    /**
     * Get the duration.
     *
     * @return duration.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Get the time unit for the duration.
     *
     * @return time unit.
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }


    public long toMillis() {
        return timeUnit.toMillis(duration);
    }

    /**
     * Multiply this duration to create a new Duration.
     *
     * @param times times
     * @return Duration
     */
    public Duration multiply(long times) {
        return new Duration(duration * times, this.timeUnit);
    }


    /**
     * Multiply this duration to create a new Duration.
     *
     * @param units units
     * @return Duration
     */
    public Duration units(long units) {
        return new Duration(duration * units, this.timeUnit);
    }


    /**
     * Equals.
     *
     * @param o other object
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Duration duration1 = (Duration) o;

        return duration == duration1.duration && timeUnit == duration1.timeUnit;

    }


    /**
     * hashCode.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result = (int) (duration ^ (duration >>> 32));
        result = 31 * result + (timeUnit != null ? timeUnit.hashCode() : 0);
        return result;
    }

    /**
     * @return string representation of this object
     */
    @Override
    public String toString() {
        return "Duration{" +
                "duration=" + getDuration() +
                ", timeUnit=" + getTimeUnit() +
                '}';
    }
}
