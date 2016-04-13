package io.advantageous.consul.endpoints;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.consul.Consul;
import io.advantageous.consul.domain.KeyValue;
import io.advantageous.consul.domain.Session;
import io.advantageous.consul.domain.SessionBehavior;
import io.advantageous.consul.domain.option.Consistency;
import io.advantageous.consul.domain.option.KeyValuePutOptions;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import static io.advantageous.boon.json.JsonFactory.fromJson;
import static io.advantageous.consul.endpoints.RequestUtils.decodeBase64;
import static org.junit.Assert.*;

/**
 * Created by rick on 4/12/16.
 */
public class SessionEndpointTest {


    Consul consul;
    SessionEndpoint sessionEndpoint;

    @Before
    public void before() throws Exception {

        consul = Consul.consul();
        sessionEndpoint = consul.session();
    }

    @Test
    public void createSessionTest() {

        Session session = new Session()
                .setName("mysession").setTtlHours(1);
        final String sessionId = sessionEndpoint.create(session);
        System.out.println(sessionId);
        assertNotNull(sessionId);

        final Optional<Session> session1 = sessionEndpoint.info(sessionId);
        assertEquals("mysession", session1.get().getName());


        final List<Session> sessions = sessionEndpoint.getSessions();
        assertTrue(sessions.size()>0);


        final List<Session> nodeSessions = sessionEndpoint.getSessionsForNode(session1.get().getNode());
        assertTrue(nodeSessions.size()>0);

        final Session renew = sessionEndpoint.renew(sessionId);

        assertNotNull(renew);

        final boolean destroyed = sessionEndpoint.destroy(sessionId, session);

        assertTrue("Session was destroyed", destroyed);


        final Optional<Session> session2 = sessionEndpoint.info(sessionId);


        assertTrue("no session was found because we deleted it", !session2.isPresent());

    }



    @Test
    public void leaderElection() throws Exception {


        final String serviceName = "myservice1";

        final EndpointDefinition endpoint2 = new EndpointDefinition(serviceName, "192.33.44.22", 9090);
        final EndpointDefinition endpoint1 = new EndpointDefinition(serviceName, "192.33.44.11", 9090);


        /** Create two sessions. */
        final Session session1 = new Session()
                .setName("leaderLock").setTtlSeconds(10).setLockDelaySeconds(1).setSessionBehavior(SessionBehavior.DELETE);

        final Session session2 = new Session()
                .setName("leaderLock").setTtlSeconds(10).setLockDelaySeconds(1).setSessionBehavior(SessionBehavior.DELETE);

        final String session1Id = sessionEndpoint.create(session1);
        final String session2Id = sessionEndpoint.create(session2);

        /** Try to get leadership for session 1. */
        final String PATH = "/service/" + serviceName + "/leader";
        boolean leader = consul.keyValueStore().putValue(PATH,
                JsonFactory.toJson(endpoint1), 0L, new KeyValuePutOptions(null, session1Id, null));

        assertTrue("Session 1 is the leader", leader);


        /** Try to get leadership for session 2. */
        leader = consul.keyValueStore().putValue(PATH,
                JsonFactory.toJson(endpoint2), 0L, new KeyValuePutOptions(null, session2Id, null));

        assertFalse("Session 2 is not the leader", leader);

        final AtomicLong index = new AtomicLong();

        /** Since session 2 is not the leader, let's see who is. */
        if (!leader) {
            final Optional<KeyValue> keyValue = consul.keyValueStore().getValue(PATH);
            assertTrue("Key is present", keyValue.isPresent());

            final String value = keyValue.get().getValue();
            System.out.println(value);

            EndpointDefinition leaderEndpoint = fromJson(value, EndpointDefinition.class);

            index.set(keyValue.get().getModifyIndex());

            assertEquals(endpoint1.getHost(), leaderEndpoint.getHost());
            assertEquals(endpoint1.getPort(), leaderEndpoint.getPort());
        }


        long start = System.currentTimeMillis();

        final CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            while (true) {
                final Optional<KeyValue> value = consul.keyValueStore().getValue(PATH, new RequestOptions("3s", (int) index.get(), Consistency.DEFAULT));
                long current = System.currentTimeMillis();

                System.out.println("So far " + ((current - start) / 1000) + " "+  value + " ");
                if (!value.isPresent()) {
                    latch.countDown();
                    break;
                }

                final Session renew = sessionEndpoint.renew(session2Id);
                System.out.println("RENEWED " + renew);

            }

        });
        thread.start();
        latch.await();

        final Optional<KeyValue> value = consul.keyValueStore().getValue(PATH, new RequestOptions("3s", (int)index.get(), Consistency.DEFAULT));
        long stop = System.currentTimeMillis();

        System.out.println("DONE " + ((stop-start)/1000)+ "  " +value);
        assertTrue(!value.isPresent());


        Thread.sleep(1_000);

        /** Try to get leadership for session 2. */
        leader = consul.keyValueStore().putValue(PATH,
                JsonFactory.toJson(endpoint2), 0L, new KeyValuePutOptions(null, session2Id, null));

        assertTrue("Session 2 is the leader", leader);

    }
}