package io.advantageous.qbit.http.server;

import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.config.CorsSupport;
import io.advantageous.qbit.http.request.decorator.HttpBinaryResponseHolder;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.request.decorator.HttpTextResponseHolder;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by cmathias on 8/13/15.
 * <p>
 * Borrowed largely from Tomcat 7 CORS Servlet impl.
 */
public class CorsResponseDecorator implements HttpResponseDecorator {

    /**
     * The Access-Control-Allow-Origin header indicates whether a resource can
     * be shared based by returning the value of the Origin request header in
     * the response.
     */
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN =
            "Access-Control-Allow-Origin";
    /**
     * The Access-Control-Allow-Credentials header indicates whether the
     * response to request can be exposed when the omit credentials flag is
     * unset. When part of the response to a preflight request it indicates that
     * the actual request can include user credentials.
     */
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS =
            "Access-Control-Allow-Credentials";
    /**
     * The Access-Control-Expose-headers header indicates which headers are safe
     * to expose to the API of a CORS API specification
     */
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS =
            "Access-Control-Expose-headers";
    /**
     * The Access-Control-Max-Age header indicates how long the results of a
     * preflight request can be cached in a preflight result cache.
     */
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_MAX_AGE =
            "Access-Control-Max-Age";
    /**
     * The Access-Control-Allow-Methods header indicates, as part of the
     * response to a preflight request, which methods can be used during the
     * actual request.
     */
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_METHODS =
            "Access-Control-Allow-Methods";
    /**
     * The Access-Control-Allow-headers header indicates, as part of the
     * response to a preflight request, which header field names can be used
     * during the actual request.
     */
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_HEADERS =
            "Access-Control-Allow-headers";
    /**
     * The Origin header indicates where the cross-origin request or preflight
     * request originates from.
     */
    public static final String REQUEST_HEADER_ORIGIN = "Origin";
    /**
     * The Access-Control-Request-Method header indicates which method will be
     * used in the actual request as part of the preflight request.
     */
    public static final String REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD =
            "Access-Control-Request-Method";
    /**
     * The Access-Control-Request-headers header indicates which headers will be
     * used in the actual request as part of the preflight request.
     */
    public static final String REQUEST_HEADER_ACCESS_CONTROL_REQUEST_HEADERS =
            "Access-Control-Request-headers";
    private static final Logger log = Logger.getLogger("CorsService");
    private final CorsSupport corsSupport;
    /**
     * Determines if any origin is allowed to make request.
     */
    private boolean anyOriginAllowed;
    /**
     * Indicates (in seconds) how long the results of a pre-flight request can
     * be cached in a pre-flight result cache.
     */
    private long preflightMaxAge = 1800;


    public CorsResponseDecorator(CorsSupport corsSupport) {
        this.corsSupport = corsSupport;
        this.anyOriginAllowed = corsSupport.getAllowedOrigins().contains("*");
    }

    /**
     * Joins elements of {@link Set} into a string, where each element is
     * separated by the provided separator.
     *
     * @param elements      The {@link Set} containing elements to join together.
     * @param joinSeparator The character to be used for separating elements.
     * @return The joined {@link String}; <code>null</code> if elements
     * {@link Set} is null.
     */
    protected static String join(final Collection<String> elements,
                                 final String joinSeparator) {
        String separator = ",";
        if (elements == null) {
            return null;
        }
        if (joinSeparator != null) {
            separator = joinSeparator;
        }
        StringBuilder buffer = new StringBuilder();
        boolean isFirst = true;
        for (String element : elements) {
            if (!isFirst) {
                buffer.append(separator);
            } else {
                isFirst = false;
            }

            if (element != null) {
                buffer.append(element);
            }
        }

        return buffer.toString();
    }

    /**
     * Checks if a given origin is valid or not. Criteria:
     * <ul>
     * <li>If an encoded character is present in origin, it's not valid.</li>
     * <li>If origin is "null", it's valid.</li>
     * <li>Origin should be a valid {@link URI}</li>
     * </ul>
     *
     * @param origin
     * @see <a href="http://tools.ietf.org/html/rfc952">RFC952</a>
     */
    protected static boolean isValidOrigin(String origin) {
        // Checks for encoded characters. Helps prevent CRLF injection.
        if (origin.contains("%")) {
            return false;
        }

        // "null" is a valid origin
        if ("null".equals(origin)) {
            return true;
        }

        URI originURI;

        try {
            originURI = new URI(origin);
        } catch (URISyntaxException e) {
            return false;
        }
        // If scheme for URI is null, return false. Return true otherwise.
        return originURI.getScheme() != null;

    }


    // -------------------------------------------------- CORS Response headers

    @Override
    public boolean decorateTextResponse(HttpTextResponseHolder responseHolder, String requestPath, String requestMethod, int code, String contentType, String payload, MultiMap<String, String> responseHeaders, MultiMap<String, String> requestHeaders, MultiMap<String, String> requestParams) {
        boolean passedCorsCheck = checkCorsAndContinue(
                new HttpRequestHolder(
                        contentType,
                        requestMethod,
                        payload.getBytes(),
                        requestPath,
                        requestHeaders,
                        requestParams),
                new HttpResponseHolder(responseHeaders)
        );

        return passedCorsCheck;
    }

    @Override
    public boolean decorateBinaryResponse(HttpBinaryResponseHolder responseHolder, String requestPath, String requestMethod, int code, String contentType, byte[] payload, MultiMap<String, String> responseHeaders, MultiMap<String, String> requestHeaders, MultiMap<String, String> requestParams) {
        boolean passedCorsCheck = checkCorsAndContinue(
                new HttpRequestHolder(
                        contentType,
                        requestMethod,
                        payload,
                        requestPath,
                        requestHeaders,
                        requestParams),
                new HttpResponseHolder(responseHeaders)
        );

        return passedCorsCheck;
    }

    private boolean checkCorsAndContinue(HttpRequestHolder requestHolder, final HttpResponseHolder responseHolder) {

        // Determines the CORS request type.
        CorsResponseDecorator.CORSRequestType requestType = checkRequestType(requestHolder);

        switch (requestType) {
            case SIMPLE:
                // Handles a Simple CORS request.
                return this.handleSimpleCORS(requestHolder, responseHolder);
            case ACTUAL:
                // Handles an Actual CORS request.
                return this.handleSimpleCORS(requestHolder, responseHolder);
            case PRE_FLIGHT:
                // Handles a Pre-flight CORS request.
                return this.handlePreflightCORS(requestHolder, responseHolder);
            case NOT_CORS:
                // Handles a Normal request that is not a cross-origin request.
                return true;
            default:
                // Handles a CORS request that violates specification.
                return this.handleInvalidCORS(requestHolder, responseHolder);
        }
    }

    /**
     * Handles a CORS request of type {@link CORSRequestType}.SIMPLE.
     */
    protected boolean handleSimpleCORS(final HttpRequestHolder request,
                                       final HttpResponseHolder response) {

        CorsResponseDecorator.CORSRequestType requestType = checkRequestType(request);
        if (!(requestType == CorsResponseDecorator.CORSRequestType.SIMPLE ||
                requestType == CorsResponseDecorator.CORSRequestType.ACTUAL)) {
            throw new IllegalArgumentException(CorsSupport.CORS_WRONG_TYPE_2);//TODO: String replacement
        }

        final String origin = request.getHeaders().get(CorsResponseDecorator.REQUEST_HEADER_ORIGIN);
        final RequestMethod method = RequestMethod.valueOf(request.getMethod());

        // Section 6.1.2
        if (!isOriginAllowed(origin)) {
            handleInvalidCORS(request, response);
            return false;
        }

        if (!corsSupport.getAllowedMethods().contains(method)) {
            handleInvalidCORS(request, response);
            return false;
        }

        // Section 6.1.3
        // Add a single Access-Control-Allow-Origin header.
        if (anyOriginAllowed && !corsSupport.isAllowCredentials()) {
            // If resource doesn't support credentials and if any origin is
            // allowed
            // to make CORS request, return header with '*'.
            response.getHeaders().add(
                    CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                    "*");
        } else {
            // If the resource supports credentials add a single
            // Access-Control-Allow-Origin header, with the value of the Origin
            // header as value.
            response.getHeaders().add(
                    CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                    origin);
        }

        // Section 6.1.3
        // If the resource supports credentials, add a single
        // Access-Control-Allow-Credentials header with the case-sensitive
        // string "true" as value.
        if (corsSupport.isAllowCredentials()) {
            response.getHeaders().add(
                    CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS,
                    "true");
        }

        // Section 6.1.4
        // If the list of exposed headers is not empty add one or more
        // Access-Control-Expose-headers headers, with as values the header
        // field names given in the list of exposed headers.
        if ((corsSupport.getExposedHeaders() != null) && (corsSupport.getExposedHeaders().size() > 0)) {
            String exposedHeadersString = join(corsSupport.getExposedHeaders(), ",");
            response.getHeaders().add(
                    CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS,
                    exposedHeadersString);
        }

        return true;
    }

    /**
     * Handles CORS pre-flight request.
     */
    protected boolean handlePreflightCORS(final HttpRequestHolder request,
                                          final HttpResponseHolder response) {

        CORSRequestType requestType = checkRequestType(request);
        if (requestType != CORSRequestType.PRE_FLIGHT) {
            throw new IllegalArgumentException(CorsSupport.CORS_WRONG_TYPE_2);
            //TODO: String replace into above CORSRequestType.PRE_FLIGHT.name().toLowerCase(Locale.ENGLISH)));
        }

        final String origin = request.getHeaders().get(CorsResponseDecorator.REQUEST_HEADER_ORIGIN);

        // Section 6.2.2
        if (!isOriginAllowed(origin)) {
            handleInvalidCORS(request, response);
            return false;
        }

        // Section 6.2.3
        String accessControlRequestMethod = request.getHeaders().get(
                CorsResponseDecorator.REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD);
        if (accessControlRequestMethod == null) {
            handleInvalidCORS(request, response);
            return false;
        } else {
            accessControlRequestMethod = accessControlRequestMethod.trim();
        }

        // Section 6.2.4
        String accessControlRequestHeadersHeader = request.getHeaders().get(
                CorsResponseDecorator.REQUEST_HEADER_ACCESS_CONTROL_REQUEST_HEADERS);
        List<String> accessControlRequestHeaders = new LinkedList<>();
        if (accessControlRequestHeadersHeader != null &&
                !accessControlRequestHeadersHeader.trim().isEmpty()) {
            String[] headers = accessControlRequestHeadersHeader.trim().split(
                    ",");
            for (String header : headers) {
                accessControlRequestHeaders.add(header.trim().toLowerCase(Locale.ENGLISH));
            }
        }

        // Section 6.2.5
        if (!corsSupport.getAllowedHeaders().contains(accessControlRequestMethod)) {
            handleInvalidCORS(request, response);
            return false;
        }

        // Section 6.2.6
        if (!accessControlRequestHeaders.isEmpty()) {
            for (String header : accessControlRequestHeaders) {
                if (!corsSupport.getAllowedHeaders().contains(header)) {
                    handleInvalidCORS(request, response);
                    return false;
                }
            }
        }

        // Section 6.2.7
        if (corsSupport.isAllowCredentials()) {
            response.getHeaders().add(
                    CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                    origin);
            response.getHeaders().add(
                    CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS,
                    "true");
        } else {
            if (anyOriginAllowed) {
                response.getHeaders().add(
                        CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                        "*");
            } else {
                response.getHeaders().add(
                        CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                        origin);
            }
        }

        // Section 6.2.8
        if (preflightMaxAge > 0) {
            response.getHeaders().add(
                    CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_MAX_AGE,
                    String.valueOf(preflightMaxAge));
        }

        // Section 6.2.9
        response.getHeaders().add(
                CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_METHODS,
                accessControlRequestMethod);

        // Section 6.2.10
        if ((corsSupport.getAllowedHeaders() != null) && (!corsSupport.getAllowedHeaders().isEmpty())) {
            response.getHeaders().add(
                    CorsResponseDecorator.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_HEADERS,
                    join(corsSupport.getAllowedHeaders(), ","));
        }

        return true;

    }

    /**
     * Handles a CORS request that violates specification.
     */
    private boolean handleInvalidCORS(final HttpRequestHolder request,
                                      final HttpResponseHolder response) {
        String origin = request.getHeaders().get(CorsResponseDecorator.REQUEST_HEADER_ORIGIN);
        String method = request.getMethod();
        String accessControlRequestHeaders = request.getHeaders().get(
                REQUEST_HEADER_ACCESS_CONTROL_REQUEST_HEADERS);

        response.getHeaders().put("Content-Type", "text/plain");

        //TODO: Rick note that the integration-point implementation still prevents me from decorating the http response code
        //response.code = HttpStatus.SC_FORBIDDEN;

        if (log.getLevel() == Level.FINE) {
            // Debug so no need for i18n
            StringBuilder message =
                    new StringBuilder("Invalid CORS request; Origin=");
            message.append(origin);
            message.append(";Method=");
            message.append(method);
            if (accessControlRequestHeaders != null) {
                message.append(";Access-Control-Request-headers=");
                message.append(accessControlRequestHeaders);
            }
            log.fine(message.toString());
        }

        return false;
    }

    // -------------------------------------------------- CORS Request headers

    /**
     * Determines the request type.
     *
     * @param request
     */
    protected CORSRequestType checkRequestType(final HttpRequestHolder request) {
        CORSRequestType requestType = CORSRequestType.INVALID_CORS;
        if (request == null) {
            throw new IllegalArgumentException(CorsSupport.CORS_NULL_REQUEST);
        }
        String originHeader = request.getHeaders().get(REQUEST_HEADER_ORIGIN);
        // Section 6.1.1 and Section 6.2.1
        if (originHeader != null) {
            if (originHeader.isEmpty()) {
                requestType = CORSRequestType.INVALID_CORS;
            } else if (!isValidOrigin(originHeader)) {
                requestType = CORSRequestType.INVALID_CORS;
            } else if (isLocalOrigin(request, originHeader)) {
                return CORSRequestType.NOT_CORS;
            } else {
                String method = request.getMethod();
                if (method != null) {
                    if ("OPTIONS".equals(method)) {
                        String accessControlRequestMethodHeader =
                                request.getHeaders().get(
                                        REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD);
                        if (accessControlRequestMethodHeader != null &&
                                !accessControlRequestMethodHeader.isEmpty()) {
                            requestType = CORSRequestType.PRE_FLIGHT;
                        } else if (accessControlRequestMethodHeader != null &&
                                accessControlRequestMethodHeader.isEmpty()) {
                            requestType = CORSRequestType.INVALID_CORS;
                        } else {
                            requestType = CORSRequestType.ACTUAL;
                        }
                    } else if ("GET".equals(method) || "HEAD".equals(method)) {
                        requestType = CORSRequestType.SIMPLE;
                    } else if ("POST".equals(method)) {
                        String mediaType = request.getContentType();
                        if (mediaType != null) {
//                                if (SIMPLE_HTTP_REQUEST_CONTENT_TYPE_VALUES
//                                        .contains(mediaType)) {
//                                    requestType = CORSRequestType.SIMPLE;
//                                } else {
//                                    requestType = CORSRequestType.ACTUAL;
//                                }

                            //TODO: Will QBit support non-simple?
                            requestType = CORSRequestType.SIMPLE;
                        }
                    } else {
                        requestType = CORSRequestType.ACTUAL;
                    }
                }
            }
        } else {
            requestType = CORSRequestType.NOT_CORS;
        }

        return requestType;
    }

    private boolean isLocalOrigin(HttpRequestHolder request, String origin) {

        // Build scheme://host:port from request
        StringBuilder target = new StringBuilder();
        URI uri = URI.create(request.getRequestUri());

        String scheme = uri.getScheme();
        if (scheme == null) {
            return false;
        } else {
            scheme = scheme.toLowerCase(Locale.ENGLISH);
        }
        target.append(scheme);
        target.append("://");

        String host = uri.getHost();
        if (host == null) {
            return false;
        }
        target.append(host);

        int port = uri.getPort();
        if ("http".equals(scheme) && port != 80 ||
                "https".equals(scheme) && port != 443) {
            target.append(':');
            target.append(port);
        }

        return origin.equalsIgnoreCase(target.toString());
    }

    /**
     * Checks if the Origin is allowed to make a CORS request.
     *
     * @param origin The Origin.
     * @return <code>true</code> if origin is allowed; <code>false</code>
     * otherwise.
     */
    private boolean isOriginAllowed(final String origin) {
        if (anyOriginAllowed) {
            return true;
        }

        // If 'Origin' header is a case-sensitive match of any of allowed
        // origins, then return true, else return false.
        return corsSupport.getAllowedOrigins().contains(origin);
    }


    // -------------------------------------------------------------- Constants

    /**
     * Enumerates varies types of CORS requests. Also, provides utility methods
     * to determine the request type.
     */
    protected enum CORSRequestType {
        /**
         * A simple HTTP request, i.e. it shouldn't be pre-flighted.
         */
        SIMPLE,
        /**
         * A HTTP request that needs to be pre-flighted.
         */
        ACTUAL,
        /**
         * A pre-flight CORS request, to get meta information, before a
         * non-simple HTTP request is sent.
         */
        PRE_FLIGHT,
        /**
         * Not a CORS request, but a normal request.
         */
        NOT_CORS,
        /**
         * An invalid CORS request, i.e. it qualifies to be a CORS request, but
         * fails to be a valid one.
         */
        INVALID_CORS
    }

    class HttpRequestHolder {
        private String contentType;
        private String method;
        private byte[] payload;
        private String requestUri;
        private MultiMap<String, String> headers;
        private MultiMap<String, String> params;

        public HttpRequestHolder(String contentType, String requestMethod, byte[] payload, String requestUri, MultiMap<String, String> Headers, MultiMap<String, String> params) {
            this.contentType = contentType;
            this.method = requestMethod;
            this.payload = payload;
            this.requestUri = requestUri;
            this.headers = Headers;
            this.params = params;
        }

        public String getContentType() {
            return contentType;
        }

        public String getMethod() {
            return method;
        }

        public byte[] getPayload() {
            return payload;
        }

        public String getRequestUri() {
            return requestUri;
        }

        public MultiMap<String, String> getHeaders() {
            if (headers == null) {
                headers = new MultiMapImpl<>();
            }
            return headers;
        }

        public MultiMap<String, String> getParams() {
            if (params == null) {
                params = new MultiMapImpl<>();
            }
            return params;
        }
    }

    class HttpResponseHolder {
        private MultiMap<String, String> headers;

        public HttpResponseHolder(MultiMap<String, String> headers) {
            this.headers = headers;
        }

        public MultiMap<String, String> getHeaders() {
            if (headers == null) {
                headers = new MultiMapImpl<>();
            }
            return headers;
        }
    }
}
