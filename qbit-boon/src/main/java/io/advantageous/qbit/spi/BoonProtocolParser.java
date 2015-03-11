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

package io.advantageous.qbit.spi;

import io.advantageous.boon.Lists;
import io.advantageous.boon.Str;
import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.boon.primitive.CharScanner;
import io.advantageous.boon.primitive.Chr;
import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.service.Protocol;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.qbit.service.Protocol.*;

/**
 * Created by Richard on 9/26/14.
 *
 * @author Rick Hightower
 */
public class BoonProtocolParser implements ProtocolParser {

    private JsonParserAndMapper jsonParser = new JsonParserFactory().create();


    private Message<Object> parseMessageFromString(String addressPrefix, String args) {

        if ( args.isEmpty() ) {
            return null;
        }
        final char[] chars = FastStringUtils.toCharArray(args);

        return parseMessageFromChars(addressPrefix, chars, null);
    }


    @Override
    public boolean supports(Object args, MultiMap<String, String> params) {

        if ( !( args instanceof String ) ) {
            return false;
        }

        String stringArgs = ( String ) args;

        if ( stringArgs.length() > 2 &&
                stringArgs.charAt(0) == PROTOCOL_MARKER &&
                ( stringArgs.charAt(1) == PROTOCOL_MESSAGE_TYPE_METHOD ||
                        stringArgs.charAt(1) == PROTOCOL_MESSAGE_TYPE_GROUP || stringArgs.charAt(1) == PROTOCOL_MESSAGE_TYPE_RESPONSE) ) {
            return true;

        }
        return false;
    }


    @Override
    public MethodCall<Object> parseMethodCall(Object body) {

        return parseMethodCallUsingAddressPrefix("", body);
    }

    @Override
    public MethodCall<Object> parseMethodCallUsingAddressPrefix(String addressPrefix, Object body) {


        if ( body != null ) {
            if ( body instanceof String ) {
                return ( MethodCall<Object> ) ( Object ) parseMessageFromString(addressPrefix, ( String ) body);
            }
        }

        return null;


    }

    @Override
    public List<Message<Object>> parse(String address, Object body) {

        if ( !( body instanceof String ) ) {

            die("Body must be a string at this point");
            return null;

        }

        String args = ( String ) body;

        if ( args.isEmpty() ) {
            return null;
        }

        final char[] chars = FastStringUtils.toCharArray(args);
        if ( chars.length > 2 && chars[ PROTOCOL_MARKER_POSITION ] == PROTOCOL_MARKER ) {

            final char versionMarker = chars[ VERSION_MARKER_POSITION ];

            if ( versionMarker == PROTOCOL_MESSAGE_TYPE_METHOD) {
                return Lists.list((Message<Object>) handleFastBodySubmissionVersion1Chars("", chars, null));
            } else if ( versionMarker == PROTOCOL_MESSAGE_TYPE_RESPONSE) {
                return Lists.list(( Message<Object> ) parseResponseFromChars("", chars, null));
            } else if ( versionMarker == PROTOCOL_MESSAGE_TYPE_GROUP) {

                String returnAddress = null;

                final char[][] messageBuffers = CharScanner.splitFrom(chars, (char) PROTOCOL_MESSAGE_SEPARATOR, 2);

                List<Message<Object>> messages = new ArrayList<>(messageBuffers.length);

                int index = 0;

                for ( char[] methodCall : messageBuffers ) {
                    final Message<Object> m = parseMessageFromChars(address, methodCall, returnAddress);

                    if ( index == 0 ) {
                        if ( m instanceof MethodCall ) {
                            final MethodCall call = ( MethodCall ) m;
                            returnAddress = call.returnAddress();
                        } else if ( m instanceof Response ) {
                            final Response response = ( Response ) m;
                            returnAddress = response.returnAddress();
                        }
                    }
                    messages.add(m);
                    index++;
                }

                return messages;


            } else {
                die("Unsupported method call", args);
                return null;

            }
        }
        return null;

    }

    @Override
    public List<MethodCall<Object>> parseMethods(Object body) {
        return ( List<MethodCall<Object>> ) ( Object ) parse("", body);
    }

    @Override
    public List<MethodCall<Object>> parseMethodCallListUsingAddressPrefix(String addressPrefix, Object body) {
        return ( List<MethodCall<Object>> ) ( Object ) parse("", body);
    }

    @Override
    public Response<Object> parseResponse(Object body) {

        if ( body instanceof String ) {
            final char[] args = FastStringUtils.toCharArray(( String ) body);
            if ( args.length > 2 && args[ PROTOCOL_MARKER_POSITION ] == PROTOCOL_MARKER ) {

                final char versionMarker = args[ VERSION_MARKER_POSITION ];

                if ( versionMarker == PROTOCOL_MESSAGE_TYPE_RESPONSE) {


                    return parseResponseFromChars("", args, null);
                } else {
                    return null;
                }
            }


        }
        return null;
    }

    private Response<Object> parseResponseFromChars(String addressPrefix, char[] args, String parentReturnAddress) {
        final char[][] chars = CharScanner.splitFromStartWithLimit(args, ( char ) PROTOCOL_SEPARATOR, 0, RESPONSE_RETURN);


        String messageId = FastStringUtils.noCopyStringFromChars(chars[ MESSAGE_ID_POS ]);

        long id = 0L;
        if ( !Str.isEmpty(messageId) ) {
            id = Long.parseLong(messageId);
        }

        String address = FastStringUtils.noCopyStringFromChars(chars[ ADDRESS_POS ]);


        String returnAddress = FastStringUtils.noCopyStringFromChars(chars[ RETURN_ADDRESS_POS ]);


        String stime = FastStringUtils.noCopyStringFromChars(chars[ TIMESTAMP_POS ]);


        long timestamp = 0L;

        if ( !Str.isEmpty(stime) ) {
            timestamp = Long.parseLong(stime);
        }


        char[] wasErrorsStr = chars[ WAS_ERRORS_POS ];


        boolean wasErrors = wasErrorsStr != null && wasErrorsStr.length == 1 && wasErrorsStr[ 0 ] == '1';

        char[] messageBodyChars = chars[ RESPONSE_RETURN ];

        Object messageBody = null;
        if ( !Chr.isEmpty(messageBodyChars) ) {
            messageBody = jsonParser.parse(messageBodyChars);
        } else {
            messageBody = null;
        }
        return new ResponseImpl<>(id, timestamp, address, returnAddress, null, messageBody, null, wasErrors);


    }


    private Message<Object> parseMessageFromChars(String addressPrefix, char[] chars, String returnAddress) {


        if ( chars.length > 2 && chars[ PROTOCOL_MARKER_POSITION ] == PROTOCOL_MARKER ) {

            final char versionMarker = chars[ VERSION_MARKER_POSITION ];

            if ( versionMarker == PROTOCOL_MESSAGE_TYPE_METHOD) {
                return handleFastBodySubmissionVersion1Chars(addressPrefix, chars, returnAddress);
            } else if ( versionMarker == PROTOCOL_MESSAGE_TYPE_RESPONSE) {
                return parseResponseFromChars(addressPrefix, chars, returnAddress);
            } else {
                die("Unsupported method call", new String(chars));
                return null;
            }
        }
        return null;
    }


    private MethodCall<Object> handleFastBodySubmissionVersion1Chars(String addressPrefix, char[] args, String parentReturnAddress) {

        final char[][] chars = CharScanner.splitFromStartWithLimit(args, ( char ) PROTOCOL_SEPARATOR, 0, METHOD_NAME_POS + 2);



        long id = 0L;
        if ( !Chr.isEmpty(chars[ MESSAGE_ID_POS ]) ) {
            id = CharScanner.parseLong(chars[ MESSAGE_ID_POS ]);
        }

        String address = FastStringUtils.noCopyStringFromChars(chars[ ADDRESS_POS ]);


        String returnAddress = null;

        returnAddress = FastStringUtils.noCopyStringFromChars(chars[ RETURN_ADDRESS_POS ]);

        if ( !Str.isEmpty(addressPrefix) ) {
            returnAddress = Str.add(addressPrefix, "" + ( ( char ) PROTOCOL_ARG_SEPARATOR ), returnAddress);
        }


        String headerBlock = FastStringUtils.noCopyStringFromChars(chars[ HEADER_POS ]);

        MultiMap<String, String> headers = parseHeaders(headerBlock);


        String paramBlock = FastStringUtils.noCopyStringFromChars(chars[ PARAMS_POS ]);


        MultiMap<String, String> params = parseHeaders(paramBlock);

        String methodName = FastStringUtils.noCopyStringFromChars(chars[ METHOD_NAME_POS ]);


        String objectName = FastStringUtils.noCopyStringFromChars(chars[ OBJECT_NAME_POS ]);


        long timestamp = 0L;

        if ( !Chr.isEmpty(chars[ TIMESTAMP_POS ]) ) {
            //timestamp = Long.parseLong(stime);
            timestamp = CharScanner.parseLong(chars[ TIMESTAMP_POS ]);
        }

        char[] body = chars[ ARGS_POS ];


        final char[][] argumentList = CharScanner.split(body, ( char ) PROTOCOL_ARG_SEPARATOR);

        Object[] argList = new Object[ argumentList.length ];

        for ( int index = 0; index < argumentList.length; index++ ) {
            char[] charArgs = argumentList[ index ];

            if ( charArgs.length == 0 ) {
                break;
            }
            Object arg = jsonParser.parse(charArgs);
            argList[ index ] = arg;
        }

        return new MethodCallBuilder().setId(id).setAddress(address).setReturnAddress(returnAddress).setHeaders(headers).setObjectName(objectName).setName(methodName).setTimestamp(timestamp).setBody(argList).setParams(params).build();

    }

    public MultiMap<String, String> parseHeaders(String header) {

        if ( Str.isEmpty(header) ) {
            return null;
        }

        MultiMap<String, String> params = new MultiMapImpl<>();

        final char[][] split = CharScanner.split(FastStringUtils.toCharArray(header), ( char ) Protocol.PROTOCOL_ENTRY_HEADER_DELIM);

        for ( char[] entry : split ) {

            final char[][] kvSplit = CharScanner.split(entry, ( char ) PROTOCOL_KEY_HEADER_DELIM);
            if ( kvSplit.length > 1 ) {
                char[] ckey = kvSplit[ 0 ];
                char[] valuesAsOne = kvSplit[ 1 ];
                final char[][] values = CharScanner.split(valuesAsOne, ( char ) PROTOCOL_VALUE_HEADER_DELIM);
                String key = new String(ckey);
                for ( char[] value : values ) {

                    params.add(key, new String(value));
                }

            }

        }

        return params;


    }


}
