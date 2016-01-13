package io.advantageous.qbit.util;

import io.advantageous.boon.core.Sys;
import org.junit.Test;

import java.time.Clock;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertTrue;

public class TimerTest {

    @Test
    public void testTimer() throws Exception {

        long now = Timer.timer().now();

        long currentTime = Clock.systemUTC().millis();

        Sys.sleep(1_000);

        puts(now, currentTime);


        for (int index = 0; index < 5; index++) {
            Sys.sleep(1000);

            now = Timer.timer().now();

            currentTime = Clock.systemUTC().millis();

            puts(now, currentTime, now - currentTime);
            assertTrue(Math.abs(now - currentTime) < 1000); //adjusted this to run on travis, should be much less than this when you have CPUs
        }

    }
}