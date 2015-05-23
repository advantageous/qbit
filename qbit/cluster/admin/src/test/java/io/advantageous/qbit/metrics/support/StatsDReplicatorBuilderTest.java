package io.advantageous.qbit.metrics.support;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.metrics.StatReplicator;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class StatsDReplicatorBuilderTest {

    @Test
    public void test() throws Exception {

        // docker run -d   --name graphite   -p 80:80   -p 2003:2003   -p 8125:8125/udp   hopsoft/graphite-statsd

        //https://github.com/advantageous/qbit/wiki/%5BZ-Blog%5D-StatsD-and-QBit

        //.setHost("192.168.59.103")
        System.setProperty("qbit.statsd.replicator.host", "192.168.59.103");
        final StatReplicator statReplicator = StatsDReplicatorBuilder.statsDReplicatorBuilder().buildAndStart();

        final Random random = new Random();

        for (int index=0; index < 10; index++) {
            statReplicator.replicateCount("foo.bar", index+1, -1);
            statReplicator.replicateLevel("foo.bar2", index +1 * 100, -1);

            int duration = (int) (400 * random.nextFloat()) + 200;
            statReplicator.replicateTiming("foo.bar3", duration, -1);

            Sys.sleep(1000);
        }

        statReplicator.flush();

        Sys.sleep(1000);
    }
}