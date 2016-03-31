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

package io.advantageous.qbit.boon.spi;

import io.advantageous.boon.core.Str;
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
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.qbit.service.Protocol.*;

/**
 * created by Richard on 9/26/14.
 *
 * @author Rick Hightower
 */
public class BoonProtocolParser implements ProtocolParser {

    private final JsonParserAndMapper jsonParser = new JsonParserFactory().create();


    @Override
    public List<Message<Object>> parse(String address, String args) {


        final char[] chars = FastStringUtils.toCharArray(args);
        if (chars.length > 2 && chars[PROTOCOL_MARKER_POSITION] == PROTOCOL_MARKER) {

            final char versionMarker = chars[VERSION_MARKER_POSITION];

            if (versionMarker == PROTOCOL_MESSAGE_TYPE_GROUP) {


                final char[][] cargs = CharScanner.splitFromStartWithLimit(chars, (char) PROTOCOL_SEPARATOR, 0, METHOD_NAME_POS + 2);
                char[] returnAddressChars = cargs[RETURN_ADDRESS_POS];

                final String returnAddress = FastStringUtils.noCopyStringFromChars(returnAddressChars);


                final char[][] messageBuffers = CharScanner.splitFrom(chars, (char) PROTOCOL_MESSAGE_SEPARATOR, 2);

                List<Message<Object>> messages = new ArrayList<>(messageBuffers.length);


                for (int index = 1; index < messageBuffers.length; index++) {
                    char[] messageBuffer = messageBuffers[index];
                    final Message<Object> m = parseMessageFromChars(returnAddress, messageBuffer);
                    messages.add(m);
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
    public List<MethodCall<Object>> parseMethodCalls(String address, String body) {
        //noinspection unchecked
        return (List<MethodCall<Object>>) (Object) parse(address, body);
    }

    @Override
    public List<Response<Object>> parseResponses(String address, String body) {
        //noinspection unchecked
        return (List<Response<Object>>) (Object) parse(address, body);
    }


    private Response<Object> parseResponseFromChars(char[] args, final String returnAddress) {
        final char[][] chars = CharScanner.splitFromStartWithLimit(args, (char) PROTOCOL_SEPARATOR, 0, RESPONSE_RETURN);


        String messageId = FastStringUtils.noCopyStringFromChars(chars[MESSAGE_ID_POS]);

        long id = 0L;
        if (!Str.isEmpty(messageId)) {
            id = Long.parseLong(messageId);
        }

        String address = FastStringUtils.noCopyStringFromChars(chars[ADDRESS_POS]);


        String stime = FastStringUtils.noCopyStringFromChars(chars[TIMESTAMP_POS]);


        long timestamp = 0L;

        if (!Str.isEmpty(stime)) {
            timestamp = Long.parseLong(stime);
        }


        char[] wasErrorsStr = chars[WAS_ERRORS_POS];


        boolean wasErrors = wasErrorsStr != null && wasErrorsStr.length == 1 && wasErrorsStr[0] == '1';

        char[] messageBodyChars = chars[RESPONSE_RETURN];

        Object messageBody;
        if (!Chr.isEmpty(messageBodyChars)) {
            messageBody = jsonParser.parse(messageBodyChars);
        } else {
            messageBody = null;
        }
        return new ResponseImpl<>(id, timestamp, address, returnAddress, null, messageBody, null, wasErrors);


    }


    private Message<Object> parseMessageFromChars(final String returnAddress, char[] chars) {

        final char messageType = chars[PROTOCOL_MESSAGE_TYPE_POSITION];

        if (messageType == PROTOCOL_MESSAGE_TYPE_METHOD) {


            return handleFastBodySubmissionVersion1Chars(returnAddress, chars);
        } else if (messageType == PROTOCOL_MESSAGE_TYPE_RESPONSE) {
            return parseResponseFromChars(chars, returnAddress);
        } else {
            die("Unsupported method call", new String(chars));
            return null;
        }

    }


    private MethodCall<Object> handleFastBodySubmissionVersion1Chars(final String returnAddress, char[] chars) {


        final char[][] args = CharScanner.splitFromStartWithLimit(chars, (char) PROTOCOL_SEPARATOR, 0, METHOD_NAME_POS + 2);


        long id = 0L;
        if (!Chr.isEmpty(args[MESSAGE_ID_POS])) {
            id = CharScanner.parseLong(args[MESSAGE_ID_POS]);
        }

        String address = FastStringUtils.noCopyStringFromChars(args[ADDRESS_POS]);


        String headerBlock = FastStringUtils.noCopyStringFromChars(args[HEADER_POS]);

        MultiMap<String, String> headers = parseHeaders(headerBlock);


        String paramBlock = FastStringUtils.noCopyStringFromChars(args[PARAMS_POS]);


        MultiMap<String, String> params = parseHeaders(paramBlock);

        String methodName = FastStringUtils.noCopyStringFromChars(args[METHOD_NAME_POS]);


        String objectName = FastStringUtils.noCopyStringFromChars(args[OBJECT_NAME_POS]);


        long timestamp = 0L;

        if (!Chr.isEmpty(args[TIMESTAMP_POS])) {
            //timestamp = Long.parseLong(stime);
            timestamp = CharScanner.parseLong(args[TIMESTAMP_POS]);
        }

        char[] body = args[ARGS_POS];


        final char[][] argumentList = CharScanner.split(body, (char) PROTOCOL_ARG_SEPARATOR);

        Object[] argList = new Object[argumentList.length];

        for (int index = 0; index < argumentList.length; index++) {
            char[] charArgs = argumentList[index];

            if (charArgs.length == 0) {
                break;
            }
            Object arg = jsonParser.parse(charArgs);
            argList[index] = arg;
        }

        return new MethodCallBuilder().setId(id).setAddress(address).setReturnAddress(returnAddress).setHeaders(headers).setObjectName(objectName).setName(methodName).setTimestamp(timestamp).setBody(argList).setParams(params).build();

    }

    public MultiMap<String, String> parseHeaders(String header) {

        if (Str.isEmpty(header)) {
            return null;
        }

        MultiMap<String, String> params = new MultiMapImpl<>();

        final char[][] split = CharScanner.split(FastStringUtils.toCharArray(header), (char) Protocol.PROTOCOL_ENTRY_HEADER_DELIM);

        for (char[] entry : split) {

            final char[][] kvSplit = CharScanner.split(entry, (char) PROTOCOL_KEY_HEADER_DELIM);
            if (kvSplit.length > 1) {
                char[] ckey = kvSplit[0];
                char[] valuesAsOne = kvSplit[1];
                final char[][] values = CharScanner.split(valuesAsOne, (char) PROTOCOL_VALUE_HEADER_DELIM);
                String key = new String(ckey);
                for (char[] value : values) {

                    params.add(key, new String(value));
                }

            }

        }

        return params;


    }


}
