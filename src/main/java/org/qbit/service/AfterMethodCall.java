package org.qbit.service;

import org.qbit.message.MethodCall;
import org.qbit.message.Response;

/**
 * Created by Richard on 8/26/14.
 */
public interface AfterMethodCall {


    boolean after(MethodCall call, Response response);
}
