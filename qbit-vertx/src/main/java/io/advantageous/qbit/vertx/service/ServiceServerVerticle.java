package io.advantageous.qbit.vertx.service;

import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.vertx.http.HttpServerVerticle;
import org.boon.core.reflection.BeanUtils;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.fields.FieldAccess;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 1/26/15.
 */
public class ServiceServerVerticle extends Verticle {


    int port = 8080;
    String host = null;
    boolean manageQueues = false;

    int pollTime;
    int requestBatchSize = 20;

    int flushInterval = 100;
    int maxRequestBatches = -1;

    int httpWorkers = 4;

    String handerClassName = null;


    public static final String SERVICE_SERVER_VERTICLE_PORT = "ServiceServerVerticle.port";

    public static final String SERVICE_SERVER_VERTICLE_HTTP_WORKERS = "ServiceServerVerticle.httpWorkers";
    public static final String SERVICE_SERVER_VERTICLE_HOST = "ServiceServerVerticle.host";
    public static final String SERVICE_SERVER_VERTICLE_MANAGE_QUEUES = "ServiceServerVerticle.manageQueues";
    public static final String SERVICE_SERVER_VERTICLE_POLL_TIME = "ServiceServerVerticle.pollTime";
    public static final String SERVICE_SERVER_VERTICLE_MAX_REQUEST_BATCHES = "ServiceServerVerticle.maxRequestBatches";
    public static final String SERVICE_SERVER_VERTICLE_FLUSH_INTERVAL = "ServiceServerVerticle.flushInterval";
    public static final String SERVICE_SERVER_VERTICLE_REQUEST_BATCH_SIZE = "ServiceServerVerticle.requestBatchSize";
    public static final String SERVICE_SERVER_VERTICLE_HANDLER = "ServiceServerVerticle.handler";


    Consumer<ServiceBundle> beforeCallbackHandler;



    @Override
    public void start() {




        extractConfig();





        JsonObject jsonObject = new JsonObject();
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_PORT, this.port);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_FLUSH_INTERVAL, this.flushInterval);
        jsonObject.putBoolean(HttpServerVerticle.HTTP_SERVER_VERTICLE_MANAGE_QUEUES, this.manageQueues);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_MAX_REQUEST_BATCHES, this.maxRequestBatches);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_POLL_TIME, this.pollTime);
        jsonObject.putString(HttpServerVerticle.HTTP_SERVER_VERTICLE_HOST, this.host);
        jsonObject.putNumber(HttpServerVerticle.HTTP_SERVER_VERTICLE_REQUEST_BATCH_SIZE, this.requestBatchSize);
        jsonObject.putString(HttpServerVerticle.HTTP_SERVER_HANDLER, BeforeStartHandler.class.getName());





        container.deployVerticle(HttpServerVerticle.class.getName(), jsonObject,  httpWorkers,
                new Handler<AsyncResult<String>>() {
                    @Override
                    public void handle(AsyncResult<String> stringAsyncResult) {
                        if (stringAsyncResult.succeeded()) {
                            puts("Launched verticle");
                        }
                    }
                }
        );

        try {
            beforeCallbackHandler = (Consumer<ServiceBundle>) Class.forName(handerClassName).newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        final Map<String, FieldAccess> fieldMap = ClassMeta
                .classMeta(beforeCallbackHandler.getClass())
                .fieldMap();

        if (fieldMap.containsKey("vertx")) {
            BeanUtils.setPropertyValue(beforeCallbackHandler, vertx, "vertx");
        }
    }

    private void extractConfig() {
        JsonObject config = container.config();

        if (config.containsField(SERVICE_SERVER_VERTICLE_HANDLER)) {
            handerClassName = config.getString(SERVICE_SERVER_VERTICLE_HANDLER);
        }



        if (config.containsField(SERVICE_SERVER_VERTICLE_PORT)) {
            port = config.getInteger(SERVICE_SERVER_VERTICLE_PORT);
        }


        if (config.containsField(SERVICE_SERVER_VERTICLE_HOST)) {
            host = config.getString(SERVICE_SERVER_VERTICLE_HOST);
        }


        if (config.containsField(SERVICE_SERVER_VERTICLE_MANAGE_QUEUES)) {
            manageQueues = config.getBoolean(SERVICE_SERVER_VERTICLE_MANAGE_QUEUES);
        }

        if (config.containsField(SERVICE_SERVER_VERTICLE_POLL_TIME)) {
            pollTime = config.getInteger(
                    SERVICE_SERVER_VERTICLE_POLL_TIME);
        }

        if (config.containsField(SERVICE_SERVER_VERTICLE_MAX_REQUEST_BATCHES)) {
            maxRequestBatches = config.getInteger(SERVICE_SERVER_VERTICLE_MAX_REQUEST_BATCHES);
        }

        if (config.containsField(SERVICE_SERVER_VERTICLE_FLUSH_INTERVAL)) {
            flushInterval = config.getInteger(SERVICE_SERVER_VERTICLE_FLUSH_INTERVAL);
        }

        if (config.containsField(SERVICE_SERVER_VERTICLE_REQUEST_BATCH_SIZE)) {
            requestBatchSize = config.getInteger(
                    SERVICE_SERVER_VERTICLE_REQUEST_BATCH_SIZE);
        }

        if (config.containsField(SERVICE_SERVER_VERTICLE_HTTP_WORKERS)) {
            httpWorkers = config.getInteger(
                    SERVICE_SERVER_VERTICLE_HTTP_WORKERS);
        }
    }

}
