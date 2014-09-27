package org.qbit;

import org.boon.collections.MultiMap;
import org.qbit.message.MethodCall;
import org.qbit.service.Service;
import org.qbit.service.ServiceBundle;

public interface Factory {


      MethodCall<Object> createMethodCall(String methodName, String path, Object args, MultiMap<String, String> params);

      ServiceBundle createBundle(String path);

      Service createService(Object object);
}
