/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.service;

/**
 * Protocol constants for QBit Websocket/HTTP support.
 * This defines constants for HTTP request params as well as constants for parsing a websocket stream.
 * <p>
 * Created by Richard on 9/26/14.
 *
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
    public static final int PROTOCOL_MESSAGE_TYPE_METHOD = 'm';
    public static final int PROTOCOL_MESSAGE_TYPE_GROUP = 'g';
    public static final int PROTOCOL_MESSAGE_TYPE_RESPONSE = 'r';
    public static final int PROTOCOL_MESSAGE_TYPE_EVENT = 'e';

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
