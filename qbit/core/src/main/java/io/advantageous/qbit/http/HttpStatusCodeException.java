package io.advantageous.qbit.http;

public class HttpStatusCodeException extends RuntimeException {

    private final int httpCode;


    public HttpStatusCodeException() {
        httpCode = 500;
    }


    public HttpStatusCodeException(final int httpCode) {
        this.httpCode = httpCode;
    }


    public HttpStatusCodeException(final int httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }


    public HttpStatusCodeException(final int httpCode, String message, Throwable cause) {
        super(message, cause);
        this.httpCode = httpCode;
    }

    public static HttpStatusCodeException httpError(int httpCode, String message) {
        return new HttpStatusCodeException(httpCode, message);
    }

    public static HttpStatusCodeException httpError(int httpCode, String message, Throwable error) {
        return new HttpStatusCodeException(httpCode, message, error);
    }

    public int code() {
        return httpCode;
    }
}
