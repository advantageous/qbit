package org.qbit.service;

/**
 * Created by Richard on 8/26/14.
 */
public interface AfterMethodCall {


    boolean after(MethodCall call, Response response);
}
