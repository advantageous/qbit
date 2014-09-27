package org.qbit.spi;

import org.boon.collections.MultiMap;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/26/14.
 */
public class FactoryImpl implements Factory{

    private ProtocolParser defaultProtocol = new ProtocolParserVersion1();

    private List<ProtocolParser> protocolParserList = new ArrayList<>();

    {
        protocolParserList.add(new ProtocolParserVersion1());
    }


    @Override
    public MethodCall<Object> createMethodCall(String address,
                                       String objectName,
                                       String methodName,
                                       Object args,
                                       MultiMap<String, String> params){


        if (args != null) {
            ProtocolParser parser = selectProtocolParser(args, params);

            if (parser != null) {
                return parser.parse(address, objectName, methodName, args, params);
            }
        }


        return defaultProtocol.parse(address, objectName, methodName, args, params);



    }

    @Override
    public MethodCall<Object> createMethodCallByAddress(String address, Object args, MultiMap<String, String> params) {
        return createMethodCall(address, "", "", args, params);
    }

    @Override
    public MethodCall<Object> createMethodCallByNames(String methodName, String objectName, Object args, MultiMap<String, String> params) {
        return createMethodCall(null, methodName, objectName, args, params);
    }

    private ProtocolParser selectProtocolParser(Object args, MultiMap<String, String> params) {
        for (ProtocolParser parser : protocolParserList) {
            if (parser.supports(args, params)) {
                return parser;
            }
        }
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
