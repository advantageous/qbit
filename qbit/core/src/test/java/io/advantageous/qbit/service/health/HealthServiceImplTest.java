package io.advantageous.qbit.service.health;

import io.advantageous.qbit.util.TestTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * created by rick on 6/3/15.
 */
public class HealthServiceImplTest {

    TestTimer timer;
    HealthServiceImpl healthService;

    @Before
    public void setUp() throws Exception {

        timer = new TestTimer();
        healthService = (HealthServiceImpl) HealthServiceBuilder.healthServiceBuilder()
                .setTimer(timer).getImplementation();
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
    public void testFail() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.failWithError("foo", new Exception("FOO"));

        assertFalse("foo is not found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        final List<NodeHealthStat> nodeHealthStats = healthService.loadNodes();

        assertEquals(1, nodeHealthStats.size());


        assertEquals("foo", nodeHealthStats.get(0).getName());
        assertEquals(HealthStatus.FAIL, nodeHealthStats.get(0).getStatus());
        assertEquals(HealthFailReason.ERROR, nodeHealthStats.get(0).getReason());
        assertEquals("FOO", nodeHealthStats.get(0).getError().get().getMessage());

    }

    @Test
    public void testWarn() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.warnWithError("foo", new Exception("FOO"));

        assertFalse("foo is not found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        final List<NodeHealthStat> nodeHealthStats = healthService.loadNodes();

        assertEquals(1, nodeHealthStats.size());


        assertEquals("foo", nodeHealthStats.get(0).getName());
        assertEquals(HealthStatus.WARN, nodeHealthStats.get(0).getStatus());
        assertEquals(HealthFailReason.ERROR, nodeHealthStats.get(0).getReason());
        assertEquals("FOO", nodeHealthStats.get(0).getError().get().getMessage());

    }

    @Test
    public void testWarnNoError() throws Exception {

        healthService.register("foo", 1, TimeUnit.SECONDS);

        healthService.warnWithReason("foo", HealthFailReason.TIMEOUT);

        assertFalse("foo is not found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        final List<NodeHealthStat> nodeHealthStats = healthService.loadNodes();

        assertEquals(1, nodeHealthStats.size());


        assertEquals("foo", nodeHealthStats.get(0).getName());
        assertEquals(HealthStatus.WARN, nodeHealthStats.get(0).getStatus());
        assertEquals(HealthFailReason.TIMEOUT, nodeHealthStats.get(0).getReason());
        assertEquals(false, nodeHealthStats.get(0).getError().isPresent());

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


        healthService.callProcess();

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

        healthService.callProcess();


        assertTrue("foo is NOT found among the healthy ",
                !healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        healthService.checkIn("foo", HealthStatus.FAIL);


        assertTrue("foo is NOT found among the healthy ",
                !healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));


        healthService.checkIn("foo", HealthStatus.PASS);

        healthService.callProcess();


        assertTrue("foo is found among the healthy ",
                healthService.findHealthyNodes().stream().anyMatch(s -> s.equals("foo")));

    }


}