package org.qbit.service;

/**
 * Created by Richard on 9/26/14.
 */
public class Protocol {


    public static final String METHOD_NAME_KEY = "methodName";

    public static final int PROTOCOL_MARKER = 0x1c;

    public static final int PROTOCOL_SEPARATOR = 0x1d;


    public static final int PROTOCOL_MARKER_POSITION = 0;
    public static final int VERSION_MARKER_POSITION = 1;

    public static final int PROTOCOL_VERSION_1 = 'a';


    public static final int ADDRESS_POS = 0;
    public static final int RETURN_ADDRESS_POS = 1;
    public static final int OBJECT_NAME_POS = 2;
    public static final int METHOD_NAME_POS = 3;

}
