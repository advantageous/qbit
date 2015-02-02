package io.advantageous.qbit.service;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.service.impl.NoOpAfterMethodCall;
import io.advantageous.qbit.service.impl.NoOpInputMethodCallQueueListener;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.service.impl.ServiceImpl;
import io.advantageous.qbit.transforms.NoOpResponseTransformer;
import io.advantageous.qbit.transforms.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rhightower on 1/28/15.
 */
public class ServiceBuilder {


    public static ServiceBuilder serviceBuilder () {return new ServiceBuilder();}
    private final Logger logger = LoggerFactory.getLogger(ServiceBuilder.class);
    private final boolean debug = logger.isDebugEnabled();

    private ServiceMethodHandler serviceMethodHandler;


    private BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;

    private BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;

    private AfterMethodCall afterMethodCall = new NoOpAfterMethodCall();

    private AfterMethodCall afterMethodCallAfterTransform = new NoOpAfterMethodCall();

    private ReceiveQueueListener<MethodCall<Object>> inputQueueListener = new NoOpInputMethodCallQueueListener();

    private Transformer<Request, Object> requestObjectTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;

    private Transformer<Response<Object>, Response> responseObjectTransformer = new NoOpResponseTransformer();


    private Queue<Response<Object>> responseQueue;

    private QueueBuilder queueBuilder;

    private QueueBuilder responseQueueBuilder = new QueueBuilder();


    private boolean asyncResponse = true;


    private boolean invokeDynamic = true;

    private String rootAddress;


    private String serviceAddress;

    private Object serviceObject;

    public QueueBuilder getResponseQueueBuilder() {
        return responseQueueBuilder;
    }

    public ServiceBuilder setResponseQueueBuilder(QueueBuilder responseQueueBuilder) {
        this.responseQueueBuilder = responseQueueBuilder;
        return this;
    }

    public Queue<Response<Object>> getResponseQueue() {
        return responseQueue;
    }

    public ServiceBuilder setResponseQueue(Queue<Response<Object>> responseQueue) {
        this.responseQueue = responseQueue;
        return this;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public ServiceBuilder setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
        return this;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public ServiceBuilder setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
        return this;
    }

    public String getRootAddress() {
        return rootAddress;
    }

    public ServiceBuilder setRootAddress(String rootAddress) {
        this.rootAddress = rootAddress;
        return this;
    }

    public ServiceMethodHandler getServiceMethodHandler() {
        return serviceMethodHandler;
    }

    public ServiceBuilder setServiceMethodHandler(ServiceMethodHandler serviceMethodHandler) {
        this.serviceMethodHandler = serviceMethodHandler;
        return this;

    }

    public BeforeMethodCall getBeforeMethodCall() {
        return beforeMethodCall;
    }

    public ServiceBuilder setBeforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;

    }

    public BeforeMethodCall getBeforeMethodCallAfterTransform() {
        return beforeMethodCallAfterTransform;
    }

    public ServiceBuilder setBeforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;

    }

    public AfterMethodCall getAfterMethodCall() {
        return afterMethodCall;
    }

    public ServiceBuilder setAfterMethodCall(AfterMethodCall afterMethodCall) {
        this.afterMethodCall = afterMethodCall;
        return this;

    }

    public AfterMethodCall getAfterMethodCallAfterTransform() {
        return afterMethodCallAfterTransform;
    }

    public ServiceBuilder setAfterMethodCallAfterTransform(AfterMethodCall afterMethodCallAfterTransform) {
        this.afterMethodCallAfterTransform = afterMethodCallAfterTransform;
        return this;

    }

    public ReceiveQueueListener<MethodCall<Object>> getInputQueueListener() {
        return inputQueueListener;
    }

    public ServiceBuilder setInputQueueListener(ReceiveQueueListener<MethodCall<Object>> inputQueueListener) {
        this.inputQueueListener = inputQueueListener;
        return this;

    }

    public Transformer<Request, Object> getRequestObjectTransformer() {
        return requestObjectTransformer;
    }

    public ServiceBuilder setRequestObjectTransformer(Transformer<Request, Object> requestObjectTransformer) {
        this.requestObjectTransformer = requestObjectTransformer;
        return this;

    }

    public Transformer<Response<Object>, Response> getResponseObjectTransformer() {
        return responseObjectTransformer;
    }

    public ServiceBuilder setResponseObjectTransformer(Transformer<Response<Object>, Response> responseObjectTransformer) {
        this.responseObjectTransformer = responseObjectTransformer;
        return this;

    }

    public QueueBuilder getQueueBuilder() {
        return queueBuilder;
    }

    public ServiceBuilder setQueueBuilder(QueueBuilder queueBuilder) {
        this.queueBuilder = queueBuilder;
        return this;

    }

    public boolean isAsyncResponse() {
        return asyncResponse;
    }

    public ServiceBuilder setAsyncResponse(boolean asyncResponse) {
        this.asyncResponse = asyncResponse;
        return this;

    }

    public boolean isInvokeDynamic() {
        return invokeDynamic;
    }

    public ServiceBuilder setInvokeDynamic(boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
        return this;
    }


    public Service build() {

        if (this.getResponseQueue()==null) {

            this.setResponseQueue(responseQueueBuilder.build());
        }

        Service service = new ServiceImpl(this.getRootAddress(),
                this.getServiceAddress(),
                this.getServiceObject(),
                this.getQueueBuilder(),
                QBit.factory().createServiceMethodHandler(invokeDynamic),
                this.getResponseQueue(), asyncResponse);

        return service;
    }
}
