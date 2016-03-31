package io.advantageous.qbit.jms;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.queue.JsonQueue;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.util.PortUtils;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created  10/8/15.
 */
public class JmsTest {

    private Queue<Person> personQueue;
    private SendQueue<Person> personSendQueue;
    private ReceiveQueue<Person> personReceiveQueue;
    private BrokerService broker;
    private int port;

    @Before
    public void setUp() throws Exception {

        port = PortUtils.findOpenPortStartAt(4000);
        broker = new BrokerService();
        broker.addConnector("tcp://localhost:" + port);
        broker.start();

        final JmsServiceBuilder jmsBuilder = JmsServiceBuilder.newJmsServiceBuilder()
                .setDefaultDestination("foobarQueue").setAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE).setPort(port);

        final Queue<String> textQueue = new JmsTextQueue(jmsBuilder);

        personQueue = new JsonQueue<>(Person.class, textQueue);
        personSendQueue = personQueue.sendQueue();
        personReceiveQueue = personQueue.receiveQueue();


        personSendQueue.shouldBatch();
        personSendQueue.name();
        personSendQueue.size();
        personQueue.name();
        personQueue.size();
    }

    @Test
    public void testSendConsume() throws Exception {


        personSendQueue.send(new Person("Geoff"));
        personSendQueue.send(new Person("Rick"));
        personSendQueue.flushSends();


        Person geoff = personReceiveQueue.pollWait();

        while (geoff == null) {
            geoff = personReceiveQueue.pollWait();
        }

        final Person rick = personReceiveQueue.pollWait();

        assertTrue(geoff.name.equals("Rick") || geoff.name.equals("Geoff"));
        assertTrue(rick.name.equals("Rick") || rick.name.equals("Geoff"));


        assertEquals(true, personQueue.started());

    }


    @Test
    public void testSendConsume2() throws Exception {

        personSendQueue.sendAndFlush(new Person("Geoff"));
        personSendQueue.sendAndFlush(new Person("Rick"));

        Person geoff = personReceiveQueue.pollWait();

        while (geoff == null) {
            geoff = personReceiveQueue.pollWait();
        }

        final Person rick = personReceiveQueue.poll();


        assertTrue(geoff.name.equals("Rick") || geoff.name.equals("Geoff"));
        assertTrue(rick.name.equals("Rick") || rick.name.equals("Geoff"));
    }


    @Test
    public void testSendConsume3() throws Exception {

        personSendQueue = personQueue.sendQueueWithAutoFlush(10, TimeUnit.MILLISECONDS);

        personSendQueue.sendMany(new Person("Geoff"), new Person("Rick"));
        final Person geoff = personReceiveQueue.take();
        final Person rick = personReceiveQueue.take();

        assertTrue(geoff.name.equals("Rick") || geoff.name.equals("Geoff"));
        assertTrue(rick.name.equals("Rick") || rick.name.equals("Geoff"));

    }


    @Test
    public void testSendConsume4() throws Exception {
        personSendQueue = personQueue.sendQueueWithAutoFlush(QBit.factory().periodicScheduler(),
                10, TimeUnit.MILLISECONDS);

        personSendQueue.sendBatch(Lists.list(new Person("Geoff"), new Person("Rick")));
        final Person geoff = personReceiveQueue.take();
        final Person rick = personReceiveQueue.take();

        assertTrue(geoff.name.equals("Rick") || geoff.name.equals("Geoff"));
        assertTrue(rick.name.equals("Rick") || rick.name.equals("Geoff"));

    }

    @Test
    @Ignore
    public void testSendConsume5() throws Exception {
        final List<Person> list = Lists.list(new Person("Geoff"), new Person("Rick"));

        Iterable<Person> persons = list::iterator;


        personSendQueue.sendBatch(persons);
        personSendQueue.flushSends();


        Person geoff = personReceiveQueue.pollWait();

        while (geoff == null) {
            geoff = personReceiveQueue.pollWait();
        }
        final Person rick = personReceiveQueue.pollWait();


        assertTrue(geoff.name.equals("Rick") || geoff.name.equals("Geoff"));
        assertTrue(rick.name.equals("Rick") || rick.name.equals("Geoff"));

    }


    @Test(expected = JmsException.class)
    public void testSendConsumeDown() throws Exception {
        final List<Person> list = Lists.list(new Person("Geoff"), new Person("Rick"));

        Iterable<Person> persons = list::iterator;


        personSendQueue.sendBatch(persons);
        personSendQueue.flushSends();


        broker.stop();

        Sys.sleep(1000);

        personReceiveQueue.pollWait();

    }


    @Test(expected = JmsException.class)
    public void testSendDown() throws Exception {
        final List<Person> list = Lists.list(new Person("Geoff"), new Person("Rick"));

        Iterable<Person> persons = list::iterator;


        personSendQueue.sendBatch(persons);
        personSendQueue.flushSends();


        broker.stop();

        Sys.sleep(1000);

        personSendQueue.sendBatch(persons);

    }

    @Test
    public void testSendConsume6() throws Exception {

        personSendQueue.send(new Person("Geoff"));
        personSendQueue.send(new Person("Rick"));

        personSendQueue.flushSends();

        Sys.sleep(2000);
        final List<Person> personsBatch = (List<Person>) personReceiveQueue.readBatch();

    }


    @Test
    public void testSendConsume7() throws Exception {
        final List<Person> list = Lists.list(new Person("Geoff"), new Person("Rick"));
        final Iterable<Person> persons = list::iterator;


        personSendQueue.sendBatch(persons);
        personSendQueue.flushSends();

        final List<Person> personsBatch = (List<Person>) personReceiveQueue.readBatch(5);


    }


    @Test
    @Ignore
    public void testSendConsume8() throws Exception {

        final ArrayBlockingQueue<Person> personsABQ = new ArrayBlockingQueue<>(100);

        personSendQueue.send(new Person("Geoff"));
        personSendQueue.send(new Person("Rick"));
        personSendQueue.flushSends();


        personQueue.startListener(personsABQ::add);

        Sys.sleep(1000);
        int count = 0;

        while (personsABQ.size() < 2) {
            Sys.sleep(100);
            count++;
            if (count > 100) break;
        }


        Sys.sleep(1000);
        assertEquals(2, personsABQ.size());
        final Person geoff = personsABQ.poll();
        final Person rick = personsABQ.poll();


        assertTrue(geoff.name.equals("Rick") || geoff.name.equals("Geoff"));
        assertTrue(rick.name.equals("Rick") || rick.name.equals("Geoff"));

    }


    @Test
    public void builder() throws Exception {
        JmsServiceBuilder jmsServiceBuilder = JmsServiceBuilder.newJmsServiceBuilder();
        jmsServiceBuilder.setJndiSettings(Collections.emptyMap());
        jmsServiceBuilder.getJndiSettings();
        jmsServiceBuilder.setConnectionFactory(new ConnectionFactory() {
            @Override
            public Connection createConnection() throws JMSException {
                return null;
            }

            @Override
            public Connection createConnection(String userName, String password) throws JMSException {
                return null;
            }
        });
        jmsServiceBuilder.getConnectionFactory();

        jmsServiceBuilder.setConnectionFactoryName("Foo");
        jmsServiceBuilder.getConnectionFactoryName();
        jmsServiceBuilder.setContext(null);
        jmsServiceBuilder.getContext();
        jmsServiceBuilder.setDefaultDestination("foo");
        jmsServiceBuilder.getDefaultDestination();
        jmsServiceBuilder.setAcknowledgeMode(5);
        jmsServiceBuilder.getAcknowledgeMode();
        jmsServiceBuilder.setDefaultTimeout(1);
        jmsServiceBuilder.getDefaultTimeout();
        jmsServiceBuilder.setHost("foo");
        jmsServiceBuilder.getHost();
        jmsServiceBuilder.setUserName("rick");
        jmsServiceBuilder.getUserName();
        jmsServiceBuilder.setPassword("foo");
        jmsServiceBuilder.getPassword();
        jmsServiceBuilder.setProviderURL("");
        jmsServiceBuilder.getProviderURL();
        jmsServiceBuilder.setProviderURLPattern("");
        jmsServiceBuilder.getProviderURLPattern();
        jmsServiceBuilder.setConnectionSupplier(null);
        jmsServiceBuilder.getConnectionSupplier();
        jmsServiceBuilder.setStartConnection(true);
        jmsServiceBuilder.isStartConnection();
        jmsServiceBuilder.setTransacted(true);
        jmsServiceBuilder.isTransacted();
        jmsServiceBuilder.setJndiSettings(null);
        jmsServiceBuilder.addJndiSetting("foo", "bar");


        jmsServiceBuilder = JmsServiceBuilder.newJmsServiceBuilder().setPort(port);

        jmsServiceBuilder.build().start();
    }

    @After
    public void tearDown() throws Exception {

        personQueue.stop();
        personReceiveQueue.stop();
        personSendQueue.stop();

        try {
            broker.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        broker = null;
        Sys.sleep(1000);
    }


    private static class Person {
        final String name;

        private Person(String name) {
            this.name = name;
        }
    }
}