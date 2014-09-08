package org.qbit.service;

import org.qbit.message.MethodCall;

/**
 * Created by Richard on 8/26/14.
 */
public interface BeforeMethodCall {

    boolean before(MethodCall call);
}
