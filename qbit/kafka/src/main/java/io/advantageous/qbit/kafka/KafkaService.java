package io.advantageous.qbit.kafka;

import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Optional;
import java.util.function.Supplier;

public class KafkaService {


    private final Supplier<KafkaProducer<String, String>> kafkaProducerSupplier;
    private final Supplier<ConsumerConnector> consumerConnectorSupplier;
    private final Supplier<Callback> callbackSupplier;
    private final String defaultTopic;

    private Optional<KafkaProducer<String, String>> kafkaProducer = Optional.empty();
    private Optional<ConsumerConnector> consumerConnector = Optional.empty();


    public KafkaService(final String defaultTopic,
                        final Supplier<KafkaProducer<String, String>> kafkaProducerSupplier,
                        final Supplier<ConsumerConnector> consumerConnectorSupplier,
                        final Supplier<Callback> callbackSupplier) {
        this.kafkaProducerSupplier = kafkaProducerSupplier;
        this.consumerConnectorSupplier = consumerConnectorSupplier;
        this.callbackSupplier = callbackSupplier;
        this.defaultTopic = defaultTopic;
    }

    public void sendMessage(final String topic, String messageContent) {

        if (!kafkaProducer.isPresent()) {
            kafkaProducer = Optional.of(kafkaProducerSupplier.get());
        }
        kafkaProducer.get().send(new ProducerRecord<>(topic, messageContent), callbackSupplier.get());
    }


    public void sendMessage(String messageContent) {
        sendMessage(defaultTopic, messageContent);
    }

    public void receiveMessage(final String topic, String messageContent) {

        if (!consumerConnector.isPresent()) {
            consumerConnector = Optional.of(consumerConnectorSupplier.get());
        }
        //finish up
    }

}
