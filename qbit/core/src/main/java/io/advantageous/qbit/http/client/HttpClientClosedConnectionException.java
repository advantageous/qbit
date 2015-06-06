package io.advantageous.qbit.http.client;

/**
 * If you try to access an HTTP client that is closed, you will get this exception.
 */
public class HttpClientClosedConnectionException extends HttpClientException {


    public HttpClientClosedConnectionException() {
    }

    public HttpClientClosedConnectionException(String message) {
        super(message);
    }

    public HttpClientClosedConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpClientClosedConnectionException(Throwable cause) {
        super(cause);
    }

    public HttpClientClosedConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
