package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.HttpServer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;


/**
 * Created by rhightower on 1/26/15.
 */
public class HttpServerVerticle extends Verticle {


    public static final String HTTP_SERVER_VERTICLE_PORT = "HttpServerVerticle.port";
    public static final String HTTP_SERVER_VERTICLE_HOST = "HttpServerVerticle.host";
    public static final String HTTP_SERVER_VERTICLE_MANAGE_QUEUES = "HttpServerVerticle.manageQueues";
    public static final String HTTP_SERVER_VERTICLE_POLL_TIME = "HttpServerVerticle.pollTime";
    public static final String HTTP_SERVER_VERTICLE_MAX_REQUEST_BATCHES = "HttpServerVerticle.maxRequestBatches";
    public static final String HTTP_SERVER_VERTICLE_FLUSH_INTERVAL = "HttpServerVerticle.flushInterval";
    public static final String HTTP_SERVER_VERTICLE_REQUEST_BATCH_SIZE = "HttpServerVerticle.requestBatchSize";
    int port = 8080;
    String host = null;
    boolean manageQueues = false;

    int pollTime;
    int requestBatchSize = 20;

    int flushInterval = 100;
    int maxRequestBatches = -1;

    HttpServerVertx httpServerVertx;


    @Override
    public void start() {

        JsonObject config = container.config();

        if (config.containsField(HTTP_SERVER_VERTICLE_PORT)) {
            port = config.getInteger(HTTP_SERVER_VERTICLE_PORT);
        }


        if (config.containsField(HTTP_SERVER_VERTICLE_HOST)) {
            host = config.getString(HTTP_SERVER_VERTICLE_HOST);
        }


        if (config.containsField(HTTP_SERVER_VERTICLE_MANAGE_QUEUES)) {
            manageQueues = config.getBoolean(HTTP_SERVER_VERTICLE_MANAGE_QUEUES);
        }

        if (config.containsField(HTTP_SERVER_VERTICLE_POLL_TIME)) {
            pollTime = config.getInteger(
                    HTTP_SERVER_VERTICLE_POLL_TIME);
        }

        if (config.containsField(HTTP_SERVER_VERTICLE_MAX_REQUEST_BATCHES)) {
            maxRequestBatches = config.getInteger(HTTP_SERVER_VERTICLE_MAX_REQUEST_BATCHES);
        }

        if (config.containsField(HTTP_SERVER_VERTICLE_FLUSH_INTERVAL)) {
            flushInterval = config.getInteger(HTTP_SERVER_VERTICLE_FLUSH_INTERVAL);
        }

        if (config.containsField(HTTP_SERVER_VERTICLE_REQUEST_BATCH_SIZE)) {
            requestBatchSize = config.getInteger(
                    HTTP_SERVER_VERTICLE_REQUEST_BATCH_SIZE);
        }


        httpServerVertx = new HttpServerVertx(port, host, manageQueues,
                                        pollTime,
                                        requestBatchSize,
                                        flushInterval,
                                        maxRequestBatches,
                                        vertx);

        beforeStart(httpServerVertx);

        httpServerVertx.start();

    }

    protected void beforeStart(HttpServer httpServerVertx) {

    }


    @Override
    public void stop() {
        httpServerVertx.stop();
    }
}
