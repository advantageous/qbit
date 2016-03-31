package io.advantageous.qbit.service.health;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.util.TestTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.advantageous.qbit.service.health.HealthServiceBuilder.healthServiceBuilder;
import static org.junit.Assert.assertTrue;


public class HealthServiceQueueTest {

    TestTimer timer;
    HealthServiceAsync healthService;


    HealthServiceBuilder healthServiceBuilder;


    @Before
    public void setUp() throws Exception {

        timer = new TestTimer();

        healthServiceBuilder = healthServiceBuilder();
        healthService = healthServiceBuilder.setTimer(timer).setAutoFlush().buildAndStart();


    }

    @Test
    public void testRegister() throws Exception {


        final AtomicBoolean result = new AtomicBoolean();

        healthService.register("foo", 1, TimeUnit.SECONDS);

        final CountDownLatch countDownLatch = new CountDownLatch(1);


        healthService.findAllNodes(names -> {
                    result.set(names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );


        countDownLatch.await(10, TimeUnit.SECONDS);


        assertTrue("foo is found", result.get());


        healthService.unregister("foo");

        final CountDownLatch countDownLatch2 = new CountDownLatch(1);

        result.set(false);
        healthService.findAllNodes(names -> {
                    result.set(!names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch2.countDown();
                }
        );


        countDownLatch2.await(10, TimeUnit.SECONDS);

    }

    @Test
    public void testCheckInOk() throws Exception {


        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean();

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkInOk("foo");


        healthService.findHealthyNodes(names -> {
                    result.set(names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );

        countDownLatch.await(10, TimeUnit.SECONDS);

        assertTrue("foo is in healthy list", result.get());


    }


    @Test
    public void testCheckInOkUsingCheckIn() throws Exception {


        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean();


        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);


        healthService.findHealthyNodes(names -> {
                    result.set(names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );

        ServiceProxyUtils.flushServiceProxy(healthService);

        countDownLatch.await(10, TimeUnit.SECONDS);


        assertTrue("foo is found among the healthy ",
                result.get());

    }


    @Test
    public void transitionFromPassToFail() throws Exception {


        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean();


        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);


        healthService.findHealthyNodes(names -> {
                    result.set(names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );


        countDownLatch.await(10, TimeUnit.SECONDS);


        healthService.checkIn("foo", HealthStatus.FAIL);


        final CountDownLatch countDownLatch2 = new CountDownLatch(1);
        result.set(false);


        healthService.findHealthyNodes(names -> {
                    result.set(!names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch2.countDown();
                }
        );


        countDownLatch2.await(10, TimeUnit.SECONDS);


        assertTrue("foo is NOT found among the healthy ",
                result.get());

    }


    @Test
    public void forceTTLExpire() throws Exception {

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean();

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);


        healthService.findAllNodes(names -> {
                    result.set(names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );
        countDownLatch.await(10, TimeUnit.SECONDS);
        assertTrue("foo is found", result.get());


        timer.setTime();
        timer.minutes(1);

        Sys.sleep(1000);
        healthService.clientProxyFlush();


        final CountDownLatch countDownLatch2 = new CountDownLatch(1);

        result.set(false);

        healthService.findHealthyNodes(names -> {
                    result.set(!names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch2.countDown();
                }
        );
        countDownLatch2.await(10, TimeUnit.SECONDS);
        assertTrue("foo should not be found", result.get());


    }


    @Test
    public void fail() {

        /* This is why we need to log it. */
        healthService.checkIn("no exist", HealthStatus.PASS);


    }


}