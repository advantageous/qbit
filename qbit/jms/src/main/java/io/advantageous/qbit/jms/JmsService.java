package io.advantageous.qbit.jms;

import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JmsService implements Stoppable, Startable{

    private final Supplier<Connection> connectionSupplier;
    private final Function<String, Destination> destinationSupplier;
    private final boolean transacted;
    private final int acknowledgeMode;
    private final boolean startConnection;
    private final String defaultDestination;
    private final int defaultTimeout;
    private Optional<Connection> connectionOption=Optional.empty();
    private Optional<Session> sessionOption=Optional.empty();
    private Map<String, Destination> destinations = new LinkedHashMap<>();
    private Map<String, MessageProducer> producers = new LinkedHashMap<>();
    private Map<String, MessageConsumer> consumers = new LinkedHashMap<>();


    public JmsService(Supplier<Connection> connectionSupplier,
                      Function<String, Destination> destinationSupplier,
                      boolean transacted,
                      int acknowledgeMode,
                      boolean startConnection,
                      String defaultDestination,
                      final int defaultTimeout) {

        this.connectionSupplier = connectionSupplier;
        this.destinationSupplier = destinationSupplier;
        this.transacted = transacted;
        this.acknowledgeMode = acknowledgeMode;
        this.startConnection = startConnection;
        this.defaultDestination = defaultDestination;
        this.defaultTimeout = defaultTimeout;

    }

    private Destination getDestination(final String destinationName) {
        if (!destinations.containsKey(destinationName)) {
            Destination destination = destinationSupplier.apply(destinationName);
            destinations.put(destinationName,destination);
        }
        return destinations.get(destinationName);
    }


    private MessageConsumer getConsumer(final String destinationName) {
        if (!consumers.containsKey(destinationName)) {
            Session session = getSession();
            Destination destination = getDestination(destinationName);
            try {
                MessageConsumer consumer = session.createConsumer(destination);
                consumers.put(destinationName, consumer);
            } catch (JMSException e) {
                throw new IllegalStateException("Unable to create consumer for destination "
                        + destinationName, e);
            }
        }
        return consumers.get(destinationName);
    }


    private MessageProducer getProducer(final String destinationName) {


        if (!producers.containsKey(destinationName)) {

            Session session = getSession();
            Destination destination = getDestination(destinationName);
            MessageProducer producer;
            try {
                producer = session.createProducer(destination);
            } catch (JMSException e) {
                throw new IllegalStateException("Unable to create producer for destination "
                        + destinationName, e);
            }
            producers.put(destinationName, producer);

        }
        return producers.get(destinationName);
    }


    private Session getSession ()  {

        if (!sessionOption.isPresent()) {
            try {
                sessionOption = Optional.of(getConnection().createSession(transacted, acknowledgeMode));
            } catch (JMSException e) {
                throw new IllegalStateException("Unable to get JMS session", e);
            }
        }
        return sessionOption.get();
    }


    private Connection getConnection() {

        if (!connectionOption.isPresent()) {
            Connection connection = connectionSupplier.get();
            if (startConnection) {
                try {
                    connection.start();
                } catch (JMSException e) {
                    throw new IllegalStateException("Unable to start JMS connection", e);
                }
            }
            connectionOption =Optional.of(connection);
        }
        return connectionOption.get();
    }


    public void sendTextMessageWithDestination(final String destinationName, final String messageContent)  {
        final Session session = getSession();
        final MessageProducer producer = getProducer(destinationName);
        try {
            TextMessage message = session.createTextMessage(messageContent);
            producer.send(message);
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to send message to " + destinationName, e);
        }
    }

    public void sendTextMessage(String messageContent)  {
        sendTextMessageWithDestination(defaultDestination, messageContent);
    }


    public void listenTextMessagesWithDestination(final String destinationName,
                                                  final Consumer<String> messageListener) {
        final MessageConsumer consumer  = getConsumer(destinationName);
        try {
            consumer.setMessageListener(message -> {
                try {
                    messageListener.accept(
                            ((TextMessage) message).getText()
                    );
                } catch (JMSException e) {

                    throw new IllegalStateException("Unable to register get text from message in listener " + destinationName, e);
                }
            });
        } catch (JMSException e) {

            throw new IllegalStateException("Unable to register message listener " + destinationName, e);
        }
    }

    public void listenTextMessages(final Consumer<String> messageListener) {
         listenTextMessagesWithDestination(defaultDestination, messageListener);
    }
    public String receiveTextMessageFromDestinationWithTimeout(final String destinationName, final int timeout) {
        MessageConsumer consumer  = getConsumer(destinationName);
        TextMessage message;
        try {
            if (timeout == 0) {
                message = (TextMessage) consumer.receiveNoWait();
            }else {
                message = (TextMessage) consumer.receive(timeout);
            }
            return message == null ? null : message.getText();
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to receive message from " + destinationName, e);
        }
    }

    public String receiveTextMessageFromDestination(final String destinationName) {
        return receiveTextMessageFromDestinationWithTimeout(destinationName, defaultTimeout);
    }


    public String receiveTextMessage() {
        return receiveTextMessageFromDestination(defaultDestination);
    }


    public String receiveTextMessageWithTimeout(final int timeout) {
        return receiveTextMessageFromDestinationWithTimeout(defaultDestination, timeout);
    }

    @Override
    public void stop() {
        if (connectionOption.isPresent()) {
            try {
                if (startConnection)
                connectionOption.get().close();
            } catch (JMSException e) {

                throw new IllegalStateException("Unable to stop ", e);
            }
            connectionOption = Optional.empty();
            sessionOption = Optional.empty();
            producers.clear();
            consumers.clear();
            destinations.clear();
        }
    }

    @Override
    public void start() {
        getConnection();
    }
}
