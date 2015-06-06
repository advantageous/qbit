package io.advantageous.qbit.service.health;

import io.advantageous.qbit.util.TestTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * created by rick on 6/3/15.
 */
public class HealthServiceImplTest {

    TestTimer timer;
    HealthServiceImpl healthService;

    @Before
    public void setUp() throws Exception {

        timer = new TestTimer();
        healthService = new HealthServiceImpl(timer, 1, TimeUnit.SECONDS);
    }

    @Test
    public void testRegister() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);


        assertTrue("foo is found", healthService.findAllNodes().stream().anyMatch(s -> s.equals("foo")));

        assertTrue("foo is not found among the healthy yet",
                !healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


    }

    @Test
    public void testCheckInOk() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkInOk("foo");

        assertTrue("foo is found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));

    }

    @Test
    public void testCheckInOkUsingCheckIn() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);

        assertTrue("foo is found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));

    }


    @Test
    public void transitionFromPassToFail() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);

        assertTrue("foo is found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        healthService.checkIn("foo", HealthStatus.FAIL);


        assertTrue("foo is NOT found among the healthy ",
                !healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));

    }


    @Test(expected = IllegalStateException.class)
    public void fail() {

        healthService.checkIn("no exist", HealthStatus.PASS);

    }

    @Test
    public void allOk() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);
        healthService.checkIn("foo", HealthStatus.PASS);


        healthService.register("bar", 1, TimeUnit.SECONDS);
        healthService.checkIn("bar", HealthStatus.PASS);


        healthService.register("baz", 1, TimeUnit.SECONDS);
        healthService.checkIn("baz", HealthStatus.PASS);

        assertTrue(healthService.ok());

        healthService.checkIn("baz", HealthStatus.FAIL);


        assertTrue(!healthService.ok());


    }


    @Test
    public void forceTTLExpire() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);

        assertTrue("foo is found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        timer.setTime();
        timer.minutes(1);

        healthService.process();


        assertTrue("foo is NOT found among the healthy ",
                !healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


    }


    @Test
    public void forceTTLExpireThenRecover() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.checkIn("foo", HealthStatus.PASS);

        assertTrue("foo is found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        timer.setTime();
        timer.minutes(1);

        healthService.process();


        assertTrue("foo is NOT found among the healthy ",
                !healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        healthService.checkIn("foo", HealthStatus.FAIL);


        assertTrue("foo is NOT found among the healthy ",
                !healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        healthService.checkIn("foo", HealthStatus.PASS);

        healthService.process();


        assertTrue("foo is found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));

    }


}