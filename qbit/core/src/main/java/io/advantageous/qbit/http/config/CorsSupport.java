package io.advantageous.qbit.http.config;

import io.advantageous.qbit.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cmathias on 8/13/15.
 * <p>
 * http://grepcode.com/file/repo1.maven.org/maven2/org.apache.tomcat/tomcat-catalina/7.0.42/org/apache/catalina/filters/CorsFilter.java
 * //http://www.html5rocks.com/static/images/cors_server_flowchart.png
 */
public class CorsSupport {

    //TODO: How is QBit handling locale type content?
    /**
     * corsFilter.invalidPreflightMaxAge=Unable to parse preflightMaxAge
     * corsFilter.nullRequest=HttpServletRequest object is null
     * corsFilter.nullRequestType=CORSRequestType object is null
     * corsFilter.onlyHttp=CORS doesn't support non-HTTP request or response
     * corsFilter.wrongType1=Expects a HttpServletRequest object of type [{0}]
     * corsFilter.wrongType2=Expects a HttpServletRequest object of type [{0}] or [{1}]
     * csrfPrevention.invalidRandomClass=Unable to create Random source using class [{0}]
     **/

    public static final String CORS_INVALID_PREFLIGHT = "Unable to parse preflightMaxAge";
    public static final String CORS_NULL_REQUEST = "Request object is null";
    public static final String CORS_NULL_REQUEST_TYPE = "CORSRequestType object is null";
    public static final String CORS_ONLY_HTTP = "CORS doesn't support non-HTTP request or response";
    public static final String CORS_WRONG_TYPE_1 = "Expects a HttpServletRequest object of type [{0}]";
    public static final String CORS_WRONG_TYPE_2 = "Expects a HttpServletRequest object of type [{0}] or [{1}]";
//    public static final String CORS_WRONG_TYPE = "csrfPrevention.invalidRandomClass";


    protected boolean corsOn;
    protected List<RequestMethod> allowedMethods = Arrays.asList(RequestMethod.values());
    protected List<String> allowedOrigins = Arrays.asList(new String[]{"*"});
    protected List<String> allowedHeaders = new ArrayList<>();
    protected List<String> exposedHeaders = new ArrayList<>();
    protected boolean allowCredentials = true;

    public boolean isCorsOn() {
        return corsOn;
    }

    public void setCorsOn(boolean corsOn) {
        this.corsOn = corsOn;
    }

    public List<RequestMethod> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<RequestMethod> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    @Override
    public String toString() {
        return "CorsSupport{" +
                "corsOn=" + corsOn +
                ", allowedMethods=" + allowedMethods +
                ", allowedOrigins=" + allowedOrigins +
                ", allowedHeaders=" + allowedHeaders +
                ", exposedHeaders=" + exposedHeaders +
                ", allowCredentials=" + allowCredentials +
                '}';
    }
}
