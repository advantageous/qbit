package io.advantageous.qbit.metrics.support;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.metrics.StatReplicator;
import org.junit.Test;

import java.util.Random;

public class StatsDReplicatorBuilderTest {

    @Test
    public void test() throws Exception {

        // docker run -d   --name graphite   -p 80:80   -p 2003:2003   -p 8125:8125/udp   hopsoft/graphite-statsd

        //https://github.com/advantageous/qbit/wiki/%5BZ-Blog%5D-StatsD-and-QBit

        //.setHost("192.168.59.103")
        System.setProperty("qbit.statsd.replicator.host", "192.168.59.103");
        final StatReplicator statReplicator = StatsDReplicatorBuilder.statsDReplicatorBuilder().buildAndStart();

        final Random random = new Random();

        //for (int index = 0; index < 200_000; index++) {
        for (int index = 0; index < 10_000; index++) {

            statReplicator.replicateCount("StatsDReplicatorBuilderTest.replicateCount", index + 1, -1);
            statReplicator.replicateLevel("StatsDReplicatorBuilderTest.replicateLevel", index + 1 * 100, -1);

            int duration = (int) (400 * random.nextFloat()) + 200;
            statReplicator.replicateTiming("StatsDReplicatorBuilderTest.replicateTiming", duration, -1);

            statReplicator.flush();
            Sys.sleep(1000);
        }

        Sys.sleep(1000);
    }
}