package io.advantageous.qbit.service.health;

import io.advantageous.boon.core.Sys;
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
    CountDownLatch countDownLatch;
    AtomicBoolean result;

    HealthServiceBuilder healthServiceBuilder;


    @Before
    public void setUp() throws Exception {

        timer = new TestTimer();

        healthServiceBuilder = healthServiceBuilder();
        healthService = healthServiceBuilder.setTimer(timer).setAutoFlush().buildAndStart();


    }

    @Test
    public void testRegister() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);
        healthService.clientProxyFlush();

        countDownLatch = new CountDownLatch(1);
        result = new AtomicBoolean();


        healthService.findAllNodes(names -> {
                    result.set(names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );

        healthService.clientProxyFlush();


        countDownLatch.await(10, TimeUnit.SECONDS);


        assertTrue("foo is found", result.get());

        countDownLatch = new CountDownLatch(1);
        result = new AtomicBoolean();


        healthService.findHealthyNodes(names -> {
                    result.set(!names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );


        countDownLatch = new CountDownLatch(1);
        result = new AtomicBoolean();


        healthService.findHealthyNodes(names -> {
                    result.set(names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );

        countDownLatch.await(10, TimeUnit.SECONDS);


        assertTrue("foo is not in healthy list", result.get());


    }

    @Test
    public void testCheckInOk() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkInOk("foo");

        countDownLatch = new CountDownLatch(1);
        result = new AtomicBoolean();


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

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);

        countDownLatch = new CountDownLatch(1);
        result = new AtomicBoolean();


        healthService.findHealthyNodes(names -> {
                    result.set(names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );


        countDownLatch.await(10, TimeUnit.SECONDS);


        assertTrue("foo is found among the healthy ",
                result.get());

    }


    @Test
    public void transitionFromPassToFail() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);


        countDownLatch = new CountDownLatch(1);
        result = new AtomicBoolean();


        healthService.findHealthyNodes(names -> {
                    result.set(names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );


        countDownLatch.await(10, TimeUnit.SECONDS);


        healthService.checkIn("foo", HealthStatus.FAIL);


        countDownLatch = new CountDownLatch(1);
        result = new AtomicBoolean();


        healthService.findHealthyNodes(names -> {
                    result.set(!names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );


        countDownLatch.await(10, TimeUnit.SECONDS);


        assertTrue("foo is NOT found among the healthy ",
                result.get());

    }


    @Test
    public void forceTTLExpire() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);


        countDownLatch = new CountDownLatch(1);
        result = new AtomicBoolean();
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


        countDownLatch = new CountDownLatch(1);
        result = new AtomicBoolean();


        healthService.findHealthyNodes(names -> {
                    result.set(!names.stream().anyMatch(s -> s.equals("foo")));
                    countDownLatch.countDown();
                }
        );
        countDownLatch.await(10, TimeUnit.SECONDS);
        assertTrue("foo should not be found", result.get());


    }


    @Test
    public void fail() {

        /* This is why we need to log it. */
        healthService.checkIn("no exist", HealthStatus.PASS);


    }


}