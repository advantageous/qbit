package org.qbit;

import org.boon.collections.MultiMap;
import org.qbit.message.MethodCall;
import org.qbit.service.Service;
import org.qbit.service.ServiceBundle;

public interface Factory {


      MethodCall<Object> createMethodCall(String address,
                                          String returnAddress,
                                          String objectName,
                                          String methodName,
                                          Object args,
                                          MultiMap<String, String> params);


      MethodCall<Object> createMethodCallByAddress(String address,
                                        Object args,
                                        MultiMap<String, String> params);


      MethodCall<Object> createMethodCallByNames(
              String methodName, String objectName, Object args,
                                                 MultiMap<String, String> params);

      ServiceBundle createBundle(String path);

      Service createService(Object object);
}
