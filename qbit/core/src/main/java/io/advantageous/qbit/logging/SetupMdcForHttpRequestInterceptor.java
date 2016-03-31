package io.advantageous.qbit.logging;

import io.advantageous.boon.core.Str;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.service.AfterMethodCall;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.util.MultiMap;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Provides MDC support for QBit REST services.
 * Intercepts method calls to a service.
 * Looks at the originatingRequest to see if HTTP request is the originating request.
 * If an HTTP request is the originating request then we decorate the Log with
 * MDC fields.
 * <p>
 * [http://logback.qos.ch/manual/mdc.html](Mapped Diagnostic Context)
 * <p>
 * You can specify the headers that you want extracted and placed inside
 * the Mapped Diagnostic Context as well.
 */
public class SetupMdcForHttpRequestInterceptor implements BeforeMethodCall, AfterMethodCall {

    public static final String REQUEST_URI = "requestUri";
    public static final String REQUEST_REMOTE_ADDRESS = "requestRemoteAddress";
    public static final String REQUEST_HTTP_METHOD = "requestHttpMethod";
    public static final String REQUEST_HEADER_PREFIX = "requestHeader.";
    public static final String REQUEST_ID = "requestId";

    /**
     * Holds the headers that we want to extract from the request.
     */
    private final Set<String> headersToAddToLoggingMappingDiagnosticsContext;

    /**
     * Construct a SetupMdcForHttpRequestInterceptor
     *
     * @param headersToAddToLoggingMappingDiagnosticsContext headers to add to the Logging Mapping Diagnostics Context.
     */
    public SetupMdcForHttpRequestInterceptor(Set<String> headersToAddToLoggingMappingDiagnosticsContext) {
        this.headersToAddToLoggingMappingDiagnosticsContext =
                Collections.unmodifiableSet(headersToAddToLoggingMappingDiagnosticsContext);
    }

    /**
     * Gets called before a method gets invoked on a service.
     * This adds request URI, remote address and request headers of the HttpRequest if found.
     *
     * @param methodCall methodCall
     * @return true to continue, always true.
     */
    @Override
    public boolean before(final MethodCall methodCall) {

        final Optional<HttpRequest> httpRequest = findHttpRequest(methodCall);
        if (httpRequest.isPresent()) {
            extractRequestInfoAndPutItIntoMappedDiagnosticContext(httpRequest.get());
        }
        return true;
    }

    private Optional<HttpRequest> findHttpRequest(Request<Object> request) {

        if (request.originatingRequest() instanceof HttpRequest) {
            return Optional.of(((HttpRequest) request.originatingRequest()));
        } else if (request.originatingRequest() != null) {
            return findHttpRequest(request.originatingRequest());
        } else {
            return Optional.empty();
        }
    }


    /**
     * Gets called after a method completes invocation on a service.
     * Used to clear the logging Mapped Diagnostic Context.
     *
     * @param call     method call
     * @param response response from method
     * @return always true
     */
    @Override
    public boolean after(final MethodCall call, final Response response) {
        MDC.clear();
        return true;
    }

    /**
     * Extract request data and put it into the logging Mapped Diagnostic Context.
     *
     * @param httpRequest httpRequest
     */
    private void extractRequestInfoAndPutItIntoMappedDiagnosticContext(final HttpRequest httpRequest) {
        MDC.put(REQUEST_URI, httpRequest.getUri());
        MDC.put(REQUEST_REMOTE_ADDRESS, httpRequest.getRemoteAddress());
        MDC.put(REQUEST_HTTP_METHOD, httpRequest.getMethod());
        MDC.put(REQUEST_ID, Long.toString(httpRequest.getMessageId()));

        extractHeaders(httpRequest);

    }

    /**
     * Extract headersToAddToLoggingMappingDiagnosticsContext data and put them into the logging mapping diagnostics context.
     *
     * @param httpRequest httpRequest
     */
    private void extractHeaders(final HttpRequest httpRequest) {
        if (headersToAddToLoggingMappingDiagnosticsContext.size() > 0) {
            final MultiMap<String, String> headers = httpRequest.getHeaders();
            headersToAddToLoggingMappingDiagnosticsContext.forEach(header -> {
                String value = headers.getFirst(header);
                if (!Str.isEmpty(value)) {
                    MDC.put(REQUEST_HEADER_PREFIX + header, value);
                }
            });
        }
    }

}
