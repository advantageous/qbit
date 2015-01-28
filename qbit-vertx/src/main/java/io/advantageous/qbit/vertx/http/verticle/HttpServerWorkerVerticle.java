package io.advantageous.qbit.vertx.http.verticle;

import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.vertx.http.HttpServerVertx;
import org.boon.core.reflection.BeanUtils;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.fields.FieldAccess;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


/**
 * Created by rhightower on 1/26/15.
 */
public class HttpServerWorkerVerticle extends Verticle {


    public static final String HTTP_SERVER_VERTICLE_PORT = "HttpServerVerticle.port";
    public static final String HTTP_SERVER_VERTICLE_HOST = "HttpServerVerticle.host";
    public static final String HTTP_SERVER_VERTICLE_MANAGE_QUEUES = "HttpServerVerticle.manageQueues";
    public static final String HTTP_SERVER_VERTICLE_POLL_TIME = "HttpServerVerticle.pollTime";
    public static final String HTTP_SERVER_VERTICLE_MAX_REQUEST_BATCHES = "HttpServerVerticle.maxRequestBatches";
    public static final String HTTP_SERVER_VERTICLE_FLUSH_INTERVAL = "HttpServerVerticle.flushInterval";
    public static final String HTTP_SERVER_VERTICLE_REQUEST_BATCH_SIZE = "HttpServerVerticle.requestBatchSize";

    public static final String HTTP_SERVER_HANDLER = "HttpServerVerticle.handler";

    public static final String HTTP_SERVER_ID = "HttpServerVerticle.serverId";

    int port = 8080;
    String host = null;
    String serverId;
    boolean manageQueues = false;

    int pollTime;
    int requestBatchSize = 20;

    int flushInterval = 100;
    int maxRequestBatches = -1;

    HttpServerVertx httpServerVertx;

    Consumer<HttpServer> beforeCallbackHandler;


    @Override
    public void start() {

        JsonObject config = container.config();

        if (config.containsField(HTTP_SERVER_ID)) {
            serverId = config.getString(HTTP_SERVER_ID);
        } else {
            serverId = UUID.randomUUID().toString();
        }

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

        if (config.containsField(HTTP_SERVER_HANDLER)) {
            String handlerClass = config.getString(HTTP_SERVER_HANDLER);

            try {
                beforeCallbackHandler = (Consumer<HttpServer>) Class.forName(handlerClass).newInstance();

                final Map<String, FieldAccess> fieldMap = ClassMeta
                        .classMeta(beforeCallbackHandler.getClass())
                        .fieldMap();

                if (fieldMap.containsKey("vertx")) {
                    BeanUtils.setPropertyValue(beforeCallbackHandler, vertx, "vertx");
                }


                if (fieldMap.containsKey("serverId")) {
                    BeanUtils.setPropertyValue(beforeCallbackHandler, serverId, "serverId");
                }

            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

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

    protected void beforeStart(HttpServer httpServer) {

        if (beforeCallbackHandler!=null) {
            beforeCallbackHandler.accept(httpServer);
        }

    }


    @Override
    public void stop() {
        httpServerVertx.stop();
    }
}
