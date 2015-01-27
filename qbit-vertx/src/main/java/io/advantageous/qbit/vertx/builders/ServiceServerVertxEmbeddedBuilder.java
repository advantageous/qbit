package io.advantageous.qbit.vertx.builders;

import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.vertx.http.verticle.BaseHttpRelay;
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


        final PlatformManager platformManager = PlatformLocator.factory.createPlatformManager();

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




                JsonObject jsonObject = new JsonObject();
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_PORT, getPort());
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_FLUSH_INTERVAL, getFlushInterval());
                jsonObject.putBoolean(BaseHttpRelay.HTTP_RELAY_VERTICLE_MANAGE_QUEUES, isManageQueues());
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_MAX_REQUEST_BATCHES, getMaxRequestBatches());
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_POLL_TIME, getPollTime());
                jsonObject.putString(BaseHttpRelay.HTTP_RELAY_VERTICLE_HOST, getHost());
                jsonObject.putNumber(BaseHttpRelay.HTTP_RELAY_VERTICLE_REQUEST_BATCH_SIZE, getRequestBatchSize());
                jsonObject.putString(ServiceServerVerticle.SERVICE_SERVER_VERTICLE_HANDLER, getBeforeStartHandler().getName());
                jsonObject.putString(ServiceServerVerticle.SERVICE_SERVER_VERTICLE_BUNDLE_URI, getUri());

                URL[] urls = getClasspathUrls();



                platformManager.deployVerticle(ServiceServerVerticle.class.getName(), jsonObject, urls, 1, null,
                        result -> {
                            if (result.succeeded()) {
                                puts("Launched service server verticle");
                            }
                        }
                );


            }

            @Override
            public void stop() {

                platformManager.uninstallModule(ServiceServerVerticle.class.getName(),
                        result -> {
                            if (result.succeeded()) {
                                puts("Stopped service server verticle");
                            }
                        });

            }
        };
    }
}
