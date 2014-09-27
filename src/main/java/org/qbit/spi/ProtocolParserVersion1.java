package org.qbit.spi;

import org.boon.Str;
import org.boon.collections.MultiMap;
import org.boon.core.reflection.FastStringUtils;
import org.boon.json.JsonParserAndMapper;
import org.boon.json.JsonParserFactory;
import org.boon.primitive.CharScanner;
import org.qbit.message.MethodCall;
import org.qbit.service.method.impl.MethodCallImpl;

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

            if ( sargs.charAt(0) == PROTOCOL_MARKER && sargs.charAt(1) == PROTOCOL_VERSION_1) {
                return true;

            }
            return false;
    }

    @Override
    public MethodCall<Object> parse(Object body) {





        if (body!=null) {
            if (body instanceof String) {
                return processStringBody((String) body);
            }
        }

        return null;


    }



    private static ThreadLocal<JsonParserAndMapper> jsonParserThreadLocal = new ThreadLocal<JsonParserAndMapper>() {
        @Override
        protected JsonParserAndMapper initialValue() {
            return new JsonParserFactory().create();
        }
    };


    private MethodCall<Object> processStringBody(String args) {

        if (args.isEmpty()) {
            return null;
        }

        final char[] chars = FastStringUtils.toCharArray(args);
        if (chars.length > 2 &&
                chars[PROTOCOL_MARKER_POSITION]
                        == PROTOCOL_MARKER) {

            final char versionMarker = chars[VERSION_MARKER_POSITION];

            if (versionMarker == PROTOCOL_VERSION_1) {
                return handleFastBodySubmissionVersion1Chars(chars);
            } else {
                die("Unsupported method call", args);
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


        String methodName = FastStringUtils.noCopyStringFromChars(chars[
                METHOD_NAME_POS]);


        String objectName = FastStringUtils.noCopyStringFromChars(chars[
                OBJECT_NAME_POS]);


        char[] body = chars[ ARGS_POS ];


        final char[][] argumentList = CharScanner.split(body,
                (char) PROTOCOL_ARG_SEPARATOR);

        List<Object> argList = new ArrayList<>();

        for (int index=0; index< argumentList.length; index++) {
            char [] charArgs = argumentList[index];
            Object arg = jsonParserThreadLocal.get().parse(charArgs);
            argList.add(arg);
        }

        MethodCallImpl methodCall =  MethodCallImpl.method(id, address, returnAddress, objectName, methodName, argList, null);

        return methodCall;

    }


}
