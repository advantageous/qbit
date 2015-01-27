package io.advantageous.qbit.vertx;

import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.vertx.service.BaseHttpRelay;
import io.advantageous.qbit.vertx.service.ServiceServerVerticle;
import org.boon.Lists;
import org.boon.Str;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 1/26/15.
 */
public class ServiceServerVertxEmbeddedBuilder extends ServiceServerBuilder {

    private int maxRequestBatches=-1;

    private int httpWorkers;
    private Class<Callback> beforeStartHandler;

    public Class<Callback> getBeforeStartHandler() {
        return beforeStartHandler;
    }

    public ServiceServerVertxEmbeddedBuilder setBeforeStartHandler(Class beforeStartHandler) {
        this.beforeStartHandler = beforeStartHandler;
        return this;
    }

    public int getMaxRequestBatches() {
        return maxRequestBatches;
    }

    public ServiceServerVertxEmbeddedBuilder setMaxRequestBatches(int maxRequestBatches) {
        this.maxRequestBatches = maxRequestBatches;
        return this;
    }

    public int getHttpWorkers() {
        return httpWorkers;
    }

    public ServiceServerVertxEmbeddedBuilder setHttpWorkers(int httpWorkers) {
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
    public ServiceServer build() {

        return new ServiceServer() {
            @Override
            public void initServices(Object... services) {

            }

            @Override
            public void initServices(Iterable services) {

            }

            @Override
            public void flush() {

            }

            @Override
            public void start() {

                PlatformManager platformManager = PlatformLocator.factory.createPlatformManager();



                JsonObject jsonObject = new JsonObject();
                jsonObject.putNumber(BaseHttpRelay.SERVICE_SERVER_VERTICLE_PORT, getPort());
                jsonObject.putNumber(BaseHttpRelay.SERVICE_SERVER_VERTICLE_FLUSH_INTERVAL, getFlushInterval());
                jsonObject.putBoolean(BaseHttpRelay.SERVICE_SERVER_VERTICLE_MANAGE_QUEUES, isManageQueues());
                jsonObject.putNumber(BaseHttpRelay.SERVICE_SERVER_VERTICLE_MAX_REQUEST_BATCHES, getMaxRequestBatches());
                jsonObject.putNumber(BaseHttpRelay.SERVICE_SERVER_VERTICLE_POLL_TIME, getPollTime());
                jsonObject.putString(BaseHttpRelay.SERVICE_SERVER_VERTICLE_HOST, getHost());
                jsonObject.putNumber(BaseHttpRelay.SERVICE_SERVER_VERTICLE_REQUEST_BATCH_SIZE, getRequestBatchSize());
                jsonObject.putString(BaseHttpRelay.SERVICE_SERVER_VERTICLE_HANDLER, getBeforeStartHandler().getName());
                jsonObject.putString(BaseHttpRelay.SERVICE_SERVER_VERTICLE_BUNDLE_URI, getUri());

                URL[] urls = getClasspathUrls();



                platformManager.deployVerticle(ServiceServerVerticle.class.getName(), jsonObject, urls, 1, null,
                        new Handler<AsyncResult<String>>() {
                            @Override
                            public void handle(AsyncResult<String> stringAsyncResult) {
                                if (stringAsyncResult.succeeded()) {
                                    puts("Launched service server verticle");
                                }
                            }
                        }
                );


            }

            @Override
            public void stop() {

            }
        };
    }
}
