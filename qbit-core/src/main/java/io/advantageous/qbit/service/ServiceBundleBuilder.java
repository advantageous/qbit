package io.advantageous.qbit.service;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.transforms.Transformer;

/**
 *
 * Allows for the programmatic construction of a service.
 * @author rhightower
 * Created by Richard on 11/14/14.
 *
 * Created by rhightower on 1/19/15.
 */
public class ServiceBundleBuilder {

    private int pollTime = GlobalConstants.POLL_WAIT;
    private int requestBatchSize = GlobalConstants.BATCH_SIZE;

    private boolean invokeDynamic = true;

    private  String address = "/services";

    private boolean eachServiceInItsOwnThread = true;


    public boolean isInvokeDynamic() {
        return invokeDynamic;
    }

    public ServiceBundleBuilder setInvokeDynamic(boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
        return this;
    }

    public boolean isEachServiceInItsOwnThread() {
        return eachServiceInItsOwnThread;
    }

    public ServiceBundleBuilder setEachServiceInItsOwnThread(boolean eachServiceInItsOwnThread) {
        this.eachServiceInItsOwnThread = eachServiceInItsOwnThread;
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


    public BeforeMethodCall getBeforeMethodCall() {
        return beforeMethodCall;
    }

    public ServiceBundleBuilder setBeforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCallAfterTransform() {
        return beforeMethodCallAfterTransform;

    }

    public ServiceBundleBuilder setBeforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;

    }

    public Transformer<Request, Object> getArgTransformer() {
        return argTransformer;

    }

    public ServiceBundleBuilder setArgTransformer(Transformer<Request, Object> argTransformer) {
        this.argTransformer = argTransformer;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public ServiceBundleBuilder setAddress(String address) {
        this.address = address;
        return this;
    }



    public int getPollTime() {
        return pollTime;
    }

    public ServiceBundleBuilder setPollTime(int pollTime) {
        this.pollTime = pollTime;
        return this;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public ServiceBundleBuilder setRequestBatchSize(int requestBatchSize) {
        this.requestBatchSize = requestBatchSize;
        return this;
    }




    QueueBuilder queueBuilder;

    public QueueBuilder getQueueBuilder() {
        return queueBuilder;
    }

    public ServiceBundleBuilder setQueueBuilder(QueueBuilder queueBuilder) {
        this.queueBuilder = queueBuilder;
        return this;
    }

    public ServiceBundle build() {

        if (queueBuilder==null) {

            queueBuilder = new QueueBuilder().setBatchSize(this.getRequestBatchSize()).setPollWait(this.getPollTime());

        }

        final ServiceBundle serviceBundle = QBit.factory().createServiceBundle(this.getAddress(),
                queueBuilder,
                QBit.factory(),
                eachServiceInItsOwnThread, this.getBeforeMethodCall(), this.getBeforeMethodCallAfterTransform(),
                this.getArgTransformer(), invokeDynamic);

        return serviceBundle;

    }
}

