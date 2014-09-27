package org.qbit.spi;

import org.boon.Str;
import org.boon.collections.MultiMap;
import org.boon.core.Conversions;
import org.qbit.Factory;
import org.qbit.message.MethodCall;
import org.qbit.service.Service;
import org.qbit.service.ServiceBundle;
import org.qbit.service.impl.ServiceBundleImpl;
import org.qbit.service.method.impl.MethodCallImpl;

/**
 * Created by Richard on 9/26/14.
 */
public class FactoryImpl implements Factory{


    @Override
    public MethodCall createMethodCall(String name, String path,
                                       Object args,
                                       MultiMap<String, String> params){
        if (params!=null && Str.isEmpty(name)) {
            name = params.get("methodName");
        }
        return MethodCallImpl.method(name, path, Conversions.toList(args));

    }

    @Override
    public ServiceBundle createBundle(String path) {
        return new ServiceBundleImpl(path, this);
    }

    @Override
    public Service createService(Object object) {
        return null;
    }
}
