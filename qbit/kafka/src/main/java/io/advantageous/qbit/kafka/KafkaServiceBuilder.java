package io.advantageous.qbit.kafka;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.util.UriUtil;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.consumer.SimpleConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class KafkaServiceBuilder {


    private String valueSerializer = "org.apache.kafka.common.serialization.StringSerializer";
    private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
    private String clientId = "qbit.client.id";
    private String consumerId = null;
    private String groupId = "qbit.group.id";
    private List<URI> bootstrapServers;
    private List<URI> zookeeperConnectServers;
    private String defaultTopic = "myTopic";

    private Logger logger = LoggerFactory.getLogger(KafkaServiceBuilder.class);

    private Properties properties;
    private Supplier<KafkaProducer<String, String>> kafkaProducerSupplier;
    private Supplier<ConsumerConnector> consumerConnectorSupplier;
    private Supplier<Callback> callbackSupplier;

    public static void main(String... args) {


        KafkaService kafkaService = new KafkaServiceBuilder()
                .addBootstrapServer("localhost", 9092)
                .addZookeeperConnectServer("127.0.0.1", 2181).build();

        kafkaService.sendMessage("mom");

        SimpleConsumer simpleConsumer = new SimpleConsumer(KafkaProperties.kafkaServerURL,
                KafkaProperties.kafkaServerPort,
                KafkaProperties.connectionTimeOut,
                KafkaProperties.kafkaProducerBufferSize,
                KafkaProperties.clientId);

//        FetchRequest req = new FetchRequestBuilder()
//                .clientId(KafkaProperties.clientId)
//                .addFetch(KafkaProperties.topic2, 0, 0L, 100)
//                .build();

        Sys.sleep(1000);
    }

    public Properties createProperties() {

        final Properties properties = new Properties();


        properties.setProperty("client.id", this.getClientId());

        if (getConsumerId() != null)
            properties.setProperty("consumer.id", getConsumerId());


        if (getGroupId() != null)
            properties.setProperty("group.id", getGroupId());

        properties.setProperty("value.serializer", this.getValueSerializer());
        properties.setProperty("serializer.class", this.getValueSerializer());
        properties.setProperty("key.serializer", this.getKeySerializer());


        properties.setProperty("bootstrap.servers", getBootstrapServers().stream().map(uri -> uri.getHost()
                + ':' + uri.getPort()).collect(Collectors.joining(",")));


        properties.setProperty("zookeeper.connect", getZookeeperConnectServers().stream().map(uri -> uri.getHost()
                + ':' + uri.getPort()).collect(Collectors.joining(",")));

        getProperties().entrySet().forEach(entry -> {
            if (entry.getValue() != null) {
                properties.setProperty(entry.getKey().toString(), entry.getValue().toString());
            }
        });


        return properties;
    }

    public String getClientId() {
        return clientId;
    }

    public KafkaServiceBuilder setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public KafkaServiceBuilder setConsumerId(String consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public String getValueSerializer() {
        return valueSerializer;
    }

    public KafkaServiceBuilder setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
        return this;
    }

    public String getKeySerializer() {
        return keySerializer;
    }

    public KafkaServiceBuilder setKeySerializer(String keySerializer) {
        this.keySerializer = keySerializer;
        return this;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public KafkaServiceBuilder setProperties(final Properties properties) {
        this.properties = properties;
        return this;
    }

    public KafkaServiceBuilder addProperty(final String key, String value) {
        getProperties().setProperty(key, value);
        return this;
    }

    public List<URI> getZookeeperConnectServers() {
        if (zookeeperConnectServers == null) {
            zookeeperConnectServers = new ArrayList<>();
        }
        return zookeeperConnectServers;
    }

    public KafkaServiceBuilder setZookeeperConnectServers(List<URI> zookeeperConnectServers) {
        this.zookeeperConnectServers = zookeeperConnectServers;
        return this;
    }

    public List<URI> getBootstrapServers() {
        if (bootstrapServers == null) {
            bootstrapServers = new ArrayList<>();
        }
        return bootstrapServers;
    }

    public KafkaServiceBuilder setBootstrapServers(final List<URI> bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
        return this;
    }

    public KafkaServiceBuilder addBootstrapServer(final URI bootStrapNode) {
        getBootstrapServers().add(bootStrapNode);
        return this;
    }

    public KafkaServiceBuilder addBootstrapServer(final String host, final int port) {
        getBootstrapServers().add(UriUtil.createURI("kafka", host, port));
        return this;
    }

    public KafkaServiceBuilder addBootstrapServer(final String node) {
        if (node.contains("://")) {
            final URI uri = UriUtil.parseURI(node);
            addBootstrapServer(uri.getHost(), uri.getPort());
        } else {
            final URI uri = UriUtil.parseURI("kafka://" + node);
            addBootstrapServer(uri.getHost(), uri.getPort());
        }
        return this;
    }

    public Logger getLogger() {
        return logger;
    }

    public KafkaServiceBuilder setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public KafkaServiceBuilder addZookeeperConnectServer(final URI node) {
        getZookeeperConnectServers().add(node);
        return this;
    }

    public KafkaServiceBuilder addZookeeperConnectServer(final String host, final int port) {
        getZookeeperConnectServers().add(UriUtil.createURI("zookeeper", host, port));
        return this;
    }

    public KafkaServiceBuilder addZookeeperConnectServer(final String node) {
        if (node.contains("://")) {
            final URI uri = UriUtil.parseURI(node);
            addZookeeperConnectServer(uri.getHost(), uri.getPort());
        } else {
            final URI uri = UriUtil.parseURI("zookeeper://" + node);
            addZookeeperConnectServer(uri.getHost(), uri.getPort());
        }
        return this;
    }

    public String getDefaultTopic() {
        return defaultTopic;
    }

    public KafkaServiceBuilder setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
        return this;
    }

    public Supplier<KafkaProducer<String, String>> getKafkaProducerSupplier() {
        if (kafkaProducerSupplier == null) {
            kafkaProducerSupplier = () -> new KafkaProducer<>(createProperties());
        }
        return kafkaProducerSupplier;
    }

    public KafkaServiceBuilder setKafkaProducerSupplier(Supplier<KafkaProducer<String, String>> kafkaProducerSupplier) {
        this.kafkaProducerSupplier = kafkaProducerSupplier;
        return this;
    }

    public Supplier<ConsumerConnector> getConsumerConnectorSupplier() {
        if (consumerConnectorSupplier == null) {
            consumerConnectorSupplier = () -> Consumer.createJavaConsumerConnector(
                    createConsumerConfig());
        }
        return consumerConnectorSupplier;
    }

    public KafkaServiceBuilder setConsumerConnectorSupplier(Supplier<ConsumerConnector> consumerConnectorSupplier) {
        this.consumerConnectorSupplier = consumerConnectorSupplier;
        return this;
    }

    public Supplier<Callback> getCallbackSupplier() {

        if (callbackSupplier == null) {

            final Logger logger = getLogger();
            final boolean debug = logger.isDebugEnabled();
            callbackSupplier = () -> (metadata, exception) -> {
                if (debug) {
                    if (metadata != null) {
                        logger.debug(metadata.toString());
                    }
                }

                if (exception != null) {
                    logger.error("Unable to send message to kafka", exception);
                }
            };
        }
        return callbackSupplier;
    }

    public KafkaServiceBuilder setCallbackSupplier(Supplier<Callback> callbackSupplier) {
        this.callbackSupplier = callbackSupplier;
        return this;
    }

    public KafkaService build() {

        return new KafkaService(getDefaultTopic(), getKafkaProducerSupplier(), getConsumerConnectorSupplier(), getCallbackSupplier());

    }

    private ConsumerConfig createConsumerConfig() {

        return new ConsumerConfig(createProperties());

    }

    public String getGroupId() {
        return groupId;
    }

    public KafkaServiceBuilder setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    interface KafkaProperties {
        final static String zkConnect = "127.0.0.1:2181";
        final static String groupId = "group1";
        final static String topic = "topic1";
        final static String kafkaServerURL = "localhost";
        final static int kafkaServerPort = 9092;
        final static int kafkaProducerBufferSize = 64 * 1024;
        final static int connectionTimeOut = 100000;
        final static int reconnectInterval = 10000;
        final static String topic2 = "topic2";
        final static String topic3 = "topic3";
        final static String clientId = "SimpleConsumerDemoClient";
    }
}
