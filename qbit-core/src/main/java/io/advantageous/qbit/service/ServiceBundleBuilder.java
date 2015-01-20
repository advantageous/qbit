package io.advantageous.qbit.service;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.Request;
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

    private  String address = "/services";

    private boolean eachServiceInItsOwnThread = true;

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

    public void setAddress(String address) {
        this.address = address;
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





    public ServiceBundle build() {

        final ServiceBundle serviceBundle = QBit.factory().createServiceBundle(this.getAddress(),
                this.getRequestBatchSize(), this.getPollTime(),
                QBit.factory(),
                eachServiceInItsOwnThread, this.getBeforeMethodCall(), this.getBeforeMethodCallAfterTransform(),
                this.getArgTransformer());

        return serviceBundle;

    }
}

