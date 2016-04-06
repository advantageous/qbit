package io.advantageous.qbit.http;

public class HttpStatus {

    public final static int OK = 200;
    public final static String OK_MSG = "OK";


    public final static int CREATED = 201;
    public final static String CREATED_MSG = "CREATED";


    public final static int ACCEPTED = 202;
    public final static String ACCEPTED_MSG = "REQUEST ACCEPTED";

    public final static int ERROR = 500;
    public final static String ERROR_MSG = "SERVER ERROR";


    public final static int SC_FORBIDDEN = 403;
    public final static String SC_FORBIDDEN_MSG = "REQUEST FORBIDDEN";

    public final static int NOT_FOUND = 404;
    public final static String NOT_FOUND_MSG = "RESOURCE NOT FOUND";


    public final static int BAD_REQUEST = 400;
    public final static String BAD_REQUEST_MSG = "BAD REQUEST";


    public final static int UNAUTHORIZED = 401;
    public final static String UNAUTHORIZED_MSG = "UNAUTHORIZED";

    public final static int TIMED_OUT = 408;
    public final static String TIMED_OUT_MSG = "REQUEST TIMED OUT";

    public final static int TOO_MANY_REQUEST = 429;
    public final static String TOO_MANY_REQUEST_MSG = "TOO MANY REQUEST";


    public final static int SERVICE_UNAVAILABLE = 503;
    public final static String SERVICE_UNAVAILABLE_MSG = "Service Unavailable";

    public final static String message(int code) {
        switch (code) {
            case OK:
                return OK_MSG;
            case CREATED:
                return CREATED_MSG;
            case NOT_FOUND:
                return NOT_FOUND_MSG;
            case BAD_REQUEST:
                return BAD_REQUEST_MSG;
            case UNAUTHORIZED:
                return UNAUTHORIZED_MSG;
            case TIMED_OUT:
                return TIMED_OUT_MSG;
            case TOO_MANY_REQUEST:
                return TOO_MANY_REQUEST_MSG;
            case ERROR:
                return ERROR_MSG;
            case ACCEPTED:
                return ACCEPTED_MSG;
            case SERVICE_UNAVAILABLE:
                return SERVICE_UNAVAILABLE_MSG;
            default:
                return "CODE = " + code;
        }
    }
}
