package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.queue.impl.BasicQueue;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.vertx.example.vertx.MultiMapWrapper;
import org.boon.core.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

import java.net.ConnectException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by rhightower on 10/28/14.
 *
 * @author rhightower
 */
public class HttpClientVertx implements HttpClient {


    private final Logger logger = LoggerFactory.getLogger(HttpClientVertx.class);

    private final boolean debug = logger.isDebugEnabled();



    /**
     * I am leaving these protected and non-final so subclasses can use injection frameworks for them.
     */
    protected  int port;
    protected  String host;
    private  int timeOutInMilliseconds;
    private  int poolSize;
    private org.vertx.java.core.http.HttpClient httpClient;
    protected Vertx vertx;

    public HttpClientVertx(int port, String host, int timeOutInMilliseconds, int poolSize) {
        this.port = port;
        this.host = host;
        this.timeOutInMilliseconds = timeOutInMilliseconds;
        this.poolSize = poolSize;
        this.vertx = VertxFactory.newVertx();
    }


    public HttpClientVertx(int port, String host) {
        this.port = port;
        this.host = host;
        this.timeOutInMilliseconds = 3000;
        this.poolSize = 5;
        this.vertx = VertxFactory.newVertx();
    }


    protected ScheduledExecutorService scheduledExecutorService;

    private  SendQueue<HttpRequest> httpRequestSendQueue;

    /**
     * Are we closed.
     */
    private final AtomicBoolean closed = new AtomicBoolean();


    private BasicQueue<HttpRequest> requestQueue;

    private final Timer timer = Timer.timer();

    private volatile long lastFlushTime;

    @Override
    public void sendHttpRequest(final HttpRequest request) {

        if(debug) logger.debug("HTTP CLIENT: sendHttpRequest:: \n{}\n", request);
        httpRequestSendQueue.send(request);
    }


    @Override
    public void run() {
        requestQueue = new BasicQueue<>("HttpClient queue " + host + ":" + port, 50, TimeUnit.MILLISECONDS, 50);
        httpRequestSendQueue = requestQueue.sendQueue();




        scheduledExecutorService = Executors.newScheduledThreadPool(2);


        this.scheduledExecutorService.schedule(this::connectWithRetry, 10, TimeUnit.MILLISECONDS);


        Sys.sleep(200);

        final org.vertx.java.core.http.HttpClient clientHttp = httpClient;



        requestQueue.startListener(new ReceiveQueueListener<HttpRequest>() {
            @Override
            public void receive(final HttpRequest request) {
                if (debug) logger.debug("HttpClientVertx::client queue listener request={}", request);
                final HttpClientRequest httpClientRequest = clientHttp.request(request.getMethod(), request.getUri(), httpClientResponse -> handleResponse(request, httpClientResponse));

                httpClientRequest.end();

                if (debug) logger.debug("HttpClientVertx::SENT \n{}", request);
            }

            @Override
            public void empty() {

                long currentTime = timer.now();

                long duration = currentTime - lastFlushTime;

                if (duration>3_000) {
                    //httpRequestSendQueue.flushSends(); nope.. can't do that
                    lastFlushTime = currentTime;
                }

            }

            @Override
            public void limit() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void idle() {

                //httpRequestSendQueue.flushSends(); nope can't do that
            }
        });

    }

    @Override
    public void flush() {
        this.httpRequestSendQueue.flushSends();
    }

    @Override
    public void stop() {
        try {
            if (this.scheduledExecutorService!=null)
            this.scheduledExecutorService.shutdown();
        } catch (Exception ex) {
            logger.warn("problem shutting down executor service for Http Client", ex);
        }

        try {
            if (requestQueue!=null) {
                requestQueue.stop();
            }
        } catch (Exception ex) {

            logger.warn("problem shutting down requestQueue for Http Client", ex);
        }

    }

    private void handleResponse(final HttpRequest request, final HttpClientResponse httpClientResponse) {
        final int statusCode = httpClientResponse.statusCode();
        final MultiMap<String, String> headers = httpClientResponse.headers().size() == 0 ? MultiMap.empty() : new MultiMapWrapper(httpClientResponse.headers());
        httpClientResponse.dataHandler(buffer -> {
            final String body = buffer.toString("UTF-8");

            handleResponseFromServer(request, statusCode, headers, body);
        });
    }

    private void handleResponseFromServer(HttpRequest request, int responseStatusCode, MultiMap<String, String> responseHeaders, String body) {
        if(debug) {
            logger.debug("HttpClientVertx::handleResponseFromServer:: request = {}, response status code = {}, \n" +
                    "response headers = {}, body = {}", request, responseStatusCode, responseHeaders, body);
        }
        request.getResponse().response(responseStatusCode, responseHeaders.get("Content-Type"), body);
    }

    private void connectWithRetry() {
        connect();
        int retry = 0;
        while (closed.get()) {

            /* Retry to connect every one second */
            Sys.sleep(1000);

            if (!closed.get()) {
                break;
            }
            retry++;
            if (retry > 10) {
                break;
            }

            if (retry % 3 == 0) {
                connect();
            }
        }
    }

    private void connect() {
        httpClient = vertx.createHttpClient().setHost(host).setPort(port)
                .setConnectTimeout(timeOutInMilliseconds).setMaxPoolSize(poolSize);


        if(debug) logger.debug("HTTP CLIENT: connect:: \nhost {} \nport {}\n", host, port);

        httpClient.exceptionHandler(throwable -> {

            if (throwable instanceof ConnectException) {
                closed.set(true);
            } else {
                logger.error("Unable to connect to " + host + " port " + port, throwable);
            }
        });

        Sys.sleep(100);

    }
}
