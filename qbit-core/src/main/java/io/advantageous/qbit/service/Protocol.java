package io.advantageous.qbit.service;

/**
 * Protocol constants for QBit Websocket/HTTP support.
 * This defines constants for HTTP request params as well as constants for parsing a websocket stream.
 *
 * Created by Richard on 9/26/14.
 * @author rhightower
 */
public class Protocol {


    public static final String METHOD_NAME_KEY = "methodName";

    public static final String OBJECT_NAME_KEY = "objectName";


    public static final String ADDRESS_KEY = "addressOfService";

    public static final String RETURN_ADDRESS_KEY = "addressOfReturn";

    public static final int PROTOCOL_MARKER = 0x1c;


    public static final int PROTOCOL_MESSAGE_SEPARATOR = 0x1f;

    public static final int PROTOCOL_SEPARATOR = 0x1d;


    public static final int PROTOCOL_ARG_SEPARATOR = 0x1e;


    public static final int PROTOCOL_KEY_HEADER_DELIM = 0x1a;

    public static final int PROTOCOL_ENTRY_HEADER_DELIM = 0x19;

    public static final int PROTOCOL_VALUE_HEADER_DELIM = 0x15;

    public static final int PROTOCOL_MARKER_POSITION = 0;
    public static final int VERSION_MARKER_POSITION = 1;

    public static final int PROTOCOL_VERSION_1 = 'a';
    public static final int PROTOCOL_VERSION_1_GROUP = 'g';
    public static final int PROTOCOL_VERSION_1_RESPONSE = 'r';


    public static final int MESSAGE_ID_POS = 1;
    public static final int ADDRESS_POS = 2;
    public static final int RETURN_ADDRESS_POS = 3;
    public static final int HEADER_POS = 4;
    public static final int PARAMS_POS = 5;
    public static final int OBJECT_NAME_POS = 6;
    public static final int METHOD_NAME_POS = 7;
    public static final int TIMESTAMP_POS = 8;
    public static final int ARGS_POS = 9;

    public static final int WAS_ERRORS_POS = 9;
    public static final int RESPONSE_RETURN = 10;


}
