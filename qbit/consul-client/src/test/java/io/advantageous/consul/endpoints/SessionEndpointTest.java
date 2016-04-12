package io.advantageous.consul.endpoints;

import io.advantageous.consul.Consul;
import io.advantageous.consul.domain.Session;
import io.advantageous.consul.domain.SessionBehavior;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

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
}