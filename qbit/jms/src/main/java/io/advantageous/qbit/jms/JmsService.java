package io.advantageous.qbit.jms;

import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.transport.TransportListener;

import javax.jms.*;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is used to startup a JMS client that can send and receive messages via JMS.
 *
 * @author rick hightower
 */
public class JmsService implements Stoppable, Startable {

    /**
     * Supply a JMS connection. Allows you to get the connection in the most convenient way possible.
     */
    private final Supplier<Connection> connectionSupplier;

    /**
     * Supply a destination supplier. Given a queue or topic name, it supplies a destination which will be a Queue or topic.
     */
    private final Function<String, Destination> destinationSupplier;

    /**
     * Should access to the destination be transacted?
     */
    private final boolean transacted;

    /**
     * Session acknowledgement mode from java.jms.Session.
     */
    private final int acknowledgeMode;

    /**
     * Are we just looking up the connection or should we start it to.
     */
    private final boolean startConnection;

    /**
     * Name of our default destination which can be a queue or topic.
     */
    private final String defaultDestination;


    /**
     * How long should we wait to receive a message?
     */
    private final int defaultTimeout;
    /**
     * connected.
     */
    private final AtomicBoolean connected = new AtomicBoolean();
    /**
     * Holds the current connection to JMS if connected.
     */
    private Optional<Connection> connectionOption = Optional.empty();
    /**
     * Holds the current session to JMS if we have a session.
     */
    private Optional<Session> sessionOption = Optional.empty();
    /**
     * Holds a map of names to destinations.
     */
    private Map<String, Destination> destinations = new LinkedHashMap<>();
    /**
     * Holds a map of names to `MessageProducer`s.
     */
    private Map<String, MessageProducer> producers = new LinkedHashMap<>();
    /**
     * Holds a map of names to `MessageConsumer`s.
     */
    private Map<String, MessageConsumer> consumers = new LinkedHashMap<>();


    /**
     * Create a new JMS Service.
     *
     * @param connectionSupplier  connectionSupplier
     * @param destinationSupplier destinationSupplier
     * @param transacted          transacted
     * @param acknowledgeMode     acknowledgeMode
     * @param startConnection     startConnection
     * @param defaultDestination  defaultDestination
     * @param defaultTimeout      defaultTimeout
     */
    public JmsService(final Supplier<Connection> connectionSupplier,
                      final Function<String, Destination> destinationSupplier,
                      final boolean transacted,
                      final int acknowledgeMode,
                      final boolean startConnection,
                      final String defaultDestination,
                      final int defaultTimeout) {

        this.connectionSupplier = connectionSupplier;
        this.destinationSupplier = destinationSupplier;
        this.transacted = transacted;
        this.acknowledgeMode = acknowledgeMode;
        this.startConnection = startConnection;
        this.defaultDestination = defaultDestination;
        this.defaultTimeout = defaultTimeout;

        getConnection();

    }

    /**
     * Get the destination.
     *
     * @param destinationName destinationName
     * @return JMS destination which is a queue or topic.
     */
    private Destination getDestination(final String destinationName) {
        if (!destinations.containsKey(destinationName)) {
            Destination destination = destinationSupplier.apply(destinationName);
            destinations.put(destinationName, destination);
        }
        return destinations.get(destinationName);
    }


    /**
     * Get the `MessageConsumer`.
     *
     * @param destinationName destinationName
     * @return JMS `MessageConsumer`.
     */
    private MessageConsumer getConsumer(final String destinationName) {
        if (!consumers.containsKey(destinationName)) {
            Session session = getSession();

            Destination destination = getDestination(destinationName);
            try {
                MessageConsumer consumer = session.createConsumer(destination);
                consumers.put(destinationName, consumer);
            } catch (JMSException e) {
                throw new JmsException("Unable to create consumer for destination "
                        + destinationName, e);
            }
        }
        return consumers.get(destinationName);
    }


    /**
     * Get the `MessageProducer` or create one from the JMS session.
     *
     * @param destinationName destinationName
     * @return JMS `MessageProducer`.
     */
    private MessageProducer getProducer(final String destinationName) {


        if (!producers.containsKey(destinationName)) {

            Session session = getSession();
            Destination destination = getDestination(destinationName);
            MessageProducer producer;
            try {
                producer = session.createProducer(destination);
            } catch (JMSException e) {
                throw new JmsException("Unable to create producer for destination "
                        + destinationName, e);
            }
            producers.put(destinationName, producer);

        }
        return producers.get(destinationName);
    }


    /**
     * Get the current session or create one from the JMS connection.
     *
     * @return JMS Session
     */
    private Session getSession() {

        if (!sessionOption.isPresent()) {
            try {
                sessionOption = Optional.of(getConnection().createSession(transacted, acknowledgeMode));
            } catch (JMSException e) {
                throw new JmsException("Unable to get JMS session", e);
            }
        }
        return sessionOption.get();
    }


    /**
     * Get the current connection or create one using the connectionSupplier.
     *
     * @return JMS Connection
     */
    private Connection getConnection() {

        if (!connectionOption.isPresent()) {
            final Connection connection = connectionSupplier.get();

            if (connection instanceof ActiveMQConnection) {
                ((ActiveMQConnection) connection).addTransportListener(new TransportListener() {
                    @Override
                    public void onCommand(Object command) {

                    }

                    @Override
                    public void onException(IOException error) {
                    }

                    @Override
                    public void transportInterupted() {
                        connected.set(false);
                    }

                    @Override
                    public void transportResumed() {
                        connected.set(true);
                    }
                });
            }


            connected.set(true);

            if (startConnection) {
                try {
                    connection.start();
                } catch (JMSException e) {
                    throw new JmsException("Unable to start JMS connection", e);
                }
            }
            connectionOption = Optional.of(connection);
        }
        return connectionOption.get();
    }


    /**
     * Send a text message given a queue or topic name and a text message.
     *
     * @param destinationName destinationName
     * @param messageContent  messageContent
     */
    public void sendTextMessageWithDestination(final String destinationName, final String messageContent) {

        if (!this.isConnected()) {
            throw new JmsNotConnectedException("JMS connection is down " + destinationName);
        }

        final Session session = getSession();
        final MessageProducer producer = getProducer(destinationName);
        try {
            TextMessage message = session.createTextMessage(messageContent);
            producer.send(message);
        } catch (JMSException e) {
            throw new JmsException("Unable to send message to " + destinationName, e);
        }
    }

    /**
     * Send a text message to the default queue.
     *
     * @param messageContent message content
     */
    public void sendTextMessage(String messageContent) {
        sendTextMessageWithDestination(defaultDestination, messageContent);
    }


    /**
     * Listen to a message from JMS from a given destination by name.
     *
     * @param destinationName destinationName
     * @param messageListener messageListener
     */
    public void listenTextMessagesWithDestination(final String destinationName,
                                                  final Consumer<String> messageListener) {
        final MessageConsumer consumer = getConsumer(destinationName);
        try {
            consumer.setMessageListener(message -> {
                try {
                    messageListener.accept(
                            ((TextMessage) message).getText()
                    );

                    if (acknowledgeMode == Session.CLIENT_ACKNOWLEDGE) {
                        message.acknowledge();
                    }

                } catch (JMSException e) {

                    throw new JmsException("Unable to register get text from message in listener " + destinationName, e);
                } catch (Exception ex) {

                    throw new IllegalStateException("Unable handle JMS Consumer  " + destinationName, ex);
                }
            });
        } catch (JMSException e) {

            throw new JmsException("Unable to register message listener " + destinationName, e);
        }
    }

    /**
     * Listen for message on default destination.
     *
     * @param messageListener messageListener
     */
    public void listenTextMessages(final Consumer<String> messageListener) {
        listenTextMessagesWithDestination(defaultDestination, messageListener);
    }

    /**
     * Receive a message from  destination with timeout.
     *
     * @param destinationName destinationName
     * @param timeout         timeout
     * @return message
     */
    public String receiveTextMessageFromDestinationWithTimeout(final String destinationName, final int timeout) {


        if (!this.isConnected()) {
            throw new JmsNotConnectedException("Not connected");
        }
        MessageConsumer consumer = getConsumer(destinationName);
        TextMessage message;
        try {
            if (timeout == 0) {
                message = (TextMessage) consumer.receiveNoWait();
            } else {
                message = (TextMessage) consumer.receive(timeout);
            }
            if (message != null) {

                if (acknowledgeMode == Session.CLIENT_ACKNOWLEDGE) {
                    message.acknowledge();
                }
                return message.getText();
            } else {
                return null;
            }
        } catch (JMSException e) {
            throw new JmsException("Unable to receive message from " + destinationName, e);
        }
    }

    /**
     * Receive a message from  destination using default timeout.
     *
     * @param destinationName destination name
     * @return message
     */
    public String receiveTextMessageFromDestination(final String destinationName) {
        return receiveTextMessageFromDestinationWithTimeout(destinationName, defaultTimeout);
    }


    /**
     * Receive a message from  destination using default timeout and default destination.
     *
     * @return received message
     */
    public String receiveTextMessage() {
        return receiveTextMessageFromDestination(defaultDestination);
    }


    /**
     * Receive a message from  destination with timeout.
     *
     * @return received message
     */
    public String receiveTextMessageWithTimeout(final int timeout) {
        return receiveTextMessageFromDestinationWithTimeout(defaultDestination, timeout);
    }

    /**
     * Stop the service
     */
    @Override
    public void stop() {
        if (connectionOption.isPresent()) {
            try {
                if (startConnection)
                    connectionOption.get().close();
            } catch (JMSException e) {

                throw new JmsException("Unable to stop ", e);
            }
            connectionOption = Optional.empty();
            sessionOption = Optional.empty();
            producers.clear();
            consumers.clear();
            destinations.clear();
        }
    }


    /**
     * Start the service
     */
    @Override
    public void start() {
        getConnection();
    }

    public boolean isConnected() {
        return connected.get();
    }
}
