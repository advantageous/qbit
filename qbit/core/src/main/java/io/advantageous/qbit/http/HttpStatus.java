package io.advantageous.qbit.http;

public class HttpStatus {

    public final static int OK = 200;
    public final static String OK_MSG = "OK";

    public final static int ACCEPTED = 202;
    public final static String ACCEPTED_MSG = "REQUEST ACCEPTED";

    public final static int ERROR = 500;
    public final static String ERROR_MSG = "SERVER ERROR";

    public final static int NOT_FOUND = 404;
    public final static String NOT_FOUND_MSG = "RESOURCE NOT FOUND";

    public final static int TIMED_OUT = 408;
    public final static String TIMED_OUT_MSG = "REQUEST TIMED OUT";

    public final static int TOO_MANY_REQUEST = 429;
    public final static String TOO_MANY_REQUEST_MSG = "TOO MANY REQUEST";

    public final static String message(int code) {
        switch (code) {
            case OK: return OK_MSG;
            case NOT_FOUND: return NOT_FOUND_MSG;
            case TIMED_OUT: return TIMED_OUT_MSG;
            case TOO_MANY_REQUEST: return TOO_MANY_REQUEST_MSG;
            case ERROR: return ERROR_MSG;
            case ACCEPTED: return ACCEPTED_MSG;

            default: return "CODE = " + code;
        }
    }
}
