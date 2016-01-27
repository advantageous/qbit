package io.advantageous.qbit.example.mdc;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.http.HttpContext;
import io.advantageous.qbit.http.HttpHeaders;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.*;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * curl http://localhost:8080/rest/mdc
 * curl http://localhost:8080/rest/callstack/queue
 * curl http://localhost:8080/rest/callstack/queue
 *
 */
@RequestMapping ("rest")
public class RestService extends BaseService {

    private final Logger logger = LoggerFactory.getLogger(RestService.class);
    private final InternalService internalServiceFromServiceQueue;
    private final InternalService internalServiceFromServiceBundle;

    public RestService(final InternalService internalServiceFromServiceBundle,
                       final InternalService internalServiceFromServiceQueue,
                       final Reactor reactor,
                       final Timer timer,
                       final StatsCollector statsCollector) {
        super(reactor, timer, statsCollector);
        this.internalServiceFromServiceBundle = internalServiceFromServiceBundle;
        this.internalServiceFromServiceQueue = internalServiceFromServiceQueue;
        reactor.addServiceToFlush(internalServiceFromServiceBundle);
        reactor.addServiceToFlush(internalServiceFromServiceQueue);
    }

    @RequestMapping ("callstack/queue")
    public void callStackFromQueue(final Callback<List<String>> callback) {
        logger.info("Logger {}", MDC.getCopyOfContextMap());
        internalServiceFromServiceQueue.getCallStack(callback);
    }

    @RequestMapping ("callstack/bundle")
    public void callStackFromBundle(final Callback<List<String>> callback) {
        logger.info("Logger {}", MDC.getCopyOfContextMap());
        internalServiceFromServiceBundle.getCallStack(callback);
    }


    @RequestMapping ("mdc")
    public void mdc(final Callback<Map<String,String>> callback) {
        logger.info("CALLED MDC");
        callback.returnThis(MDC.getCopyOfContextMap());
    }

    @RequestMapping ("ping")
    public boolean ping() {
        return true;
    }


    @RequestMapping ("http-info")
    public String httpInfo() {

        final StringBuilder builder = new StringBuilder();
        final HttpContext httpContext = new HttpContext();
        final Optional<HttpRequest> httpRequest = httpContext.getHttpRequest();
        if (httpRequest.isPresent()) {
            builder.append("URI = ").append(httpRequest.get().getUri()).append("\n");
            builder.append("HTTP Method = ").append(httpRequest.get().getMethod()).append("\n");
            builder.append("USER AGENT = ").append(
                    httpRequest.get().getHeaders().getFirst(HttpHeaders.USER_AGENT)).append("\n");
        } else {
            builder.append("request not found");
        }


        final RequestContext requestContext = new RequestContext();

        if (requestContext.getMethodCall().isPresent()) {
            final MethodCall<Object> methodCall = requestContext.getMethodCall().get();
            builder.append("Object Name = ").append(methodCall.objectName()).append("\n");
            builder.append("Method Name = ").append(methodCall.name()).append("\n");
        }
        return builder.toString();
    }


    public static void main(String... args) throws Exception {
        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder();


        managedServiceBuilder.setRootURI("/");

        managedServiceBuilder.enableLoggingMappedDiagnosticContext();

        /** Create Service from Service Queue. */
        final InternalService internalServiceFromServiceQueue = getInternalServiceFromServiceQueue(managedServiceBuilder);


        /** Create Service from Service Bundle. */
        final InternalService internalServiceFromServiceBundle = getInternalServiceFromServiceBundle(managedServiceBuilder);


        final StatsCollector statsCollectorForRest = managedServiceBuilder.getStatServiceBuilder().buildStatsCollector();

        final RestService restService = new RestService(internalServiceFromServiceBundle,
                internalServiceFromServiceQueue,
                ReactorBuilder.reactorBuilder().build(), Timer.timer(), statsCollectorForRest);

        managedServiceBuilder.addEndpointService(restService);


        managedServiceBuilder.getEndpointServerBuilder().build().startServer();


    }

    private static InternalService getInternalServiceFromServiceQueue(ManagedServiceBuilder managedServiceBuilder) {
        final InternalServiceImpl internalServiceImpl = new InternalServiceImpl(new RequestContext());
        final ServiceBuilder serviceBuilderForServiceObject = managedServiceBuilder.createServiceBuilderForServiceObject(internalServiceImpl);
        final ServiceQueue serviceQueue = serviceBuilderForServiceObject.buildAndStartAll();
        return serviceQueue.createProxy(InternalService.class);
    }


    private static InternalService getInternalServiceFromServiceBundle(ManagedServiceBuilder managedServiceBuilder) {
        final InternalServiceImpl internalServiceImpl = new InternalServiceImpl(new RequestContext());
        final ServiceBundle serviceBundle = managedServiceBuilder.createServiceBundleBuilder().build().startServiceBundle();
        serviceBundle.addServiceObject("myService", internalServiceImpl);
        return serviceBundle.createLocalProxy(InternalService.class, "myService");
    }

}
