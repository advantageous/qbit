package org.qbit.spi;

import org.boon.Str;
import org.boon.collections.MultiMap;
import org.boon.core.Conversions;
import org.boon.core.reflection.FastStringUtils;
import org.boon.json.JsonParserAndMapper;
import org.boon.json.JsonParserFactory;
import org.boon.primitive.CharScanner;
import org.qbit.Factory;
import org.qbit.message.MethodCall;
import static org.qbit.service.Protocol.*;
import org.qbit.service.Service;
import org.qbit.service.ServiceBundle;
import org.qbit.service.impl.ServiceBundleImpl;
import org.qbit.service.impl.ServiceImpl;
import org.qbit.service.impl.ServiceMethodCallHandlerImpl;
import org.qbit.service.method.impl.MethodCallImpl;

import java.util.concurrent.TimeUnit;

import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/26/14.
 */
public class FactoryImpl implements Factory{




    @Override
    public MethodCall createMethodCall(String name, String path,
                                       Object args,
                                       MultiMap<String, String> params){
        if (params!=null && Str.isEmpty(name)) {
            name = params.get(METHOD_NAME_KEY);
        }

        if (args instanceof String) {
            return processArgs((String) args);
        } else {
            return MethodCallImpl.method(name, path, Conversions.toList(args));

        }


    }

    ThreadLocal<JsonParserAndMapper> jsonParserThreadLocal = new ThreadLocal<JsonParserAndMapper>() {
        @Override
        protected JsonParserAndMapper initialValue() {
            return new JsonParserFactory().create();
        }
    };


    private MethodCall processArgs(String args) {
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
        } else {
            return jsonParserThreadLocal.get().parse(MethodCallImpl.class, chars);
        }
    }

    private MethodCall handleFastBodySubmissionVersion1Chars(char[] args) {

        int index=0;
        index++;
        index++;

        final char[][] chars = CharScanner.splitFromStartWithLimit(args,
                (char) PROTOCOL_SEPARATOR, index, 3);

        String address = FastStringUtils.noCopyStringFromChars(chars[
                ADDRESS_POS]);

        String returnAddress = FastStringUtils.noCopyStringFromChars(chars[
                RETURN_ADDRESS_POS]);


        String methodName = FastStringUtils.noCopyStringFromChars(chars[
                METHOD_NAME_POS]);


        String objectName = FastStringUtils.noCopyStringFromChars(chars[
                OBJECT_NAME_POS]);

        return null;

    }

    @Override
    public ServiceBundle createBundle(String path) {
        return new ServiceBundleImpl(path, this);
    }

    @Override
    public Service createService(Object object) {
        return new ServiceImpl(
                object.getClass().getSimpleName(),
                object,
                5, TimeUnit.MILLISECONDS,
                50,
                new ServiceMethodCallHandlerImpl()
        );
    }
}
