package io.advantageous.qbit.http.client;

/**
 * created by rhightower on 4/30/15.
 */
public class HttpClientTimeoutException extends HttpClientException {

    public HttpClientTimeoutException() {
    }

    public HttpClientTimeoutException(String message) {
        super(message);
    }

    public HttpClientTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpClientTimeoutException(Throwable cause) {
        super(cause);
    }

    public HttpClientTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
