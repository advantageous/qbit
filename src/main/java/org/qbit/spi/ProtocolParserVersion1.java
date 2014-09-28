package org.qbit.spi;

import org.boon.Lists;
import org.boon.Str;
import org.boon.collections.MultiMap;
import org.boon.collections.MultiMaps;
import org.boon.core.reflection.FastStringUtils;
import org.boon.json.JsonParserAndMapper;
import org.boon.json.JsonParserFactory;
import org.boon.primitive.CharScanner;
import org.qbit.message.Message;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.service.Protocol;
import org.qbit.service.method.impl.MethodCallImpl;
import org.qbit.service.method.impl.ResponseImpl;

import java.util.ArrayList;
import java.util.List;

import static org.boon.Exceptions.die;
import static org.qbit.service.Protocol.*;

/**
 * Created by Richard on 9/26/14.
 */
public class ProtocolParserVersion1 implements ProtocolParser {

    @Override
    public boolean supports(Object args, MultiMap<String, String> params) {

            if (!( args instanceof String)) {
                return false;
            }

            String sargs = (String) args;

            if ( sargs.length() > 2 &&
                    sargs.charAt(0) == PROTOCOL_MARKER &&
                    (sargs.charAt(1) == PROTOCOL_VERSION_1 ||
                            sargs.charAt(1)  == PROTOCOL_VERSION_1_GROUP
                            || sargs.charAt(1)  == PROTOCOL_VERSION_1_RESPONSE
                    )) {
                return true;

            }
            return false;
    }

    @Override
    public MethodCall<Object> parseMethodCall(Object body) {





        if (body!=null) {
            if (body instanceof String) {
                return (MethodCall<Object>) (Object) parseMessageFromString((String) body);
            }
        }

        return null;


    }

    @Override
    public List<Message<Object>> parse(Object body) {

        if (! (body instanceof String)) {

            die("Body must be a string at this point");

        }

        String args = (String)body;

        if (args.isEmpty()) {
            return null;
        }

        final char[] chars = FastStringUtils.toCharArray(args);
        if (chars.length > 2 &&
                chars[PROTOCOL_MARKER_POSITION]
                        == PROTOCOL_MARKER) {

            final char versionMarker = chars[VERSION_MARKER_POSITION];

            if (versionMarker == PROTOCOL_VERSION_1) {
                return Lists.list((Message<Object>)handleFastBodySubmissionVersion1Chars(chars));
            } else if (versionMarker == PROTOCOL_VERSION_1_GROUP){

                final char[][] methodCalls = CharScanner.splitFrom(chars,
                        (char) PROTOCOL_MESSAGE_SEPARATOR, 2);

                List<Message<Object>> messages = new ArrayList<>(methodCalls.length);
                for (char[] methodCall : methodCalls) {
                    final Message<Object> m = parseMessageFromChars(methodCall);
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
    public List<MethodCall<Object>> parseMethods(Object body) {
        return  (List<MethodCall<Object>>) (Object) parse(body);
    }

    @Override
    public Response<Object> parseResponse(Object body) {

        if (body instanceof  String) {
            final char[] args = FastStringUtils.toCharArray((String) body);
            if (args.length > 2 &&
                    args[PROTOCOL_MARKER_POSITION]
                            == PROTOCOL_MARKER) {

                final char versionMarker = args[VERSION_MARKER_POSITION];

                if (versionMarker == PROTOCOL_VERSION_1_RESPONSE) {


                    return parseResponseFromChars(args);
                } else {
                    return null;
                }
            }


        }
        return null;
    }

    private Response<Object> parseResponseFromChars(char[] args) {
            final char[][] chars = CharScanner.splitFromStartWithLimit(args,
                    (char) PROTOCOL_SEPARATOR, 0, METHOD_NAME_POS + 2);


            String messageId = FastStringUtils.noCopyStringFromChars(chars[
                    MESSAGE_ID_POS]);

            long id = 0L;
            if (!Str.isEmpty(messageId)) {
                id = Long.parseLong(messageId);
            }

            String address = FastStringUtils.noCopyStringFromChars(chars[
                    ADDRESS_POS]);

            String returnAddress = FastStringUtils.noCopyStringFromChars(chars[
                    RETURN_ADDRESS_POS]);




            String stime = FastStringUtils.noCopyStringFromChars(chars[
                    TIMESTAMP_POS]);

            long timestamp = 0L;

            if (!Str.isEmpty(stime)) {
                timestamp = Long.parseLong(stime);
            }

            char[] messageBodyChars = chars[ ARGS_POS ];
            Object messageBody = jsonParserThreadLocal.get().parse(messageBodyChars);

            return new ResponseImpl<>( id,  timestamp,  address,  returnAddress, null, messageBody);



    }


    private static ThreadLocal<JsonParserAndMapper> jsonParserThreadLocal = new ThreadLocal<JsonParserAndMapper>() {
        @Override
        protected JsonParserAndMapper initialValue() {
            return new JsonParserFactory().create();
        }
    };


    private Message<Object> parseMessageFromString(String args) {

        if (args.isEmpty()) {
            return null;
        }
        final char[] chars = FastStringUtils.toCharArray(args);

        return parseMessageFromChars(chars);
    }


    private Message<Object> parseMessageFromChars(char[] chars) {


        if (chars.length > 2 &&
                chars[PROTOCOL_MARKER_POSITION]
                        == PROTOCOL_MARKER) {

            final char versionMarker = chars[VERSION_MARKER_POSITION];

            if (versionMarker == PROTOCOL_VERSION_1) {
                return handleFastBodySubmissionVersion1Chars(chars);
            } else if (versionMarker == PROTOCOL_VERSION_1_RESPONSE) {
                return parseResponseFromChars(chars);
            }
            else {
                die("Unsupported method call", new String(chars));
                return null;
            }
        }
        return null;
    }


    private MethodCallImpl handleFastBodySubmissionVersion1Chars(char[] args) {

        final char[][] chars = CharScanner.splitFromStartWithLimit(args,
                (char) PROTOCOL_SEPARATOR, 0, METHOD_NAME_POS+2);


        String messageId = FastStringUtils.noCopyStringFromChars(chars[
                MESSAGE_ID_POS]);

        long id = 0L;
        if (!Str.isEmpty(messageId)) {
            id = Long.parseLong(messageId);
        }

        String address = FastStringUtils.noCopyStringFromChars(chars[
                ADDRESS_POS]);

        String returnAddress = FastStringUtils.noCopyStringFromChars(chars[
                RETURN_ADDRESS_POS]);


        String headerBlock = FastStringUtils.noCopyStringFromChars(chars[
                HEADER_POS]);

        MultiMap<String, String> headers = parseHeaders(headerBlock);


        String paramBlock = FastStringUtils.noCopyStringFromChars(chars[
                PARAMS_POS]);


        MultiMap<String, String> params = parseHeaders(paramBlock);

        String methodName = FastStringUtils.noCopyStringFromChars(chars[
                METHOD_NAME_POS]);


        String objectName = FastStringUtils.noCopyStringFromChars(chars[
                OBJECT_NAME_POS]);


        String stime = FastStringUtils.noCopyStringFromChars(chars[
                TIMESTAMP_POS]);

        long timestamp = 0L;

        if (!Str.isEmpty(stime)) {
            timestamp = Long.parseLong(stime);
        }

        char[] body = chars[ ARGS_POS ];


        final char[][] argumentList = CharScanner.split(body,
                (char) PROTOCOL_ARG_SEPARATOR);

        List<Object> argList = new ArrayList<>();

        for (int index=0; index< argumentList.length; index++) {
            char [] charArgs = argumentList[index];
            Object arg = jsonParserThreadLocal.get().parse(charArgs);
            argList.add(arg);
        }

        MethodCallImpl methodCall =  MethodCallImpl.method(id, address, returnAddress, objectName, methodName, timestamp, argList, params);

        methodCall.headers(headers);
        return methodCall;

    }

    public MultiMap<String, String> parseHeaders(String header) {

        if (Str.isEmpty(header)) {
            return null;
        }

        MultiMap<String, String> params = MultiMaps.multiMap();

        final char[][] split = CharScanner.split(FastStringUtils.toCharArray(header), (char) Protocol.PROTOCOL_ENTRY_HEADER_DELIM);

        for (char [] entry : split) {

            final char[][] kvSplit = CharScanner.split(entry, (char) PROTOCOL_KEY_HEADER_DELIM);
            if (kvSplit.length>1) {
                char [] ckey = kvSplit[0];
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
