package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.transforms.Transformer;

/**
 *
 * Allows for the programmatic construction of a service.
 * @author rhightower
 * Created by Richard on 11/14/14.
 */

public class ServiceServerBuilder {

    private String host = "localhost";
    private int port = 8080;
    private boolean manageQueues = true;
    private int pollTime = GlobalConstants.POLL_WAIT;
    private int requestBatchSize = GlobalConstants.BATCH_SIZE;
    private int flushInterval = 200;
    private String uri = "/services";
    private int numberOfOutstandingRequests = 1_000_000;

    private int maxRequestBatches = 1_0000;

    private int timeoutSeconds = 30;
    private boolean invokeDynamic = true;


    public boolean isInvokeDynamic() {
        return invokeDynamic;
    }

    public ServiceServerBuilder setInvokeDynamic(boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
        return this;
    }
    /**
     * Allows interception of method calls before they get sent to a client.
     * This allows us to transform or reject method calls.
     */
    private  BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;

    /**
     * Allows interception of method calls before they get transformed and sent to a client.
     * This allows us to transform or reject method calls.
     */
    private  BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;


    /**
     * Allows transformation of arguments, for example from JSON to Java objects.
     */
    private Transformer<Request, Object> argTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;

    public int getMaxRequestBatches() {
        return maxRequestBatches;
    }

    public ServiceServerBuilder setMaxRequestBatches(int maxRequestBatches) {
        this.maxRequestBatches = maxRequestBatches;
        return this;
    }

    private boolean eachServiceInItsOwnThread=true;

    public boolean isEachServiceInItsOwnThread() {
        return eachServiceInItsOwnThread;
    }

    public ServiceServerBuilder setEachServiceInItsOwnThread(boolean eachServiceInItsOwnThread) {
        this.eachServiceInItsOwnThread = eachServiceInItsOwnThread;
        return this;
    }


    public BeforeMethodCall getBeforeMethodCall() {
        return beforeMethodCall;
    }

    public ServiceServerBuilder setBeforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCallAfterTransform() {
        return beforeMethodCallAfterTransform;

    }

    public ServiceServerBuilder setBeforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;

    }

    public Transformer<Request, Object> getArgTransformer() {
        return argTransformer;

    }

    public ServiceServerBuilder setArgTransformer(Transformer<Request, Object> argTransformer) {
        this.argTransformer = argTransformer;
        return this;
    }

    public int getNumberOfOutstandingRequests() {
        return numberOfOutstandingRequests;
    }

    public ServiceServerBuilder setNumberOfOutstandingRequests(int numberOfOutstandingRequests) {
        this.numberOfOutstandingRequests = numberOfOutstandingRequests;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public ServiceServerBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public ServiceServerBuilder setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }


    public String getHost() {
        return host;
    }

    public ServiceServerBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ServiceServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isManageQueues() {
        return manageQueues;
    }

    public ServiceServerBuilder setManageQueues(boolean manageQueues) {
        this.manageQueues = manageQueues;
        return this;
    }

    public int getPollTime() {
        return pollTime;
    }

    public ServiceServerBuilder setPollTime(int pollTime) {
        this.pollTime = pollTime;
        return this;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public ServiceServerBuilder setRequestBatchSize(int requestBatchSize) {
        this.requestBatchSize = requestBatchSize;
        return this;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public ServiceServerBuilder setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
        return this;
    }



    QueueBuilder queueBuilder;

    public QueueBuilder getQueueBuilder() {
        return queueBuilder;
    }

    public ServiceServerBuilder setQueueBuilder(QueueBuilder queueBuilder) {
        this.queueBuilder = queueBuilder;
        return this;
    }

    public ServiceServer build() {
        final HttpServer httpServer = QBit.factory().createHttpServer(host, port, manageQueues, pollTime, requestBatchSize, flushInterval, maxRequestBatches);
        final JsonMapper jsonMapper = QBit.factory().createJsonMapper();
        final ProtocolEncoder encoder = QBit.factory().createEncoder();


        if (queueBuilder==null) {

            queueBuilder = new QueueBuilder().setBatchSize(this.getRequestBatchSize()).setPollWait(this.getPollTime());

        }



        final ServiceBundle serviceBundle = QBit.factory().createServiceBundle(uri,
                queueBuilder,
                QBit.factory(),
                eachServiceInItsOwnThread, this.getBeforeMethodCall(),
                this.getBeforeMethodCallAfterTransform(),
                this.getArgTransformer(), true);

        final ProtocolParser parser = QBit.factory().createProtocolParser();


        final ServiceServer serviceServer = QBit.factory().createServiceServer(httpServer, encoder, parser, serviceBundle, jsonMapper, timeoutSeconds, numberOfOutstandingRequests, requestBatchSize);
        return serviceServer;

    }
}
