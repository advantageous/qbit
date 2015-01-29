package io.advantageous.qbit.vertx.builders;

import io.advantageous.qbit.http.*;
import io.advantageous.qbit.vertx.http.verticle.BaseHttpRelay;
import io.advantageous.qbit.vertx.http.verticle.HttpHandlerConcentratorVerticle;
import io.advantageous.qbit.vertx.service.ServiceServerVerticle;
import org.boon.Lists;
import org.boon.Str;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 1/27/15.
 */
public class HttpServerVertxEmbeddedBuilder extends HttpServerBuilder {


    private int httpWorkers=4;
    private Class<HttpServerHttpHandler> handlerCallbackClass;

    public Class<HttpServerHttpHandler> getHandlerCallbackClass() {
        return handlerCallbackClass;
    }

    public HttpServerVertxEmbeddedBuilder setHandlerCallbackClass(Class handlerCallbackClass) {
        this.handlerCallbackClass = handlerCallbackClass;
        return this;
    }

    public int getHttpWorkers() {
        return httpWorkers;
    }

    public HttpServerVertxEmbeddedBuilder setHttpWorkers(int httpWorkers) {
        this.httpWorkers = httpWorkers;
        return this;
    }

    private URL[] getClasspathUrls() {


        final String classpathString = System.getProperty("java.class.path");

        final List<String> classpathStrings = Lists.list(Str.split(classpathString, ':'));

        final List<URL> urlList = new ArrayList<>(classpathStrings.size());

        for (String path : classpathStrings) {
            File file = new File(path);
            try {
                urlList.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return urlList.toArray(new URL[urlList.size()]);
    }



    @Override
    public HttpServer build() {


        final PlatformManager platformManager = PlatformLocator.factory.createPlatformManager();

        return new HttpServer() {

            @Override
            public void start() {




                JsonObject jsonObject = new JsonObject();
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_PORT, getPort());
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_HTTP_WORKERS, getHttpWorkers());
                jsonObject.putString(HttpHandlerConcentratorVerticle.HTTP_HANDLER_VERTICLE_HANDLER, getHandlerCallbackClass().getName());
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_FLUSH_INTERVAL, getFlushInterval());
                jsonObject.putBoolean(BaseHttpRelay.HTTP_RELAY_VERTICLE_MANAGE_QUEUES, isManageQueues());
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_MAX_REQUEST_BATCHES, getMaxRequestBatches());
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_POLL_TIME, getPollTime());
                jsonObject.putString(BaseHttpRelay.HTTP_RELAY_VERTICLE_HOST, getHost());
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_REQUEST_BATCH_SIZE, getRequestBatchSize());

                URL[] urls = getClasspathUrls();



                platformManager.deployVerticle(HttpHandlerConcentratorVerticle.class.getName(), jsonObject, urls, 1, null,
                        result -> {
                            if (result.succeeded()) {
                                puts("Launched service http server verticle");
                            }
                        }
                );


            }

            @Override
            public void stop() {

                platformManager.uninstallModule(ServiceServerVerticle.class.getName(),
                        result -> {
                            if (result.succeeded()) {
                                puts("Stopped http server verticle");
                            }
                        });

            }

            @Override
            public void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

            }

            @Override
            public void setWebSocketCloseConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

            }

            @Override
            public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {

            }

            @Override
            public void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer) {

            }

            @Override
            public void setWebSocketIdleConsume(Consumer<Void> idleConsumer) {

            }
        };
    }
}
